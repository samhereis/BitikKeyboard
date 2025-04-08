package com.shoktuk.shoktukkeyboard.project.systems

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TranscriptionViewModel(context: Context) : ViewModel() {
    private val transcriber = JSTranscriber(context)

    fun transcribe(text: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = transcriber.getTranscription(text)
            onResult(result)
        }
    }

    override fun onCleared() {
        transcriber.cleanup()
    }
}