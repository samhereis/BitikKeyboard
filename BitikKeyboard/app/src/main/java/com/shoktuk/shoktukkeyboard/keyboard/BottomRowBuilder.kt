package com.shoktuk.shoktukkeyboard.keyboard

import Haptics
import android.graphics.drawable.GradientDrawable
import android.graphics.Typeface
import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.inputmethodservice.InputMethodService
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.widget.TextViewCompat
import com.shoktuk.shoktukkeyboard.project.data.BitikDialect
import com.shoktuk.shoktukkeyboard.project.data.BitikVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.keyboardVariant
import com.shoktuk.shoktukkeyboard.project.data.Latin_Variant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.bitikDialect
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.latinVariant
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.vibrations
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.wordSeparator
import com.shoktuk.shoktukkeyboard.project.data.Vibrations
import com.shoktuk.shoktukkeyboard.project.data.WordSeparator
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme.dpToPx

object BottomRowBuilder {
    private const val SPACE_HOLD_MILLIS = 400L
    private var carousel: LanguageCarousel? = null

    private fun keyboardLabel(system: WritingSystem) = when (system) {
        WritingSystem.Bitik -> "𐰌𐰄𐱆𐰚"
        WritingSystem.Latin -> "Latin"
        WritingSystem.Kiril -> "Кирил"
        WritingSystem.Arab -> "العربية"
    }

    private val hiddenLabels = HashMap<View, Float>()

    /** The system keyboard's trackpad mode: every label disappears while the cursor is being dragged. */
    private fun setLabelsHidden(anchor: View, hidden: Boolean) {
        if (!hidden) {
            hiddenLabels.forEach { (view, alpha) -> view.alpha = alpha }
            hiddenLabels.clear()
            return
        }
        fun walk(view: View) {
            if ((view is TextView || view is android.widget.ImageView) && view.tag != KEEP_VISIBLE) {
                hiddenLabels[view] = view.alpha
                view.alpha = 0f
            }
            if (view is ViewGroup) for (i in 0 until view.childCount) walk(view.getChildAt(i))
        }
        walk(anchor.rootView)
    }

    private const val KEEP_VISIBLE = "keepVisible"

    /** Every key that can drag the cursor (space, enter) has one of these; they all show together while the cursor moves. */
    private val cursorDragIcons = ArrayList<android.widget.ImageView>()

    private fun showCursorDragIcons(active: Boolean) {
        cursorDragIcons.forEach { it.alpha = if (active) 1f else 0f }
    }

    /** A choice shown above a carousel item: Bitik's dialects, Latin's minimal / full layouts. */
    private class SubOption(val label: String, val isCurrent: Boolean, val children: List<SubOption> = emptyList(), val apply: () -> Unit)

    private fun subOptionsFor(system: WritingSystem): List<SubOption> {
        val context = MyKeyboardService.context
        return when (system) {
            WritingSystem.Bitik -> {
                // Third floor: every dialect offers the keyboard variants (original / default / modern).
                val variants = listOf(
                    SubOption("Original", context.keyboardVariant == BitikVariant.CLASSIC) { context.keyboardVariant = BitikVariant.CLASSIC },
                    SubOption("Default", context.keyboardVariant == BitikVariant.Modern) { context.keyboardVariant = BitikVariant.Modern },
                    SubOption("Modern", context.keyboardVariant == BitikVariant.SAMAGAN) { context.keyboardVariant = BitikVariant.SAMAGAN },
                )
                listOf(
                    SubOption("Altay", context.bitikDialect == BitikDialect.Altay, variants) { context.bitikDialect = BitikDialect.Altay },
                    SubOption("Orkon", context.bitikDialect == BitikDialect.Orkon, variants) { context.bitikDialect = BitikDialect.Orkon },
                )
            }
            WritingSystem.Latin -> listOf(
                SubOption("Minimal", context.latinVariant == Latin_Variant.minimal) { context.latinVariant = Latin_Variant.minimal },
                SubOption("Full", context.latinVariant == Latin_Variant.full) { context.latinVariant = Latin_Variant.full },
            )
            else -> emptyList()
        }
    }

