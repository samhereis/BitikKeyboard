package com.shoktuk.shoktukkeyboard.keyboard

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class KeyboardRowsModel {
    val row1: SnapshotStateList<KeyboardKey> = mutableStateListOf()
    val row2: SnapshotStateList<KeyboardKey> = mutableStateListOf()
    val row3: SnapshotStateList<KeyboardKey> = mutableStateListOf()

    init {
        row1 += KeyboardLetters.row1
        row2 += KeyboardLetters.row2
        row3 += KeyboardLetters.row3
    }
}