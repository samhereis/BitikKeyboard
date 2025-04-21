package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.widget.Button
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toColorInt

object SystemKeyBuilder {

    fun createSystemKey(
        service: InputMethodService, key: KeyEntry, layout: KeyboardLayout, buttonHeight: Int, margin: Int, isCaps: Boolean, onCapsChange: (Boolean) -> Unit
    ): Button {
        return Button(service).apply {
            text = ""
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 0)
            compoundDrawablePadding = 0

            // Style setup
            textSize = KeyboardTheme.getSystemButtonStyle(service).textSizeSp
            setTextColor(KeyboardTheme.getSystemButtonStyle(service).textColor.toColorInt())
            layoutParams = android.widget.LinearLayout.LayoutParams(
                KeyboardTheme.getSystemButtonWidth(service), buttonHeight
            ).apply {
                marginStart = margin
                marginEnd = margin
            }

            // Background styling
            background = when {
                key.name == "Shift" && isCaps -> KeyboardTheme.createDrawableFromStyle(
                    service, KeyboardTheme.getSystemButtonStyle(service).copy(fillColor = KeyboardTheme.colorIndexes[3])
                )

                else -> KeyboardTheme.createDrawableFromStyle(service, KeyboardTheme.getSystemButtonStyle(service))
            }

            // Icon setup
            val assetPath = when (key.name) {
                "Shift" -> KeyboardTheme.SHIFT_ICON_FILE
                "Del" -> KeyboardTheme.DELETE_ICON_FILE
                else -> null
            }
            assetPath?.let { path ->
                KeyboardTheme.loadAssetDrawable(service, path)?.let { icon ->
                    val tintedIcon = DrawableCompat.wrap(icon).mutate().apply {
                        DrawableCompat.setTint(this, KeyboardTheme.getSystemButtonStyle(service).textColor.toColorInt())
                        setBounds(0, 0, buttonHeight / 2, buttonHeight / 2)
                    }
                    setCompoundDrawables(null, tintedIcon, null, null)
                    setPadding(0, KeyboardTheme.getButtonHeight() / 4, 0, 0)
                }
            }

            // Input handlers
            when (key.name) {
                "Shift" -> setupShiftButton(onCapsChange, isCaps)
                "Del" -> setupDeleteButton(service)
                else -> setOnClickListener { /* Other key handlers */ }
            }
        }
    }

    private fun Button.setupShiftButton(
        onCapsChange: (Boolean) -> Unit, isCaps: Boolean
    ) {
        setOnClickListener {
            onCapsChange(!isCaps)
        }
    }

    private fun Button.setupDeleteButton(service: InputMethodService) {
        val deleteHandler = Handler(Looper.getMainLooper())
        val deleteInterval = 100L // 0.25 seconds
        var deleteRunnable: Runnable? = null

        fun performDelete() {
            service.currentInputConnection?.let { ic ->
                val textBefore = ic.getTextBeforeCursor(2, 0)
                val deleteCount = when {
                    textBefore.isNullOrEmpty() -> 1
                    textBefore.length >= 2 && Character.isSurrogatePair(textBefore[0], textBefore[1]) -> 2
                    textBefore.toString().contains("\\uD803".toRegex()) -> 2
                    else -> 1
                }

                ic.deleteSurroundingText(deleteCount, 0)

                ic.getTextAfterCursor(2, 0)?.let { remaining ->
                    if (remaining.toString().matches("[\\uD800-\\uDFFF]".toRegex())) {
                        ic.deleteSurroundingText(1, 0)
                    }
                }
                TopRowBuilder_Old.onTypedListener?.invoke()
            }
        }

        deleteRunnable = object : Runnable {
            override fun run() {
                deleteHandler.postDelayed(this, deleteInterval)
                performDelete()
            }
        }

        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Initial immediate delete
                    performDelete()
                    // Start continuous deletion
                    deleteHandler.postDelayed(deleteRunnable!!, deleteInterval)
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Stop continuous deletion
                    deleteHandler.removeCallbacks(deleteRunnable!!)
                    true
                }

                else -> false
            }
        }
    }
}