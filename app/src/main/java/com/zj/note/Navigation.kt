@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.zj.note

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zj.ink.NoteScreen
import com.zj.ink.data.EditNoteViewModel
import com.zj.ink.data.NoteViewModel
import com.zj.ink.edit.EditNoteScreen
import com.zj.ink.md.ImagePreview
import com.zj.ink.md.ImageViewModel
import com.zj.ink.preview.NotePreview
import com.zj.ink.preview.NotePreviewViewModel
import com.zj.ink.widget.NoteAppWidget.Companion.NOTE_FROM_VALUE

private const val NOTES_ROUTE = "notes"
private const val EDIT_NOTE_ROUTE = "edit_note"
const val DEFAULT_INVALID_ID = -1
private const val IMAGE_PREVIEW_ROUTE = "image_preview"
private const val NOTE_PREVIEW_ROUTE = "edit_preview"

const val NOTE_ID_ARG = "noteId"
const val IMAGE_URL_ARG = "imageUrl"

@SuppressLint("NewApi")
@Composable
fun NoteApp(noteId: Int = DEFAULT_INVALID_ID, noteFromArg: String?) {
    val navController = rememberNavController()
    val startRoute = if (noteId <= DEFAULT_INVALID_ID) {
        NOTES_ROUTE
    } else {
        "$NOTE_PREVIEW_ROUTE/${noteId}"
    }
    SharedTransitionLayout {
        NavHost(
            navController, startDestination = startRoute, modifier = Modifier.fillMaxSize()
        ) {
            animateComposable(route = NOTES_ROUTE) {
                val viewModel = hiltViewModel<NoteViewModel>()
                NoteScreen(
                    viewModel = viewModel,
                    previewNote = {
                        navController.navigate("$NOTE_PREVIEW_ROUTE/${it}")
                    }, editNote = {
                        navController.navigate("$EDIT_NOTE_ROUTE/${it}")
                    })
            }
            animateComposable(
                route = "$EDIT_NOTE_ROUTE/{$NOTE_ID_ARG}",
                arguments = listOf(navArgument(NOTE_ID_ARG) { type = NavType.IntType }),
            ) { backStackEntry ->
                val id =
                    backStackEntry.arguments?.getInt(NOTE_ID_ARG) ?: return@animateComposable

                val viewModel = hiltViewModel<EditNoteViewModel>()
                if (id != DEFAULT_INVALID_ID) {
                    LaunchedEffect(Unit) {
                        viewModel.getNoteById(id)
                    }
                }
                EditNoteScreen(
                    viewModel = viewModel,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@animateComposable,
                    onImageClick = {
                        navController.navigate("${IMAGE_PREVIEW_ROUTE}/${Uri.encode(it)}")
                    }) {
                    navController.popBackStack()
                }
            }
            animateComposable(
                route = "$NOTE_PREVIEW_ROUTE/{$NOTE_ID_ARG}",
                arguments = listOf(navArgument(NOTE_ID_ARG) { type = NavType.IntType }),
            ) { backStackEntry ->
                val id =
                    backStackEntry.arguments?.getInt(NOTE_ID_ARG) ?: return@animateComposable

                val viewModel = hiltViewModel<NotePreviewViewModel>()
                if (id != DEFAULT_INVALID_ID) {
                    LaunchedEffect(Unit) {
                        viewModel.getNoteById(id)
                    }
                }
                NotePreview(
                    viewModel = viewModel,
                    showBackButton = noteFromArg != NOTE_FROM_VALUE,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@animateComposable,
                    onImageClick = {
                        navController.navigate("${IMAGE_PREVIEW_ROUTE}/${Uri.encode(it)}")
                    },
                    onEditClick = {
                        navController.navigate("$EDIT_NOTE_ROUTE/${it}")
                    },
                    back = { navController.popBackStack() })
            }
            composable(
                route = "${IMAGE_PREVIEW_ROUTE}/{${IMAGE_URL_ARG}}",
                arguments = listOf(navArgument(IMAGE_URL_ARG) { type = NavType.StringType }),
            ) { backStackEntry ->
                val imageUrl =
                    backStackEntry.arguments?.getString(IMAGE_URL_ARG)?.let { Uri.decode(it) }
                        ?: return@composable
                val viewModel: ImageViewModel = hiltViewModel()
                ImagePreview(
                    viewModel = viewModel,
                    imageUrl = imageUrl,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable,
                ) {
                    navController.popBackStack()
                }
            }
        }
    }
}

private fun NavGraphBuilder.animateComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        enterTransition = { slideEnter() },
        exitTransition = { slideExit() },
        popEnterTransition = { slidePopEnter() },
        popExitTransition = { slidePopExit() },
        arguments = arguments
    ) {
        content(it)
    }
}


private fun AnimatedContentTransitionScope<NavBackStackEntry>.slideEnter(): EnterTransition {
    return slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.slideExit(): ExitTransition {
    return slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.slidePopEnter(): EnterTransition {
    return slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.slidePopExit(): ExitTransition {
    return slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
    )
}
