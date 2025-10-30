package com.zj.ink.brush

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * 画笔预设管理器
 * 负责画笔预设的保存、加载、管理和同步
 */
class BrushPresetManager(private val context: Context) {

    companion object {
        private val Context.presetDataStore: DataStore<Preferences> by preferencesDataStore(name = "brush_presets")
        private val USER_PRESETS_KEY = stringPreferencesKey("user_presets")
        private val RECENT_PRESETS_KEY = stringPreferencesKey("recent_presets")
        private val FAVORITE_PRESETS_KEY = stringPreferencesKey("favorite_presets")
        private const val MAX_RECENT_PRESETS = 10
        private const val MAX_USER_PRESETS = 50
    }

    private val dataStore = context.presetDataStore
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 获取所有系统预设
     */
    fun getSystemPresets(): List<BrushPreset> {
        return BrushPreset.createSystemPresets()
    }

    /**
     * 获取用户自定义预设
     */
    fun getUserPresets(): Flow<List<BrushPreset>> {
        return dataStore.data.map { preferences ->
            val presetsJson = preferences[USER_PRESETS_KEY] ?: "[]"
            parsePresetsList(presetsJson)
        }
    }

    /**
     * 获取最近使用的预设
     */
    fun getRecentPresets(): Flow<List<BrushPreset>> {
        return dataStore.data.map { preferences ->
            val presetsJson = preferences[RECENT_PRESETS_KEY] ?: "[]"
            parsePresetsList(presetsJson)
        }
    }

    /**
     * 获取收藏的预设
     */
    fun getFavoritePresets(): Flow<List<BrushPreset>> {
        return dataStore.data.map { preferences ->
            val presetsJson = preferences[FAVORITE_PRESETS_KEY] ?: "[]"
            parsePresetsList(presetsJson)
        }
    }

    /**
     * 获取指定分类的预设
     */
    fun getPresetsByCategory(category: PresetCategory): Flow<List<BrushPreset>> {
        return when (category) {
            PresetCategory.SYSTEM -> kotlinx.coroutines.flow.flowOf(getSystemPresets())
            PresetCategory.USER -> getUserPresets()
            PresetCategory.RECENT -> getRecentPresets()
            PresetCategory.FAVORITE -> getFavoritePresets()
        }
    }

