package com.zj.note

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.zj.data.model.Note
import com.zj.note.ui.theme.PlayNoteTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var noteId: Int = DEFAULT_INVALID_ID
    private val viewModel: SendNoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            PlayNoteTheme {
                NoteApp(noteId)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        try {
            noteId =
                intent?.getIntExtra(NOTE_ID_ARG, DEFAULT_INVALID_ID) ?: DEFAULT_INVALID_ID
            if (noteId != DEFAULT_INVALID_ID) {
                return
            }
            when (intent?.action) {
                Intent.ACTION_SEND -> {
                    handleSendIntent(intent)
                }

                Intent.ACTION_VIEW -> {
                    handleViewIntent(intent)
                }

                else -> {
                    noteId =
                        intent?.getIntExtra(NOTE_ID_ARG, DEFAULT_INVALID_ID) ?: DEFAULT_INVALID_ID
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("NewApi")
    private fun handleSendIntent(intent: Intent) {
        lifecycleScope.launch {
            try {
                val type = intent.type
                if (type == "text/markdown" || type == "text/x-markdown") {
                    // 处理分享的Markdown文件
                    val uri = intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    uri?.let {
                        val newNoteId = viewModel.readAndSaveMarkdownFile(it)
                        noteId = newNoteId.toInt()
                        refreshContent()
                    }
                } else if (type == "text/plain") {
                    // 处理分享的纯文本
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                    if (sharedText.isNotEmpty()) {
                        val note = Note(
                            id = 0,
                            title = getString(com.zj.data.R.string.note),
                            content = sharedText,
                            timestamp = System.currentTimeMillis()
                        )
                        val newNoteId = viewModel.insertNote(note)
                        noteId = newNoteId.toInt()
                        refreshContent()
                    }
                } else if (type?.startsWith("image/") == true) {
                    val uri = intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    uri?.let {
                        val newNoteId = viewModel.readAndSaveImageFile(it)
                        noteId = newNoteId.toInt()
                        refreshContent()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleViewIntent(intent: Intent) {
        val uri = intent.data
        val type = intent.type

        uri?.let {
            lifecycleScope.launch {
                try {
                    // 根据MIME类型或文件扩展名判断是否为Markdown文件
                    if (type == "text/markdown" || type == "text/x-markdown" ||
                        uri.toString().endsWith(".md", ignoreCase = true) ||
                        uri.toString().endsWith(".markdown", ignoreCase = true) ||
                        type == "text/plain"
                    ) {
                        val newNoteId = viewModel.readAndSaveMarkdownFile(it)
                        noteId = newNoteId.toInt()
                        refreshContent()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun refreshContent() {
        // 重新设置content以刷新UI
        setContent {
            PlayNoteTheme {
                NoteApp(noteId)
            }
        }
    }

    companion object {
        const val DEFAULT_INVALID_ID = -1
        const val NOTE_ID_ARG = "noteId"
    }
}
