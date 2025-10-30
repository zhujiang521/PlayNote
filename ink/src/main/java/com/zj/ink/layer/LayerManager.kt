package com.zj.ink.layer

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.ink.strokes.Stroke
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 图层管理器
 * 负责图层的创建、管理、排序和渲染控制
 */
class LayerManager {

    // 图层列表（按z-order排序，索引越大越在上层）
    private val _layers = mutableStateListOf<Layer>()
    val layers: SnapshotStateList<Layer> = _layers

    // 当前活跃图层ID
    private val _activeLayerId = MutableStateFlow<String?>(null)
    val activeLayerId: StateFlow<String?> = _activeLayerId.asStateFlow()

    // 选中的图层ID列表（支持多选）
    private val _selectedLayerIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedLayerIds: StateFlow<Set<String>> = _selectedLayerIds.asStateFlow()

    // 图层变更监听器
    private val layerChangeListeners = mutableListOf<(LayerChangeEvent) -> Unit>()

    init {
        // 创建默认背景图层
        createBackgroundLayer()
    }

    /**
     * 添加图层变更监听器
     */
    fun addLayerChangeListener(listener: (LayerChangeEvent) -> Unit) {
        layerChangeListeners.add(listener)
    }

    /**
     * 移除图层变更监听器
     */
    fun removeLayerChangeListener(listener: (LayerChangeEvent) -> Unit) {
        layerChangeListeners.remove(listener)
    }

    /**
     * 触发图层变更事件
     */
    private fun notifyLayerChanged(event: LayerChangeEvent) {
        layerChangeListeners.forEach { it(event) }
    }

    /**
     * 创建新的绘图图层
     */
    fun createDrawingLayer(name: String? = null): Layer {
        val layerName = name ?: "图层 ${_layers.size + 1}"
        val newLayer = Layer.createDrawingLayer(layerName).copy(
            zOrder = getNextZOrder()
        )

        _layers.add(newLayer)
        setActiveLayer(newLayer.id)
        notifyLayerChanged(LayerChangeEvent.LayerAdded(newLayer))

        return newLayer
    }

    /**
     * 创建文本图层
     */
    fun createTextLayer(name: String? = null, content: String = ""): Layer {
        val layerName = name ?: "文本图层 ${_layers.count { it.getLayerType() == LayerType.TEXT } + 1}"
        val newLayer = Layer.createTextLayer(layerName, content).copy(
            zOrder = getNextZOrder()
        )

        _layers.add(newLayer)
        setActiveLayer(newLayer.id)
        notifyLayerChanged(LayerChangeEvent.LayerAdded(newLayer))

        return newLayer
    }

    /**
     * 创建图像图层
     */
    fun createImageLayer(name: String? = null, imagePath: String = ""): Layer {
        val layerName = name ?: "图像图层 ${_layers.count { it.getLayerType() == LayerType.IMAGE } + 1}"
        val newLayer = Layer.createImageLayer(layerName, imagePath).copy(
            zOrder = getNextZOrder()
        )

        _layers.add(newLayer)
        setActiveLayer(newLayer.id)
        notifyLayerChanged(LayerChangeEvent.LayerAdded(newLayer))

        return newLayer
    }

    /**
     * 创建背景图层
     */
    private fun createBackgroundLayer(): Layer {
        val backgroundLayer = Layer.createBackgroundLayer()
        _layers.add(backgroundLayer)
        return backgroundLayer
    }

    /**
     * 删除图层
     */
    fun deleteLayer(layerId: String): Boolean {
        val layer = findLayerById(layerId) ?: return false

        // 不允许删除背景图层
        if (layer.getLayerType() == LayerType.BACKGROUND) {
            return false
        }

        val removed = _layers.removeAll { it.id == layerId }

        if (removed) {
            // 如果删除的是活跃图层，切换到其他图层
            if (_activeLayerId.value == layerId) {
                val nextLayer = _layers.lastOrNull { it.getLayerType() != LayerType.BACKGROUND }
                setActiveLayer(nextLayer?.id)
            }

            // 从选中列表中移除
            _selectedLayerIds.value = _selectedLayerIds.value - layerId

            notifyLayerChanged(LayerChangeEvent.LayerRemoved(layer))
        }

        return removed
    }