    /**
     * 保存用户自定义预设
     */
    suspend fun saveUserPreset(preset: BrushPreset): Result<BrushPreset> {
        return try {
            val currentPresets = getUserPresets().first().toMutableList()

            // 检查是否超过最大数量限制
            if (currentPresets.size >= MAX_USER_PRESETS) {
                return Result.failure(Exception("用户预设数量已达上限 ($MAX_USER_PRESETS)"))
            }

            // 检查是否已存在相同ID的预设
            val existingIndex = currentPresets.indexOfFirst { it.id == preset.id }
            val finalPreset = if (existingIndex >= 0) {
                // 更新现有预设
                currentPresets[existingIndex] = preset
                preset
            } else {
                // 添加新预设
                val newPreset = preset.copy(
                    id = if (preset.id.isEmpty()) UUID.randomUUID().toString() else preset.id,
                    isSystemPreset = false,
                    createdAt = System.currentTimeMillis()
                )
                currentPresets.add(newPreset)
                newPreset
            }

            // 保存到DataStore
            dataStore.edit { preferences ->
                preferences[USER_PRESETS_KEY] = json.encodeToString(currentPresets)
            }

            Result.success(finalPreset)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除用户预设
     */
    suspend fun deleteUserPreset(presetId: String): Result<Boolean> {
        return try {
            val currentPresets = getUserPresets().first().toMutableList()
            val removed = currentPresets.removeAll { it.id == presetId }

            if (removed) {
                dataStore.edit { preferences ->
                    preferences[USER_PRESETS_KEY] = json.encodeToString(currentPresets)
                }

                // 同时从收藏中移除
                removeFavoritePreset(presetId)
            }

            Result.success(removed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 添加到最近使用
     */
    suspend fun addToRecentPresets(preset: BrushPreset) {
        try {
            val currentRecents = getRecentPresets().first().toMutableList()

            // 移除已存在的相同预设
            currentRecents.removeAll { it.id == preset.id }

            // 添加到列表开头
            currentRecents.add(0, preset)

            // 限制最大数量
            if (currentRecents.size > MAX_RECENT_PRESETS) {
                currentRecents.removeAt(currentRecents.size - 1)
            }

            dataStore.edit { preferences ->
                preferences[RECENT_PRESETS_KEY] = json.encodeToString(currentRecents)
            }
        } catch (e: Exception) {
            // 静默处理错误，不影响主要功能
        }
    }

    /**
     * 添加到收藏
     */
    suspend fun addFavoritePreset(preset: BrushPreset): Result<Boolean> {
        return try {
            val currentFavorites = getFavoritePresets().first().toMutableList()

            // 检查是否已收藏
            if (currentFavorites.any { it.id == preset.id }) {
                return Result.success(false) // 已存在
            }

            currentFavorites.add(preset)

            dataStore.edit { preferences ->
                preferences[FAVORITE_PRESETS_KEY] = json.encodeToString(currentFavorites)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从收藏中移除
     */
    suspend fun removeFavoritePreset(presetId: String): Result<Boolean> {
        return try {
            val currentFavorites = getFavoritePresets().first().toMutableList()
            val removed = currentFavorites.removeAll { it.id == presetId }

            if (removed) {
                dataStore.edit { preferences ->
                    preferences[FAVORITE_PRESETS_KEY] = json.encodeToString(currentFavorites)
                }
            }

            Result.success(removed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 根据ID查找预设
     */
    suspend fun findPresetById(presetId: String): BrushPreset? {
        // 先从系统预设中查找
        getSystemPresets().find { it.id == presetId }?.let { return it }

        // 再从用户预设中查找
        getUserPresets().first().find { it.id == presetId }?.let { return it }

        return null
    }

    /**
     * 搜索预设
     */
    suspend fun searchPresets(query: String): List<BrushPreset> {
        if (query.isBlank()) return emptyList()

        val allPresets = mutableListOf<BrushPreset>()
        allPresets.addAll(getSystemPresets())
        allPresets.addAll(getUserPresets().first())

        return allPresets.filter { preset ->
            preset.name.contains(query, ignoreCase = true) ||
            preset.description.contains(query, ignoreCase = true) ||
            preset.brushTypeName.contains(query, ignoreCase = true)
        }
    }

    /**
     * 导出用户预设
     */
    suspend fun exportUserPresets(): String {
        val userPresets = getUserPresets().first()
        return json.encodeToString(userPresets)
    }

    /**
     * 导入用户预设
     */
    suspend fun importUserPresets(presetsJson: String): Result<Int> {
        return try {
            val importedPresets = parsePresetsList(presetsJson)
            val currentPresets = getUserPresets().first().toMutableList()

            var importedCount = 0
            importedPresets.forEach { preset ->
                if (preset.isValid() && !currentPresets.any { it.id == preset.id }) {
                    val userPreset = preset.copy(
                        id = UUID.randomUUID().toString(),
                        isSystemPreset = false,
                        createdAt = System.currentTimeMillis()
                    )
                    currentPresets.add(userPreset)
                    importedCount++
                }
            }

            // 限制总数量
            if (currentPresets.size > MAX_USER_PRESETS) {
                currentPresets.subList(0, MAX_USER_PRESETS)
            }

            dataStore.edit { preferences ->
                preferences[USER_PRESETS_KEY] = json.encodeToString(currentPresets)
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 清空用户数据
     */
    suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.remove(USER_PRESETS_KEY)
            preferences.remove(RECENT_PRESETS_KEY)
            preferences.remove(FAVORITE_PRESETS_KEY)
        }
    }

    /**
     * 解析预设列表JSON
     */
    private fun parsePresetsList(presetsJson: String): List<BrushPreset> {
        return try {
            json.decodeFromString<List<BrushPreset>>(presetsJson)
                .filter { it.isValid() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 获取预设使用统计
     */
    suspend fun getPresetUsageStats(): Map<String, Int> {
        // 这里可以实现预设使用次数的统计
        // 暂时返回空Map，后续可以扩展
        return emptyMap()
    }
}