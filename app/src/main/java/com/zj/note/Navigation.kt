@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3AdaptiveApi::class)

package com.zj.note

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.zj.data.lce.NoContent
import com.zj.data.model.INVALID_ID
import com.zj.data.model.Note
import com.zj.ink.NoteScreen
import com.zj.ink.data.EditNoteViewModel
import com.zj.ink.data.NoteViewModel
import com.zj.ink.edit.EditNoteScreen
import com.zj.ink.md.ImagePreview
import com.zj.ink.md.ImageViewModel
import com.zj.ink.preview.NotePreview
import com.zj.ink.preview.NotePreviewViewModel
import com.zj.ink.widget.NoteAppWidget.Companion.NOTE_FROM_VALUE
import kotlinx.serialization.Serializable

const val DEFAULT_INVALID_ID = -1

// Navigation 3 路由键对象
@Serializable
object NotesList : NavKey

@Serializable
data class EditNote(val note: Note?) : NavKey

@Serializable
data class NotePreview(val note: Note) : NavKey

@Serializable
data class NotePreviewById(val noteId: Int) : NavKey

@Serializable
data class ImagePreview(val imageUrl: String) : NavKey

@SuppressLint("NewApi")
@Composable
fun NoteApp(noteId: Int = DEFAULT_INVALID_ID, noteFromArg: String?) {
    // 根据启动参数确定初始路由
    val initialRoute = if (noteId <= DEFAULT_INVALID_ID) {
        NotesList
    } else {
        NotePreviewById(noteId)
    }

    val backStack = rememberNavBackStack(initialRoute)

    // Override the defaults so that there isn't a horizontal space between the panes.
    // See b/418201867
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)
    SharedTransitionLayout {
        NavDisplay(
            backStack = backStack,
            onBack = {
                backStack.removeLastOrNull()
            },
            sceneStrategy = listDetailStrategy,
            entryProvider = entryProvider {
                entry<NotesList>(
                    metadata = ListDetailSceneStrategy.listPane(
                        detailPlaceholder = {
                            NoContent()
                        }
                    )
                ) {
                    val viewModel = hiltViewModel<NoteViewModel>()
                    NoteScreen(
                        viewModel = viewModel,
                        previewNote = { note ->
                            val last = backStack.last()
                            if (last is NotePreview) {
                                if (last.note.id != note.id) {
                                    backStack.add(NotePreview(note))
                                }
                            } else {
                                backStack.add(NotePreview(note))
                            }
                        },
                        editNote = { note ->
                            backStack.add(EditNote(note))
                        }
                    )
                    val last = backStack.last()
                    if (last is NotePreview) {
                        viewModel.setSelectedNoteId(last.note.id)
                    } else {
                        viewModel.setSelectedNoteId(INVALID_ID)
                    }
                }
                entry<NotePreview>(
                    metadata = ListDetailSceneStrategy.detailPane()
                ) { note ->
                    val viewModel = hiltViewModel<NotePreviewViewModel>()
                    viewModel.setNote(note = note.note)
                    NotePreview(
                        viewModel = viewModel,
                        showBackButton = noteFromArg != NOTE_FROM_VALUE,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = null,
                        onImageClick = { imageUrl ->
                            backStack.add(ImagePreview(imageUrl))
                        },
                        onEditClick = {
                            backStack.add(EditNote(note = note.note))
                        },
                        back = { backStack.removeLastOrNull() })
                }
                entry<NotePreviewById> { note ->
                    val viewModel = hiltViewModel<NotePreviewViewModel>()
                    val id = note.noteId
                    if (id != DEFAULT_INVALID_ID) {
                        LaunchedEffect(Unit) {
                            viewModel.getNoteById(id)
                        }
                    }
                    NotePreview(
                        viewModel = viewModel,
                        showBackButton = noteFromArg != NOTE_FROM_VALUE,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = null,
                        onImageClick = { imageUrl ->
                            backStack.add(ImagePreview(imageUrl))
                        },
                        onEditClick = {
                            backStack.add(EditNote(note = viewModel.note.value))
                        },
                        back = { backStack.removeLastOrNull() })
                }
                entry<EditNote> { notePreview ->
                    val viewModel = hiltViewModel<EditNoteViewModel>()
                    val note = notePreview.note
                    if (note != null) {
                        LaunchedEffect(Unit) {
                            viewModel.setNote(notePreview.note)
                        }
                    } else {
                        viewModel.resetNote()
                    }
                    EditNoteScreen(
                        viewModel = viewModel,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = null,
                        onImageClick = { imageUrl ->
                            backStack.add(ImagePreview(imageUrl))
                        }
                    ) {
                        backStack.removeLastOrNull()
                    }
                }
                entry<ImagePreview> { imagePreview ->
                    val viewModel: ImageViewModel = hiltViewModel()
                    ImagePreview(
                        viewModel = viewModel,
                        imageUrl = imagePreview.imageUrl,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = null
                    ) {
                        backStack.removeLastOrNull()
                    }
                }
            }
        )
    }
}