    /**
     * 复制图层
     */
    fun duplicateLayer(layerId: String): Layer? {
        val originalLayer = findLayerById(layerId) ?: return null

        val duplicatedLayer = originalLayer.duplicate().copy(
            zOrder = getNextZOrder()
        )

        _layers.add(duplicatedLayer)
        setActiveLayer(duplicatedLayer.id)
        notifyLayerChanged(LayerChangeEvent.LayerAdded(duplicatedLayer))

        return duplicatedLayer
    }

    /**
     * 更新图层
     */
    fun updateLayer(layerId: String, updater: (Layer) -> Layer): Boolean {
        val index = _layers.indexOfFirst { it.id == layerId }
        if (index == -1) return false

        val oldLayer = _layers[index]
        val newLayer = updater(oldLayer)

        if (newLayer != oldLayer) {
            _layers[index] = newLayer
            notifyLayerChanged(LayerChangeEvent.LayerUpdated(oldLayer, newLayer))
        }

        return true
    }

    /**
     * 设置活跃图层
     */
    fun setActiveLayer(layerId: String?) {
        val layer = layerId?.let { findLayerById(it) }
        if (layer?.isEditable() != false) { // null或可编辑的图层都可以设为活跃
            _activeLayerId.value = layerId
            notifyLayerChanged(LayerChangeEvent.ActiveLayerChanged(layerId))
        }
    }

    /**
     * 获取活跃图层
     */
    fun getActiveLayer(): Layer? {
        return _activeLayerId.value?.let { findLayerById(it) }
    }

    /**
     * 选中图层（支持多选）
     */
    fun selectLayers(layerIds: Set<String>) {
        val validIds = layerIds.filter { findLayerById(it) != null }.toSet()
        _selectedLayerIds.value = validIds
        notifyLayerChanged(LayerChangeEvent.SelectionChanged(validIds))
    }

    /**
     * 添加到选中列表
     */
    fun addToSelection(layerId: String) {
        if (findLayerById(layerId) != null) {
            _selectedLayerIds.value = _selectedLayerIds.value + layerId
            notifyLayerChanged(LayerChangeEvent.SelectionChanged(_selectedLayerIds.value))
        }
    }

    /**
     * 从选中列表移除
     */
    fun removeFromSelection(layerId: String) {
        _selectedLayerIds.value = _selectedLayerIds.value - layerId
        notifyLayerChanged(LayerChangeEvent.SelectionChanged(_selectedLayerIds.value))
    }

    /**
     * 清空选择
     */
    fun clearSelection() {
        _selectedLayerIds.value = emptySet()
        notifyLayerChanged(LayerChangeEvent.SelectionChanged(emptySet()))
    }

    /**
     * 移动图层到指定位置
     */
    fun moveLayer(layerId: String, targetIndex: Int): Boolean {
        val currentIndex = _layers.indexOfFirst { it.id == layerId }
        if (currentIndex == -1 || targetIndex < 0 || targetIndex >= _layers.size) {
            return false
        }

        val layer = _layers.removeAt(currentIndex)
        val adjustedIndex = if (targetIndex > currentIndex) targetIndex - 1 else targetIndex
        _layers.add(adjustedIndex, layer)

        // 重新计算z-order
        reorderLayers()

        notifyLayerChanged(LayerChangeEvent.LayerReordered(layerId, currentIndex, adjustedIndex))
        return true
    }

    /**
     * 向上移动图层
     */
    fun moveLayerUp(layerId: String): Boolean {
        val currentIndex = _layers.indexOfFirst { it.id == layerId }
        if (currentIndex == -1 || currentIndex >= _layers.size - 1) {
            return false
        }

        return moveLayer(layerId, currentIndex + 1)
    }

    /**
     * 向下移动图层
     */
    fun moveLayerDown(layerId: String): Boolean {
        val currentIndex = _layers.indexOfFirst { it.id == layerId }
        if (currentIndex <= 0) {
            return false
        }

        return moveLayer(layerId, currentIndex - 1)
    }

    /**
     * 移动到顶层
     */
    fun moveToTop(layerId: String): Boolean {
        return moveLayer(layerId, _layers.size - 1)
    }

