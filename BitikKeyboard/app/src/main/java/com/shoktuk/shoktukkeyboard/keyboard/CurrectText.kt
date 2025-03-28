package com.shoktuk.shoktukkeyboard.keyboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CurrectText : ViewModel() {
    private val _globalString = MutableLiveData<String>("Default Text")
    val globalString: LiveData<String> = _globalString

    fun updateGlobalString(newText: String) {
        _globalString.value = newText
    }
}