    /** Applies a sub choice and lands on its keyboard (reloading in place when it is already the active one). */
    private fun applySubOption(system: WritingSystem, option: SubOption, child: SubOption? = null) {
        val context = MyKeyboardService.context
        option.apply()
        child?.apply?.invoke()
        if (system != MyKeyboardService.current_writingSystem) context.switchAlphabetTo(system) else context.reloadKeyboard()
    }

    private fun dismissCarousel() {
        carousel?.dismiss()
        carousel = null
    }

    /**
     * A row with every keyboard and a highlight card under it. The card follows the finger (see [moveTo]), the items it
     * passes over light up, and [snapTo] settles it on one item.
     */
    /** The system blue iOS uses for the picker's card. */
    private const val CARD_BLUE = 0xFF007AFF.toInt()

    /** The picker's card as on iOS: a blue capsule with a soft blue glow. */
    private fun styleCard(card: View, density: Float) {
        card.background = GradientDrawable().apply {
            setColor(CARD_BLUE)
            cornerRadius = 15 * density
        }
        card.elevation = 5 * density
        if (Build.VERSION.SDK_INT >= 28) {
            card.outlineSpotShadowColor = CARD_BLUE
            card.outlineAmbientShadowColor = CARD_BLUE
        }
    }

    private class LanguageCarousel(
        service: InputMethodService, private val anchor: View, private val systems: List<WritingSystem>, start: Int, private val textColor: Int, private val fill: Int, fingerX: Float
    ) {
        private val density = service.resources.displayMetrics.density
        private val hPad = (10 * density).toInt()
        private val windowHeight = (38 * density).toInt()
        private val labels = systems.map { system ->
            TextView(service).apply {
                val name = keyboardLabel(system)
                // A check mark shows which keyboard is in use right now.
                text = if (system == MyKeyboardService.current_writingSystem) "✓ $name" else name
                setTextColor(textColor)
                maxLines = 1
                gravity = Gravity.CENTER
                textSize = 15f
                setTypeface(KeyboardTheme.typefaceFor(service, name), Typeface.BOLD)
            }
        }
        private val widths = ArrayList<Float>()
        val centers = ArrayList<Float>()
        private val totalWidth: Int
        private val card = View(service).apply { styleCard(this, density) }
        private val popup: PopupWindow
        private val frame: FrameLayout
        private var cardCenter = 0f
        private var shownIndex = -1
        private var stripLeft = 0f
        private val yOff: Int

        init {
            val row = LinearLayout(service).apply { orientation = LinearLayout.HORIZONTAL }
            labels.forEach { it.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED) }
            val cell = maxOf((labels.maxOf { it.measuredWidth } + 2 * hPad), (84 * density).toInt())
            var x = 0f
            labels.forEach { label ->
                label.layoutParams = LinearLayout.LayoutParams(cell, ViewGroup.LayoutParams.MATCH_PARENT)
                widths.add(cell.toFloat())
                centers.add(x + cell / 2f)
                x += cell
                row.addView(label)
            }
            totalWidth = x.toInt()
            val inset = (4 * density).toInt()

            frame = FrameLayout(service).apply {
                background = GradientDrawable().apply {
                    setColor(fill and 0x00FFFFFF or 0xEE000000.toInt())
                    cornerRadius = 19 * density
                    setStroke((1 * density).toInt(), textColor and 0x00FFFFFF or 0x1F000000)
                }
                elevation = 12 * density
                clipChildren = false
                addView(card, FrameLayout.LayoutParams(widths[start].toInt(), windowHeight - 2 * inset).apply { topMargin = inset })
                // The card's glow lifts it above its siblings, so the labels sit higher still.
                row.elevation = 6 * density
                addView(row, FrameLayout.LayoutParams(totalWidth, ViewGroup.LayoutParams.MATCH_PARENT))
            }
            val screenWidth = service.resources.displayMetrics.widthPixels
            val host = FrameLayout(service).apply { addView(frame, FrameLayout.LayoutParams(totalWidth, windowHeight)) }
            popup = PopupWindow(host, screenWidth, windowHeight).apply {
                isTouchable = false
                isFocusable = false
            }

            popup.isClippingEnabled = false
            stripLeft = (screenWidth - totalWidth) / 2f
            moveTo(centers[start])

            val location = IntArray(2)
            anchor.getLocationOnScreen(location)
            yOff = -(anchor.height + windowHeight + (4 * density).toInt())
            popup.showAsDropDown(anchor, -location[0], yOff, Gravity.START)
            anchor.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {}
                override fun onViewDetachedFromWindow(v: View) {
                    dismiss()
                    v.removeOnAttachStateChangeListener(this)
                }
            })
            frame.translationX = stripLeft
        }

        fun centerUnder(rawX: Float): Float = rawX - stripLeft

        // --- Floors above the carousel: floor 1 = the item's sub choices, floor 2 = the sub choice's own choices ---
        /** One small strip of choices floating above the carousel; the single picker card visits it while the finger is on its floor. */
        inner class OptionStrip(private val floor: Int) {
            private var popup: PopupWindow? = null
            private var card: View? = null
            private var labels: List<TextView> = emptyList()
            private var left = 0f
            private val cell = (84 * density).toInt()
            var count = 0
                private set

            /** True once the finger is at this floor's height or higher. */
            fun fingerAtOrAbove(rawY: Float): Boolean {
                val location = IntArray(2)
                anchor.getLocationOnScreen(location)
                return rawY < location[1] - floor * (windowHeight + 4 * density)
            }

            private var options: List<SubOption> = emptyList()

            fun show(centerX: Float, options: List<SubOption>) {
                hide()
                this.options = options
                count = options.size
                if (options.isEmpty()) return
                val screenWidth = anchor.resources.displayMetrics.widthPixels
                val width = options.size * cell
                val margin = 4 * density
                left = (centerX - width / 2f).coerceIn(margin, screenWidth - width - margin)
                val inset = (4 * density).toInt()
                val newCard = View(anchor.context).apply {
                    styleCard(this, density)
                    alpha = 0f
                }
                card = newCard
                labels = options.map { option ->
                    TextView(anchor.context).apply {
                        text = if (option.isCurrent) "✓ ${option.label}" else option.label
                        setTextColor(if (option.isCurrent) CARD_BLUE else textColor)
                        textSize = 15f
                        gravity = Gravity.CENTER
                        setTypeface(null, if (option.isCurrent) Typeface.BOLD else Typeface.NORMAL)
                        alpha = if (option.isCurrent) 1f else 0.6f
                    }
                }
                val row = LinearLayout(anchor.context).apply { orientation = LinearLayout.HORIZONTAL }
                labels.forEach { row.addView(it, LinearLayout.LayoutParams(cell, ViewGroup.LayoutParams.MATCH_PARENT)) }
                val box = FrameLayout(anchor.context).apply {
                    background = GradientDrawable().apply {
                        setColor(fill and 0x00FFFFFF or 0xEE000000.toInt())
                        cornerRadius = 19 * density
                        setStroke((1 * density).toInt(), textColor and 0x00FFFFFF or 0x1F000000)
                    }
                    elevation = 12 * density
                    clipChildren = false
                    addView(newCard, FrameLayout.LayoutParams(cell, windowHeight - 2 * inset).apply { topMargin = inset })
                    row.elevation = 6 * density
                    addView(row, FrameLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT))
                }
                val window = PopupWindow(box, width, windowHeight).apply {
                    isTouchable = false
                    isFocusable = false
                    isClippingEnabled = false
                }
                val location = IntArray(2)
                anchor.getLocationOnScreen(location)
                window.showAsDropDown(anchor, (left - location[0]).toInt(), yOff - floor * (windowHeight + (4 * density).toInt()), Gravity.START)
                popup = window
            }

            /** Moves the card onto the choice under the finger and returns its index. */
            fun highlight(rawX: Float): Int {
                val index = ((rawX - left) / cell).toInt().coerceIn(0, count - 1)
                card?.apply {
                    alpha = 1f
                    translationX = (index * cell).toFloat()
                }
                labels.forEachIndexed { i, label ->
                    label.alpha = if (i == index) 1f else if (options.getOrNull(i)?.isCurrent == true) 1f else 0.6f
                    label.setTextColor(if (i == index) Color.WHITE else if (options.getOrNull(i)?.isCurrent == true) CARD_BLUE else textColor)
                }
                return index
            }

            fun clearHighlight() {
                card?.alpha = 0f
            }

            /** Horizontal centre of choice [index] on screen. */
            fun centerOf(index: Int): Float = left + (index + 0.5f) * cell

            fun hide() {
                popup?.let { if (it.isShowing) it.dismiss() }
                popup = null
                card = null
                labels = emptyList()
                count = 0
            }
        }

        val subStrip = OptionStrip(1)
        val thirdStrip = OptionStrip(2)

        private var activeFloor = 0

        /** One picker: the main card hands over to the strips' card as the finger goes up; below the floor it comes back. */
        fun setFloor(value: Int) {
            if (activeFloor == value) return
            activeFloor = value
            card.animate().alpha(if (value == 0) 1f else 0f).setDuration(90).start()
            if (value < 2) thirdStrip.clearHighlight()
            if (value == 2) subStrip.clearHighlight()
            if (value < 1) subStrip.clearHighlight()
        }

        fun showSub(index: Int, options: List<SubOption>) {
            thirdStrip.hide()
            subStrip.show(stripLeft + centers[index], options)
        }

        fun hideSub() {
            thirdStrip.hide()
            subStrip.hide()
        }

        private fun nearest(center: Float): Int = centers.indices.minByOrNull { kotlin.math.abs(centers[it] - center) } ?: 0

        /** Puts the card's centre at [center] (clamped to the row) and returns the item it is closest to. */
        fun moveTo(center: Float): Int {
            cardCenter = center.coerceIn(centers.first(), centers.last())
            frame.translationX = stripLeft
            val index = nearest(cardCenter)

            var width = widths[index]
            val left = centers.indexOfLast { it <= cardCenter }.coerceAtLeast(0)
            if (left < centers.lastIndex) {
                val fraction = ((cardCenter - centers[left]) / (centers[left + 1] - centers[left])).coerceIn(0f, 1f)
                width = widths[left] + (widths[left + 1] - widths[left]) * fraction
            }
            card.layoutParams = (card.layoutParams as FrameLayout.LayoutParams).apply { this.width = width.toInt() }
            card.translationX = cardCenter - width / 2f

            labels.forEachIndexed { i, label ->
                val t = (1f - kotlin.math.abs(cardCenter - centers[i]) / (widths[i] * 0.8f)).coerceIn(0f, 1f)
                val isCurrent = systems[i] == MyKeyboardService.current_writingSystem
                val base = if (isCurrent) CARD_BLUE else textColor
                label.setTextColor(androidx.core.graphics.ColorUtils.blendARGB(base, Color.WHITE, t))
                label.alpha = if (isCurrent) 1f else 0.6f + 0.4f * t
                label.scaleX = 1f + 0.2f * t
                label.scaleY = label.scaleX
            }
            shownIndex = index
            return index
        }

        /** Slides the card onto item [index], then calls [done]. */
        fun snapTo(index: Int, done: () -> Unit) {
            val from = cardCenter
            android.animation.ValueAnimator.ofFloat(from, centers[index]).apply {
                duration = 110
                interpolator = android.view.animation.DecelerateInterpolator()
                addUpdateListener { moveTo(it.animatedValue as Float) }
                addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) = done()
                })
                start()
            }
        }

        fun dismiss() {
            hideSub()
            if (popup.isShowing) popup.dismiss()
        }
    }

    private fun extracted(service: InputMethodService): ExtractedText? = service.currentInputConnection?.getExtractedText(ExtractedTextRequest(), 0)

    private fun currentCursor(service: InputMethodService): Int = extracted(service)?.selectionEnd ?: 0

    private fun setSel(service: InputMethodService, start: Int, end: Int) {
        service.currentInputConnection?.setSelection(start, end)
    }

    /** Enter keeps its tap; dragging sideways from it right away moves the cursor, like the space bar used to. */
    private fun wireEnterCursorDrag(service: InputMethodService, key: View) {
        val density = key.resources.displayMetrics.density
        val stepPx = density * 10
        val slopPx = density * 12
        val tint = KeyboardTheme.getSystemButtonStyle(service).textColor.toColorInt()
        // Added once and only faded in and out, like the space bar's icon, so it can't be lost between drags.
        val dragIcon = android.widget.ImageView(service).apply {
            setImageResource(com.shoktuk.shoktukkeyboard.R.drawable.ic_drag_all)
            setColorFilter(tint)
            alpha = 0f
            layoutParams = FrameLayout.LayoutParams(dpToPx(service, 26), dpToPx(service, 26), Gravity.CENTER)
            tag = KEEP_VISIBLE
        }
        val container = key as? ViewGroup
        container?.addView(dragIcon)
        cursorDragIcons.add(dragIcon)

        fun show(active: Boolean) {
            setLabelsHidden(key, active)
            container?.let { group ->
                for (i in 0 until group.childCount) {
                    val child = group.getChildAt(i)
                    if (child !== dragIcon) child.alpha = if (active) 0f else 1f
                }
                group.bringChildToFront(dragIcon)
            }
            showCursorDragIcons(active)
            key.foreground = if (active) ColorDrawable(tint).apply { alpha = 40 } else null
        }

        var downX = 0f
        var lastStep = 0
        var cursorMode = false

        key.setOnTouchListener { v, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.x
                    lastStep = 0
                    cursorMode = false
                    v.isPressed = true
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!cursorMode && kotlin.math.abs(e.x - downX) > slopPx) {
                        cursorMode = true
                        v.isPressed = false
                        // Steps count from where the drag began, not from the touch-down point.
                        downX = e.x
                        Haptics.longPress(key)
                        show(true)
                    } else if (cursorMode) {
                        val step = ((e.x - downX) / stepPx).toInt()
                        if (step != lastStep) {
                            val delta = step - lastStep
                            lastStep = step
                            val et = extracted(service)
                            if (et != null) {
                                val newPos = (et.selectionEnd + delta).coerceIn(0, (et.text?.length ?: 0))
                                setSel(service, newPos, newPos)
                            }
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.isPressed = false
                    if (cursorMode) show(false)
                    else if (e.actionMasked == MotionEvent.ACTION_UP) v.performClick()
                    cursorMode = false
                    v.parent?.requestDisallowInterceptTouchEvent(false)
                    true
                }

                else -> false
            }
        }
    }

    fun createBottomRow(
        service: InputMethodService, isCaps: Boolean, mode: KeyboardMode, buttonHeight: Int, onModeChange: (KeyboardMode) -> Unit,
    ): LinearLayout {
        var isBitik = MyKeyboardService.current_writingSystem == WritingSystem.Bitik
        var isLeftSide = isBitik || MyKeyboardService.current_writingSystem == WritingSystem.Arab
        var comma = if (isLeftSide) "⹁ " else ", "
        var comma_alt = if (isLeftSide) ", " else "⹁ "
        var dot = if (isBitik) "· " else ". "
        var dot_alt = if (isBitik) ". " else "· "

        val action = service.currentInputEditorInfo.imeOptions and EditorInfo.IME_MASK_ACTION
        val isMultiline = (service.currentInputEditorInfo.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0
        val enterIcon = when {
            action == EditorInfo.IME_ACTION_SEARCH -> KeyboardTheme.ENTER_SEARCH_ICON_FILE
            action == EditorInfo.IME_ACTION_GO -> KeyboardTheme.ENTER_GO_ICON_FILE
            action == EditorInfo.IME_ACTION_NEXT -> KeyboardTheme.ENTER_NEXT_ICON_FILE
            isMultiline -> KeyboardTheme.ENTER_ICON_FILE
            else -> KeyboardTheme.ENTER_ICON_FILE
        }

        val bottomRow = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = MyKeyboardService.buttonMargin
                bottomMargin = MyKeyboardService.buttonMargin
            }
        }

        val targetHeight = (buttonHeight * 0.85f).toInt()

        var returnView = SystemKeyBuilder.systemButton_Text(service, if (mode == KeyboardMode.Main) "⓬😀" else "🅰😀", buttonHeight, onClick = {
            try {
                if (MyKeyboardService.context.vibrations == Vibrations.On) {
                    Haptics.perform(bottomRow, HapticFeedbackConstants.KEYBOARD_TAP)
                }
            } catch (e: Exception) {
                print(e.message)
            }

            if (MyKeyboardService.keyboardMode == KeyboardMode.Main) {
                onModeChange(KeyboardMode.Symbols)
            } else {
                onModeChange(KeyboardMode.Main)
            }
        }, onLongClick = {
            onModeChange(KeyboardMode.Emojis)
        }).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(context, 50).toFloat()
                setColor(KeyboardTheme.getSystemButtonStyle(context).fillColor.toColorInt())
            }

            if (Build.VERSION.SDK_INT >= 21) {
                clipToOutline = true
            }

            layoutParams = LinearLayout.LayoutParams(
                KeyboardTheme.getSystemButtonWidth(context), targetHeight, 0.1f
            ).apply {
                marginStart = dpToPx(context, 4)
                marginEnd = dpToPx(context, 4)
            }
        }

        bottomRow.addView(returnView)

        bottomRow.addView(
            SystemKeyBuilder.systemButton_Text(service, dot, buttonHeight, style = KeyboardTheme.getLetterButtonStyle_Normal(service), onClick = {
                onKeyPressed?.invoke(service.currentInputConnection, dot, true)
            }, onLongClick = {
                onKeyPressed?.invoke(service.currentInputConnection, dot_alt, true)
            })
        )

        val spaceBtn = SystemKeyBuilder.expandableSystemButton_Icon(service, KeyboardTheme.SPACE_ICON_FILE, " ", buttonHeight) as FrameLayout
        val spaceStyle = KeyboardTheme.getLetterButtonStyle_Normal(service)
        val spaceTextColor = spaceStyle.textColor.toColorInt()
        val canSwitchKeyboard = MyKeyboardService.context.availableWritingSystems().size > 1
        val mutedColor = androidx.core.graphics.ColorUtils.setAlphaComponent(spaceTextColor, 110)

        fun arrow(symbol: String) = TextView(service).apply {
            text = symbol
            setTextColor(mutedColor)
            textSize = 24f
            includeFontPadding = false
            alpha = if (canSwitchKeyboard) 1f else 0f
        }

        val leftArrow = arrow("◂")
        val rightArrow = arrow("▸")
        val keyboardName = TextView(service).apply {
            text = keyboardLabel(MyKeyboardService.current_writingSystem)
            typeface = KeyboardTheme.typefaceFor(service, text)
            gravity = Gravity.CENTER
            setTextColor(mutedColor)
            maxLines = 1
            includeFontPadding = false
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(this, 6, 14, 1, TypedValue.COMPLEX_UNIT_SP)
        }

        spaceBtn.removeAllViews()
        spaceBtn.addView(LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(service, 6), 0, dpToPx(service, 6), 0)
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            addView(leftArrow)
            addView(keyboardName)
            addView(rightArrow)
        })
        val dragIcon = android.widget.ImageView(service).apply {
            setImageResource(com.shoktuk.shoktukkeyboard.R.drawable.ic_drag_all)
            setColorFilter(mutedColor)
            alpha = 0f
            tag = KEEP_VISIBLE
            layoutParams = FrameLayout.LayoutParams(dpToPx(service, 26), dpToPx(service, 26), Gravity.CENTER)
        }
        spaceBtn.addView(dragIcon)
        cursorDragIcons.clear()
        cursorDragIcons.add(dragIcon)

        fun showCursorMode(active: Boolean) {
            setLabelsHidden(spaceBtn, active)
            keyboardName.alpha = if (active) 0f else 1f
            leftArrow.alpha = if (active || !canSwitchKeyboard) 0f else 1f
            rightArrow.alpha = leftArrow.alpha
            showCursorDragIcons(active)
            spaceBtn.foreground = if (active) ColorDrawable(spaceTextColor).apply { alpha = 40 } else null
        }

        spaceBtn.setOnTouchListener(object : View.OnTouchListener {
            val handler = Handler(Looper.getMainLooper())
            val stepPx = spaceBtn.resources.displayMetrics.density * 10
            val dragGain = 1f
            var downRawX = 0f
            val slopPx = spaceBtn.resources.displayMetrics.density * 12
            var downX = 0f
            var lastStep = 0
            var cursorMode = false
            var carouselActive = false
            var startIndex = 0
            var selected = 0
            var subIndex = -1
            var thirdIndex = -1
            var view: View? = null

            val enterCursorMode = Runnable {
                cursorMode = true
                view?.let { Haptics.longPress(it) }
                showCursorMode(true)
            }

            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        view = v
                        downX = e.x
                        downRawX = e.rawX
                        lastStep = 0
                        cursorMode = false
                        carouselActive = false
                        handler.postDelayed(enterCursorMode, SPACE_HOLD_MILLIS)
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = e.x - downX
                        if (cursorMode) {
                            val step = (dx / stepPx).toInt()
                            if (step != lastStep) {
                                val delta = step - lastStep
                                lastStep = step
                                val et = extracted(service) ?: return true
                                val newPos = (et.selectionEnd + delta).coerceIn(0, (et.text?.length ?: 0))
                                setSel(service, newPos, newPos)
                            }
                        } else if (canSwitchKeyboard && (carouselActive || kotlin.math.abs(dx) > slopPx)) {
                            val systems = MyKeyboardService.context.availableWritingSystems()
                            if (!carouselActive) {
                                handler.removeCallbacks(enterCursorMode)
                                carouselActive = true
                                startIndex = systems.indexOf(MyKeyboardService.current_writingSystem).coerceAtLeast(0)
                                selected = startIndex
                                subIndex = -1
                                thirdIndex = -1
                                carousel = LanguageCarousel(service, v, systems, startIndex, spaceTextColor, spaceStyle.fillColor.toColorInt(), downRawX)
                                carousel?.showSub(startIndex, subOptionsFor(systems[startIndex]))
                            }
                            carousel?.let { active ->
                                // The finger's height picks the floor: 0 = main carousel, 1 = sub choices, 2 = the sub choice's own choices.
                                val optionsHere = subOptionsFor(systems[selected])
                                val floor = when {
                                    active.subStrip.count > 0 && active.thirdStrip.count > 0 && active.thirdStrip.fingerAtOrAbove(e.rawY) -> 2
                                    active.subStrip.count > 0 && active.subStrip.fingerAtOrAbove(e.rawY) -> 1
                                    else -> 0
                                }
                                active.setFloor(floor)
                                when (floor) {
                                    0 -> {
                                        val index = active.moveTo(active.centerUnder(e.rawX))
                                        if (index != selected) {
                                            selected = index
                                            keyboardName.text = keyboardLabel(systems[index])
                                            Haptics.contextClick(v)
                                            active.showSub(index, subOptionsFor(systems[index]))
                                        }
                                        subIndex = -1
                                        thirdIndex = -1
                                    }
                                    1 -> {
                                        val sub = active.subStrip.highlight(e.rawX)
                                        if (sub != subIndex) {
                                            subIndex = sub
                                            thirdIndex = -1
                                            Haptics.contextClick(v)
                                            active.thirdStrip.show(active.subStrip.centerOf(sub), optionsHere.getOrNull(sub)?.children.orEmpty())
                                        }
                                    }
                                    else -> {
                                        val third = active.thirdStrip.highlight(e.rawX)
                                        if (third != thirdIndex) {
                                            thirdIndex = third
                                            Haptics.contextClick(v)
                                        }
                                    }
                                }
                            }
                        }
                        return true
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        handler.removeCallbacks(enterCursorMode)
                        if (carouselActive) {
                            val active = carousel
                            val target = MyKeyboardService.context.availableWritingSystems().getOrNull(selected)
                            val subChoice = if (e.actionMasked == MotionEvent.ACTION_UP && subIndex >= 0 && target != null) subOptionsFor(target).getOrNull(subIndex) else null
                            val thirdChoice = if (subChoice != null && thirdIndex >= 0) subChoice.children.getOrNull(thirdIndex) else null
                            val apply = e.actionMasked == MotionEvent.ACTION_UP && (subChoice != null || (selected != startIndex && target != null))
                            if (active != null) {
                                active.snapTo(selected) {
                                    dismissCarousel()
                                    if (subChoice != null && target != null) applySubOption(target, subChoice, thirdChoice)
                                    else if (apply && target != null) MyKeyboardService.context.switchAlphabetTo(target)
                                }
                            }
                            if (!apply) keyboardName.text = keyboardLabel(MyKeyboardService.current_writingSystem)
                        } else if (!cursorMode && kotlin.math.abs(e.x - downX) <= slopPx && e.actionMasked == MotionEvent.ACTION_UP) {
                            onKeyPressed?.invoke(service.currentInputConnection, " ", true)
                        }
                        carouselActive = false
                        if (cursorMode) showCursorMode(false)
                        cursorMode = false
                        v.parent?.requestDisallowInterceptTouchEvent(false)
                        return true
                    }
                }
                return false
            }
        })

        bottomRow.addView(spaceBtn)

        if (isBitik && MyKeyboardService.context.wordSeparator != WordSeparator.Off) {
            var wp = MyKeyboardService.context.wordSeparator.id.replace("{", "").replace("}", "").reversed()

            bottomRow.addView(
                SystemKeyBuilder.expandableSystemButton_Text(
                    service, "⁚", buttonHeight, wp, onClick = {
                        onKeyPressed?.invoke(service.currentInputConnection, ":", true)
                    })
            )
        }

        bottomRow.addView(
            SystemKeyBuilder.systemButton_Text(service, comma, buttonHeight, style = KeyboardTheme.getLetterButtonStyle_Normal(service), onClick = {
                onKeyPressed?.invoke(service.currentInputConnection, comma, true)
            }, onLongClick = {
                onKeyPressed?.invoke(service.currentInputConnection, comma_alt, true)
            })
        )

        returnView = SystemKeyBuilder.systemButton_Icon(
            service, if (MyKeyboardService.keyboardMode == KeyboardMode.SavedStrings) KeyboardTheme.DELETE_ICON_FILE else enterIcon, buttonHeight, onClick = {
                if (MyKeyboardService.keyboardMode == KeyboardMode.SavedStrings) {
                    SystemKeyBuilder.performDelete(MyKeyboardService.context)
                } else {
                    val ic = service.currentInputConnection
                    val editorInfo = service.currentInputEditorInfo

                    if (ic != null && editorInfo != null) {
                        val action = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION
                        val isMultiline = (editorInfo.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0

                        when {
                            action == EditorInfo.IME_ACTION_SEARCH || action == EditorInfo.IME_ACTION_GO || action == EditorInfo.IME_ACTION_NEXT -> {
                                ic.performEditorAction(action)
                            }

                            isMultiline -> {
                                onKeyPressed?.invoke(service.currentInputConnection, "\n", true)
                            }

                            else -> {
                                onKeyPressed?.invoke(service.currentInputConnection, "\n", true)
                            }
                        }
                    }
                }
            }).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(context, 50).toFloat()
                setColor(KeyboardTheme.getSystemButtonStyle(context).fillColor.toColorInt())
            }

            if (Build.VERSION.SDK_INT >= 21) {
                clipToOutline = true
            }

            layoutParams = LinearLayout.LayoutParams(
                KeyboardTheme.getSystemButtonWidth(context), targetHeight, 0.1f
            ).apply {
                marginStart = dpToPx(context, 4)
                marginEnd = dpToPx(context, 4)
            }
        }

        if (Build.VERSION.SDK_INT >= 21) returnView.clipToOutline = true
        if (MyKeyboardService.keyboardMode != KeyboardMode.SavedStrings) wireEnterCursorDrag(service, returnView)
        bottomRow.addView(returnView)
        return bottomRow
    }
}
