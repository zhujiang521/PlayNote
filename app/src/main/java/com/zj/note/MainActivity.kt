package com.zj.note

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zj.note.ui.theme.PlayNoteTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var noteId: Int = DEFAULT_INVALID_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        disposeIntent(intent)
        setContent {
            PlayNoteTheme {
                NoteApp(noteId)
            }
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        disposeIntent(intent)
    }

    private fun disposeIntent(intent: Intent?) {
        try {
            noteId =
                intent?.getIntExtra(NOTE_ID_ARG, DEFAULT_INVALID_ID) ?: DEFAULT_INVALID_ID
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}