    /**
     * 移动到底层（但在背景层之上）
     */
    fun moveToBottom(layerId: String): Boolean {
        val backgroundIndex = _layers.indexOfFirst { it.getLayerType() == LayerType.BACKGROUND }
        val targetIndex = if (backgroundIndex >= 0) backgroundIndex + 1 else 0
        return moveLayer(layerId, targetIndex)
    }

    /**
     * 根据ID查找图层
     */
    fun findLayerById(layerId: String): Layer? {
        return _layers.find { it.id == layerId }
    }

    /**
     * 获取可见图层列表（按渲染顺序）
     */
    fun getVisibleLayers(): List<Layer> {
        return _layers.filter { it.isVisible }.sortedBy { it.zOrder }
    }

    /**
     * 获取可编辑图层列表
     */
    fun getEditableLayers(): List<Layer> {
        return _layers.filter { it.isEditable() }
    }

    /**
     * 获取指定类型的图层
     */
    fun getLayersByType(type: LayerType): List<Layer> {
        return _layers.filter { it.getLayerType() == type }
    }

    /**
     * 获取下一个z-order值
     */
    private fun getNextZOrder(): Int {
        return (_layers.maxOfOrNull { it.zOrder } ?: 0) + 1
    }

    /**
     * 重新排序图层的z-order
     */
    private fun reorderLayers() {
        _layers.forEachIndexed { index, layer ->
            val newZOrder = if (layer.getLayerType() == LayerType.BACKGROUND) {
                -1000 // 背景层始终在底部
            } else {
                index * 10 // 留出间隔便于插入
            }

            if (layer.zOrder != newZOrder) {
                updateLayer(layer.id) { it.withZOrder(newZOrder) }
            }
        }
    }

    /**
     * 合并选中的图层
     */
    fun mergeLayers(layerIds: List<String>): Layer? {
        if (layerIds.size < 2) return null

        val layersToMerge = layerIds.mapNotNull { findLayerById(it) }
            .filter { it.getLayerType() == LayerType.DRAWING }

        if (layersToMerge.size < 2) return null

        // 创建新的合并图层
        val mergedLayer = createDrawingLayer("合并图层")

        // TODO: 实现实际的图层合并逻辑
        // 这里需要合并所有图层的笔迹数据

        // 删除原图层
        layersToMerge.forEach { deleteLayer(it.id) }

        return mergedLayer
    }

    /**
     * 清空所有图层（保留背景层）
     */
    fun clearAllLayers() {
        val backgroundLayer = _layers.find { it.getLayerType() == LayerType.BACKGROUND }
        _layers.clear()

        backgroundLayer?.let { _layers.add(it) }

        _activeLayerId.value = null
        _selectedLayerIds.value = emptySet()

        notifyLayerChanged(LayerChangeEvent.AllLayersCleared)
    }

    /**
     * 获取图层统计信息
     */
    fun getLayerStats(): LayerStats {
        return LayerStats(
            totalLayers = _layers.size,
            visibleLayers = _layers.count { it.isVisible },
            lockedLayers = _layers.count { it.isLocked },
            drawingLayers = _layers.count { it.getLayerType() == LayerType.DRAWING },
            textLayers = _layers.count { it.getLayerType() == LayerType.TEXT },
            imageLayers = _layers.count { it.getLayerType() == LayerType.IMAGE }
        )
    }
}

/**
 * 图层变更事件
 */
sealed class LayerChangeEvent {
    data class LayerAdded(val layer: Layer) : LayerChangeEvent()
    data class LayerRemoved(val layer: Layer) : LayerChangeEvent()
    data class LayerUpdated(val oldLayer: Layer, val newLayer: Layer) : LayerChangeEvent()
    data class LayerReordered(val layerId: String, val fromIndex: Int, val toIndex: Int) : LayerChangeEvent()
    data class ActiveLayerChanged(val layerId: String?) : LayerChangeEvent()
    data class SelectionChanged(val selectedIds: Set<String>) : LayerChangeEvent()
    object AllLayersCleared : LayerChangeEvent()
}

/**
 * 图层统计信息
 */
data class LayerStats(
    val totalLayers: Int,
    val visibleLayers: Int,
    val lockedLayers: Int,
    val drawingLayers: Int,
    val textLayers: Int,
    val imageLayers: Int
)