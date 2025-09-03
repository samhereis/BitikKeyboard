package com.shoktuk.shoktukkeyboard.keyboard

import android.graphics.drawable.InsetDrawable
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toColorInt
import androidx.core.widget.TextViewCompat
import com.shoktuk.shoktukkeyboard.ui.theme.ButtonStyle
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme.dpToPx

object SystemKeyBuilder {

    // CHANGED: four public creators only

    fun systemButton_Text(
        service: InputMethodService, text: String, buttonHeight: Int, style: ButtonStyle = KeyboardTheme.getSystemButtonStyle(service), onClick: (() -> Unit)? = null, weight: Float = 0f
    ): View {
        val root = newContainer(service, style, buttonHeight, weight)
        root.addView(makeCenteredContent(service, style, buttonHeight, textToSet = text))
        root.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onClick?.invoke()
            TopRowBuilder_Old.onTypedListener?.invoke()
        }
        return root
    }

    fun systemButton_Icon(
        service: InputMethodService, assetPath: String, buttonHeight: Int, style: ButtonStyle = KeyboardTheme.getSystemButtonStyle(service), onClick: (() -> Unit)? = null, weight: Float = 0f
    ): View {
        val root = newContainer(service, style, buttonHeight, weight)
        root.addView(makeCenteredContent(service, style, buttonHeight, iconAssetPath = assetPath))
        root.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onClick?.invoke()
            TopRowBuilder_Old.onTypedListener?.invoke()
        }
        return root
    }

    fun expandableSystemButton_Text(
        service: InputMethodService, text: String, buttonHeight: Int, textToCommit: String, style: ButtonStyle = KeyboardTheme.getLetterButtonStyle_Normal(service)
    ): View {
        val root = newContainer(service, style, buttonHeight, weight = 1f)
        root.addView(makeCenteredContent(service, style, buttonHeight, textToSet = text))
        root.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            service.currentInputConnection?.commitText(textToCommit, 1)
            TopRowBuilder_Old.onTypedListener?.invoke()
        }
        return root
    }

    fun expandableSystemButton_Icon(
        service: InputMethodService, assetPath: String, textToCommit: String, buttonHeight: Int, style: ButtonStyle = KeyboardTheme.getLetterButtonStyle_Normal(service)
    ): View {
        val root = newContainer(service, style, buttonHeight, weight = 1f)
        root.addView(makeCenteredContent(service, style, buttonHeight, iconAssetPath = assetPath))
        root.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            service.currentInputConnection?.commitText(textToCommit, 1)
            TopRowBuilder_Old.onTypedListener?.invoke()
        }
        return root
    }

    // OPTIONAL: use this to keep Shift/Del logic uniform
    fun forKey(
        service: InputMethodService, key: KeyEntry, buttonHeight: Int, onCapsChange: (Boolean) -> Unit
    ): View {
        val baseStyle = KeyboardTheme.getSystemButtonStyle(service)
        val style = if (key.name == "Shift" && MyKeyboardService.isCaps) baseStyle.copy(fillColor = KeyboardTheme.getColor(3), textColor = KeyboardTheme.getColor(1))
        else baseStyle

        val root = newContainer(service, style, buttonHeight)

        val icon = when (key.name) {
            "Shift" -> KeyboardTheme.SHIFT_ICON_FILE
            "Del" -> KeyboardTheme.DELETE_ICON_FILE
            else -> null
        }
        root.addView(makeCenteredContent(service, style, buttonHeight, textToSet = key.lowercase.orEmpty(), iconAssetPath = icon))

        when (key.name) {
            "Shift" -> wireShift(root, onCapsChange)
            "Del" -> wireDelete(root, service)
            else -> root.setOnClickListener { it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
        }
        return root
    }

    // internals

    private fun newContainer(
        service: InputMethodService, style: ButtonStyle, buttonHeight: Int, weight: Float = 0f
    ): FrameLayout {
        return FrameLayout(service).apply {
            isClickable = true
            isFocusable = true
            foregroundGravity = Gravity.CENTER
            layoutParams = if (weight == 0f) {
                LinearLayout.LayoutParams(
                    KeyboardTheme.getSystemButtonWidth(service), buttonHeight
                )
            } else {
                LinearLayout.LayoutParams(0, buttonHeight, weight)
            }
            val inset = dpToPx(service, 6)
            val pill = KeyboardTheme.createDrawableFromStyle(service, style)
            background = InsetDrawable(pill, inset, inset, inset, inset) // inner visual padding; full cell clickable
        }
    }

    private fun makeCenteredContent(
        service: InputMethodService,
        style: ButtonStyle,
        buttonHeight: Int,
        textToSet: String? = null,
        iconAssetPath: String? = null
    ): View {
        val container = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
        }
        val textColorInt = style.textColor.toColorInt()

        if (iconAssetPath != null) {
            KeyboardTheme.loadAssetDrawable(service, iconAssetPath)?.let { d ->
                val icon = DrawableCompat.wrap(d).mutate()
                DrawableCompat.setTint(icon, textColorInt)
                val size = (buttonHeight / 2.5).toInt()
                val iv = ImageView(service).apply {
                    layoutParams = LinearLayout.LayoutParams(size, size).apply { gravity = Gravity.CENTER }
                    setImageDrawable(icon)
                }
                container.addView(iv)
            }
        }

        if (!textToSet.isNullOrEmpty()) {
            val tv = TextView(service).apply {
                gravity = Gravity.CENTER
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                isAllCaps = false
                includeFontPadding = false
                setTextColor(textColorInt)
                setSingleLine(false)
                maxLines = 1
                ellipsize = null // CHANGED: allow shrink instead of ellipsize
                textSize = KeyboardTheme.getLetterButtonStyle_Normal(service).textSizeSp.value
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { gravity = Gravity.CENTER }
                text = textToSet
            }

            // CHANGED: enable AutoSize so text scales down to fit
            val maxSp = KeyboardTheme.getLetterButtonStyle_Normal(service).textSizeSp.value.toInt()
            val minSp = 1
            val stepSp = 1
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                tv, minSp, maxSp, stepSp, TypedValue.COMPLEX_UNIT_SP
            )

            container.addView(tv)
        }
        return container
    }

    private fun wireShift(root: View, onCapsChange: (Boolean) -> Unit) {
        root.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onCapsChange(!MyKeyboardService.isCaps)
        }
        root.setOnLongClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            onCapsChange(true)
            true
        }
    }

    private fun wireDelete(root: View, service: InputMethodService) {
        val handler = Handler(Looper.getMainLooper())
        val interval = 100L
        val repeater = object : Runnable {
            override fun run() {
                performDelete(service)
                handler.postDelayed(this, interval)
            }
        }
        root.setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    performDelete(service)
                    handler.postDelayed(repeater, interval)
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(repeater)
                    true
                }

                else -> false
            }
        }
    }

    private fun performDelete(service: InputMethodService) {
        val ic = service.currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(2, 0)
        val deleteCount = when {
            before.isNullOrEmpty() -> 1
            before.length >= 2 && Character.isSurrogatePair(before[0], before[1]) -> 2
            before.toString().contains("\\uD803".toRegex()) -> 2
            else -> 1
        }
        ic.deleteSurroundingText(deleteCount, 0)
        ic.getTextAfterCursor(2, 0)?.let { after ->
            if (after.toString().matches("[\\uD800-\\uDFFF]".toRegex())) {
                ic.deleteSurroundingText(1, 0)
            }
        }
        TopRowBuilder_Old.onTypedListener?.invoke()
    }
}