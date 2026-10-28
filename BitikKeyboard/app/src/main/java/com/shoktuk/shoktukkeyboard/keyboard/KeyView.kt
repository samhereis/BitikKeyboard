package com.shoktuk.shoktukkeyboard.keyboard

import Haptics
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColorInt
import androidx.core.widget.TextViewCompat
import com.shoktuk.shoktukkeyboard.R
import com.shoktuk.shoktukkeyboard.project.data.Coloring
import com.shoktuk.shoktukkeyboard.project.data.HoldabilityColoring
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.coloring
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.holdabilityColoring
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme.dpToPx
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class KeyView(
    context: Context,
    private val key: KeyEntry,
    private val isCaps: Boolean,
    private val buttonHeight: Int,
    private val buttonWidth: Int,
    private val onKeyClick: (String) -> Unit,
    private val onLongPress: (String?) -> Unit
) : FrameLayout(context) {

    private val handler = Handler(Looper.getMainLooper())
    private val longPressDelay = 250L

    private var isTouchInBounds = false
    private var isLongPressed = false
    private var currentBackgroundColorIndex = 0
    private var popupWindow: PopupWindow? = null
    private var picker: AlternatesPicker? = null
    private var lastRawX = 0f
    private var lastRawY = 0f

    private lateinit var mainText: TextView
    private lateinit var subText_bottom: TextView
    private lateinit var subText_top: TextView
    private var style = KeyboardTheme.getLetterButtonStyle_Normal(context, MyKeyboardService.showLetterTranscription)

    private lateinit var visualContainer: FrameLayout
    private val visualInsetPx_H = dpToPx(context, if (MyKeyboardService.maxButtonInOneRow > 10) KeyboardTheme.KEY_MARGIN_DP_OnlyVisual_H else (KeyboardTheme.KEY_MARGIN_DP_OnlyVisual_H * 1.5f).toInt())
    private val visualInsetPx_V = dpToPx(context, KeyboardTheme.KEY_MARGIN_DP_OnlyVisual_V)

    private var lastErrorAt = 0L
    private var overlayMainText: TextView? = null

    private var hintButtonTextSize = KeyboardTheme.getHintButtonTextSize(context)

    private fun notifyError(message: String, t: Throwable? = null) {
        val now = System.currentTimeMillis()
        if (now - lastErrorAt >= 1000) {
            lastErrorAt = now
            try {
                android.widget.Toast.makeText(context.applicationContext, message, android.widget.Toast.LENGTH_SHORT).show()
            } catch (_: Throwable) {
            }
        }
        t?.printStackTrace()
    }

    init {
        if (MyKeyboardService.isTamga && isCaps && MyKeyboardService.current_writingSystem == WritingSystem.Bitik) {
            style = KeyboardTheme.getLetterButtonStyle_UpperCase(context, MyKeyboardService.showLetterTranscription)
        }
        setupView()
        setupContent()
        setupTouchHandling()
        setupClickListeners()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        dismissOverlay()
    }

    private fun setupView() {
        layoutParams = LinearLayout.LayoutParams(
            buttonWidth, buttonHeight
        ).apply {
            marginStart = 0
            marginEnd = 0
        }
        foregroundGravity = Gravity.CENTER
        setPadding(0, 0, 0, 0)
        setBackgroundColor(Color.TRANSPARENT)
    }

    private fun applyStyle() {
        currentBackgroundColorIndex = if (isCaps) {
            key.backgroundColorIndex_uppercase ?: 1
        } else {
            key.backgroundColorIndex_lowercase ?: 1
        }

        if (context.coloring == Coloring.Off) {
            style = style.copy(fillColor = KeyboardTheme.getColor(1), textColor = KeyboardTheme.getColor(2))
        } else {
            style = style.copy(fillColor = KeyboardTheme.getColor(currentBackgroundColorIndex))
        }

        if (::visualContainer.isInitialized) {
            visualContainer.background = KeyboardTheme.createDrawableFromStyle(context, style)
        }
    }

    private fun setupContent() {
        removeAllViews()

        visualContainer = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
                (this as MarginLayoutParams).setMargins(visualInsetPx_H, visualInsetPx_V, visualInsetPx_H, visualInsetPx_V)
            }
        }

        addView(visualContainer)

        val view = LayoutInflater.from(context).inflate(R.layout.keyboard_key, visualContainer, true)

        mainText = view.findViewById(R.id.mainText)
        subText_bottom = view.findViewById(R.id.bottomSubText)
        subText_top = view.findViewById(R.id.topSubText)

        applyStyle()
        updateContent()
        addHoldIndicatorIfNeeded(view)
    }

    fun pxFromDp(dp: Float, context: Context) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)

    fun pxFromSp(sp: Float, context: Context) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sp, context.resources.displayMetrics)

    private fun updateContent() {
        var mainTextSize = style.textSizeSp
        if (MyKeyboardService.current_writingSystem == WritingSystem.Latin && isCaps) {
            mainTextSize /= 1.1f
        }

        val mainPx = pxFromSp(mainTextSize, context)
        mainText.apply {
            text = getCurrentMainText()?.markDisplay()
            setTextColor(style.textColor.toColorInt())
            setTextSize(TypedValue.COMPLEX_UNIT_PX, mainPx)
            typeface = if (MyKeyboardService.current_writingSystem == WritingSystem.Bitik) KeyboardTheme.bitikTypeface(context) else null
            maxLines = 1
            ellipsize = null
            includeFontPadding = false

            paint.strokeWidth = 0.5f
            paint.style = Paint.Style.FILL_AND_STROKE

            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                mainText, 1, mainPx.toInt(), 1, TypedValue.COMPLEX_UNIT_PX
            )
        }

        val first = getCurrentSubText()?.toString().orEmpty()
        val second = getCurrentSubText_Alt()?.toString().orEmpty()

        val topMaxPx = pxFromSp(hintButtonTextSize, context)
        val topMinPx = pxFromSp(1f, context)

        val trailing = KeyboardTheme.hintsOnTrailing && MyKeyboardService.current_writingSystem != WritingSystem.Bitik
        val hintPaddingEnd = if (trailing) dpToPx(context, 6) else 0
        val hintPaddingVertical = dpToPx(context, 3)

        subText_top.apply {
            text = second
            setPadding(0, hintPaddingVertical, hintPaddingEnd, 0)
            maxLines = 1
            isSingleLine = true
            ellipsize = TextUtils.TruncateAt.END
            includeFontPadding = false

            setLineSpacing(0f, 1f)
            setTextColor(style.textColor.toColorInt())
            setTypeface(null, Typeface.BOLD)

            (layoutParams as ConstraintLayout.LayoutParams).apply {
                width = ConstraintLayout.LayoutParams.MATCH_PARENT
                height = ConstraintLayout.LayoutParams.MATCH_PARENT
                gravity = if (trailing) Gravity.TOP or Gravity.END else Gravity.TOP or Gravity.CENTER_HORIZONTAL
            }.also { layoutParams = it }

            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                subText_top, topMinPx.toInt(), topMaxPx.toInt(), 1, TypedValue.COMPLEX_UNIT_PX
            )
        }

        val bottomMaxPx = pxFromSp(hintButtonTextSize, context)
        val bottomMinPx = pxFromSp(1f, context)
        subText_bottom.apply {
            text = first
            setPadding(0, 0, hintPaddingEnd, hintPaddingVertical)
            maxLines = 1
            isSingleLine = true
            ellipsize = TextUtils.TruncateAt.END
            includeFontPadding = false

            setLineSpacing(0f, 1f)
            setTextColor(style.textColor.toColorInt())
            setTypeface(null, Typeface.BOLD)

            (layoutParams as ConstraintLayout.LayoutParams).apply {
                width = ConstraintLayout.LayoutParams.MATCH_PARENT
                height = ConstraintLayout.LayoutParams.MATCH_PARENT
                gravity = if (trailing) Gravity.BOTTOM or Gravity.END else Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            }.also { layoutParams = it }

            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                subText_bottom, bottomMinPx.toInt(), bottomMaxPx.toInt(), 1, TypedValue.COMPLEX_UNIT_PX
            )
        }

        if (!MyKeyboardService.showLetterTranscription) {
            subText_top.text = ""
            subText_bottom.text = ""
            mainText.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.textSizeSp)
        }
    }

    private fun setupClickListeners() {
        setOnClickListener {
            var text = getCurrentMainText()
            if (isLongPressed) {
                text = getCurrentMainText_Hold()
            }
            text = text?.replace("\uD803\\", "")
            onKeyClick(text!!)
        }
    }

    private fun setupTouchHandling() {
        val longPressRunnable = Runnable {
            var hasHoldValue: Boolean = false;

            if (isCaps && key.upperCaseHold != null) {
                hasHoldValue = true
            }
            if (!isCaps && key.lowerCaseHold != null) {
                hasHoldValue = true
            }

            val alternates = currentAlternates()
            if (isTouchInBounds && alternates.isNotEmpty()) {
                isLongPressed = true
                dismissOverlay()
                Haptics.longPress(this)
                picker = AlternatesPicker(this, alternates, currentAlternateHints(), lastRawX, lastRawY)
                return@Runnable
            }

            if (isTouchInBounds && hasHoldValue) {
                isLongPressed = true
                onLongPress(getCurrentMainText_Hold())
                applyStyle()
                updateContent()
                updateOverlay()
            }

            TopRowBuilder_Old.onTypedListener?.invoke()
        }

        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastRawX = event.rawX
                    lastRawY = event.rawY
                    isTouchInBounds = true
                    handler.postDelayed(longPressRunnable, longPressDelay)
                    showOverlay()
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    lastRawX = event.rawX
                    lastRawY = event.rawY
                    picker?.let {
                        it.moveTo(event.rawX, event.rawY)
                        return@setOnTouchListener true
                    }
                    val inBounds = event.x in 0f..width.toFloat() && event.y in 0f..height.toFloat()
                    if (isTouchInBounds != inBounds) {
                        isTouchInBounds = inBounds
                        if (!isTouchInBounds) {
                            handler.removeCallbacks(longPressRunnable)
                            dismissOverlay()
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    picker?.let {
                        val chosen = it.selectedText()
                        it.dismiss()
                        picker = null
                        handler.removeCallbacks(longPressRunnable)
                        onKeyClick(chosen)
                        TopRowBuilder_Old.onTypedListener?.invoke()
                        resetState()
                        return@setOnTouchListener true
                    }
                    if (isTouchInBounds && !isLongPressed) {
                        performClick()
                    }
                    handler.removeCallbacks(longPressRunnable)
                    dismissOverlay()
                    resetState()
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    picker?.dismiss()
                    picker = null
                    handler.removeCallbacks(longPressRunnable)
                    dismissOverlay()
                    resetState()
                    true
                }

                else -> false
            }
        }
    }

    fun showOverlay() {
        try {
            dismissOverlay()

            if (!androidx.core.view.ViewCompat.isAttachedToWindow(this)) return

            val w = kotlin.math.max(1, width)
            val h = kotlin.math.max(1, height)
            if (w <= 0 || h <= 0) return

            val overlayView = createOverlayView().apply {
                layoutParams = LayoutParams(w, h)
            }

            popupWindow = PopupWindow(overlayView, w, h, false).apply {
                isOutsideTouchable = false
                isFocusable = false
                elevation = 24f
                setClippingEnabled(true)
                animationStyle = 0
            }

            val xOff = (width - w) / 2
            val yOff = -h - dpToPxInt(50)

            try {
                popupWindow?.showAsDropDown(this, xOff, yOff, Gravity.START)
            } catch (e: android.view.WindowManager.BadTokenException) {
                popupWindow?.dismiss()
                popupWindow = null
                notifyError("Can't show key preview on this device (window token).", e)
            } catch (e: Throwable) {
                popupWindow?.dismiss()
                popupWindow = null
                notifyError("Failed to show key preview.", e)
            }
        } catch (e: Throwable) {
            notifyError("Unexpected error while showing preview.", e)
        }
    }

    private fun dpToPxInt(dp: Int): Int = max(
        1, (resources.displayMetrics.density * dp).roundToInt()
    )

    private fun createOverlayView(): View {
        return FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            background = com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme.createDrawableFromStyle(context, style)

            val pad = dpToPxInt(6)
            setPadding(pad, pad, pad, pad)

            val column = android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }

            val main = TextView(context).apply {
                text = getCurrentMainText()?.markDisplay()
                setTextColor(style.textColor.toColorInt())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, style.textSizeSp)
                maxLines = 1
                isAllCaps = false

                paint.strokeWidth = 0.5f
                paint.style = Paint.Style.FILL_AND_STROKE
            }

            column.addView(
                main, android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            addView(
                column, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER
                )
            )

            overlayMainText = main
        }
    }

    private fun updateOverlay() {
        try {
            val main = if (isLongPressed) getCurrentMainText_Hold() else getCurrentMainText()
            val hint = if (isLongPressed) getCurrentSubText_Hold() else getCurrentSubText()

            overlayMainText?.text = (main ?: "").markDisplay()

            var colorToSet: Int = KeyboardTheme.getColor(1).toColorInt()
            colorToSet = if (isCaps) {
                when {
                    key.backgroundColorIndex_uppercase_Hold != null -> KeyboardTheme.getColor(key.backgroundColorIndex_uppercase_Hold!!).toColorInt()
                    key.backgroundColorIndex_lowercase != null -> KeyboardTheme.getColor(key.backgroundColorIndex_lowercase!!).toColorInt()
                    else -> colorToSet
                }
            } else {
                when {
                    key.backgroundColorIndex_lowercase_Hold != null -> KeyboardTheme.getColor(key.backgroundColorIndex_lowercase_Hold!!).toColorInt()
                    key.backgroundColorIndex_lowercase != null -> KeyboardTheme.getColor(key.backgroundColorIndex_lowercase!!).toColorInt()
                    else -> colorToSet
                }
            }
            popupWindow?.contentView?.setBackgroundColor(colorToSet)
        } catch (e: Throwable) {
            notifyError("Failed to update preview.", e)
        }
    }

    private fun addHoldIndicatorIfNeeded(root: View) {
        if (context.holdabilityColoring == HoldabilityColoring.Off) {
            return
        }

        // Bitik key data selectively marks which hold values are indicator-worthy; other
        // scripts don't carry that flag at all, so they just need a hold value to exist.
        if (MyKeyboardService.current_writingSystem == WritingSystem.Bitik) {
            if (isCaps) {
                if (key.uppercase_HoldabilityIndicator != "on") {
                    return
                }
            } else {
                if (key.lowerCase_HoldabilityIndicator != "on") {
                    return
                }
            }
        }

        val hasHoldVariant = if (isCaps) {
            key.upperCaseHold != null
        } else {
            key.lowerCaseHold != null
        }
        if (!hasHoldVariant) return

        val radius = dpToPx(context, 6).toFloat()
        val clipToParentOutline = true
        val topPadding = dpToPx(context, 0)
        val indicatorColor = KeyboardTheme.getColor(3).toColorInt()
        val indicatorHeight = dpToPx(context, 1)

        if (clipToParentOutline && android.os.Build.VERSION.SDK_INT >= 21) {
            (root as? ViewGroup)?.clipToPadding = false
            root.clipToOutline = true
        }

        val indicator = View(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, indicatorHeight
            ).apply {
                gravity = Gravity.TOP
                topMargin = topPadding
            }

            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(indicatorColor)

                cornerRadii = floatArrayOf(
                    radius, radius, radius, radius, 0f, 0f, 0f, 0f
                )
            }
        }

        (root as ViewGroup).addView(indicator)
    }

    /** Hold always opens the picker: the key's variants, else its legacy hold value, else just its own glyph. */
    private fun currentAlternates(): List<String> {
        if (key.name == "Shift" || key.name == "Del" || key.name.isNullOrEmpty()) return emptyList()
        val variants = (if (isCaps) key.upperCaseAlternates else key.lowerCaseAlternates).orEmpty()
        if (variants.isNotEmpty()) return variants
        return listOfNotNull(getCurrentMainText_Hold() ?: getCurrentMainText()).filter { it.isNotEmpty() }
    }

    private fun currentAlternateHints(): List<String> = (if (isCaps) key.upperCaseAlternateHints else key.lowerCaseAlternateHints).orEmpty()

    private fun getCurrentMainText(): String? = if (isCaps) key.uppercase else key.lowercase

    private fun getCurrentSubText(): CharSequence? = if (isCaps) key.upperCaseRomanization ?: "" else key.lowerCaseRomanization ?: ""

    private fun getCurrentSubText_Alt(): CharSequence? = if (isCaps) key.upperCaseRomanization_Alt ?: "" else key.lowerCaseRomanization_Alt ?: ""

    private fun getCurrentMainText_Hold(): String? = if (isCaps) key.upperCaseHold else key.lowerCaseHold

    private fun getCurrentSubText_Hold(): CharSequence? = if (isCaps) key.upperCaseHoldHint ?: "" else key.lowerCaseHoldHint ?: ""

    /** The strip shown above a held key: drag sideways to pick one of its variants, release to type it. */
    private inner class AlternatesPicker(
        private val anchor: View, private val items: List<String>, private val hints: List<String>, holdRawX: Float, holdRawY: Float
    ) {
        // Neutral backdrop so the highlighted cell stands out even on vowel keys, whose own fill matches the highlight.
        private val backdrop = KeyboardTheme.getLetterButtonStyle_Normal(context)
        private val density = resources.displayMetrics.density
        private val maxCell = 40 * density
        private val minCell = 26 * density
        private val cell: Float
        private val direction: Int
        private val originX = holdRawX
        private val originY = holdRawY
        private var selected = 0
        private val cells = arrayOfNulls<LinearLayout>(items.size)
        private val perRow: Int
        private val rows: Int
        private val popup: PopupWindow

        init {
            val location = IntArray(2)
            anchor.getLocationOnScreen(location)
            val screenWidth = resources.displayMetrics.widthPixels
            val midX = location[0] + anchor.width / 2f
            val margin = 4 * density
            val rightRoom = screenWidth - midX - margin
            val leftRoom = midX - margin
            direction = if (rightRoom >= leftRoom) 1 else -1
            val steps = max(items.size - 0.5f, 1f)
            val room = max(rightRoom, leftRoom)
            cell = (room / steps).coerceIn(minCell, maxCell)
            // Even at the narrowest cell some variants may not fit on one row: the rest wrap onto rows above.
            perRow = min(items.size, max((room / cell + 0.51f).toInt(), 1))
            rows = (items.size + perRow - 1) / perRow

            items.indices.forEach { index ->
                val c = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    addView(TextView(context).apply {
                        text = items[index].markDisplay()
                        gravity = Gravity.CENTER
                        setTextColor(backdrop.textColor.toColorInt())
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, style.textSizeSp)
                        includeFontPadding = false
                    })
                    val hint = hints.getOrNull(index).orEmpty()
                    if (hint.isNotEmpty()) {
                        addView(TextView(context).apply {
                            text = hint
                            gravity = Gravity.CENTER
                            setTextColor(backdrop.textColor.toColorInt())
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f)
                            includeFontPadding = false
                        })
                    }
                }
                cells[index] = c
            }
            // Row 0 sits closest to the key; further rows stack above it.
            val grid = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
            for (r in rows - 1 downTo 0) {
                val indices = (r * perRow until min((r + 1) * perRow, items.size)).toList()
                grid.addView(LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = if (direction > 0) Gravity.START else Gravity.END
                    (if (direction > 0) indices else indices.reversed()).forEach { index ->
                        addView(cells[index], LinearLayout.LayoutParams(cell.toInt(), anchor.height))
                    }
                }, LinearLayout.LayoutParams((cell * perRow).toInt(), anchor.height))
            }
            val pad = (4 * density).toInt()
            val frame = FrameLayout(context).apply {
                background = GradientDrawable().apply {
                    setColor(backdrop.fillColor.toColorInt())
                    cornerRadius = 12 * density
                    setStroke((1 * density).toInt(), backdrop.textColor.toColorInt() and 0x00FFFFFF or 0x55000000)
                }
                elevation = 6 * density
                setPadding(pad, pad, pad, pad)
                addView(grid)
            }
            val totalWidth = (cell * perRow).toInt() + 2 * pad
            popup = PopupWindow(frame, totalWidth, anchor.height * rows + 2 * pad).apply {
                isTouchable = false
                isFocusable = false
            }
            val left = if (direction > 0) midX - cell / 2f - pad else midX + cell / 2f + pad - totalWidth
            val xOff = (left - location[0]).toInt()
            val yOff = -(anchor.height * (rows + 1) + 4 * pad)
            popup.showAsDropDown(anchor, xOff, yOff, Gravity.START)
            highlight()
        }

        private fun highlight() {
            cells.forEachIndexed { index, c ->
                c?.background = if (index == selected) GradientDrawable().apply {
                    setColor(KeyboardTheme.getColor(4).toColorInt())
                    cornerRadius = 8 * density
                } else null
            }
        }

        fun moveTo(rawX: Float, rawY: Float) {
            val column = ((rawX - originX) * direction / cell).roundToInt().coerceIn(0, perRow - 1)
            // Dragging up from the hold point reaches the rows above.
            val row = ((originY - rawY) / anchor.height).roundToInt().coerceIn(0, rows - 1)
            val step = (row * perRow + column).coerceAtMost(items.lastIndex)
            if (step != selected) {
                selected = step
                highlight()
                Haptics.contextClick(anchor)
            }
        }

        fun selectedText(): String = items[selected]

        fun dismiss() {
            try {
                popup.dismiss()
            } catch (_: Throwable) {
            }
        }
    }

    private fun String.markDisplay(): String =
        if (isNotEmpty() && Character.getType(codePointAt(0)) == Character.NON_SPACING_MARK.toInt()) "\u25CC" + this else this

    fun dismissOverlay() {
        try {
            popupWindow?.dismiss()
        } catch (e: Throwable) {
            notifyError("Failed to hide key preview.", e)
        } finally {
            popupWindow = null
            overlayMainText = null
        }
    }

    private fun resetState() {
        handler.postDelayed({
            isLongPressed = false
            applyStyle()
            updateContent()
        }, 0)
    }
}