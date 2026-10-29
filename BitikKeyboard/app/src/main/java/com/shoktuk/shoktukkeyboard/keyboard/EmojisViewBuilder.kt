package com.shoktuk.shoktukkeyboard.emoji

import Haptics
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.keyboard.SystemKeyBuilder
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme.dpToPx
import org.json.JSONArray
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

object EmojisViewBuilder {

    private const val PREFS = "emoji_prefs"
    private const val KEY_RECENTS = "emojiRecents.v1"
    private const val RECENTS_LIMIT = 30

    private fun loadRecents(ctx: Context): MutableList<String> {
        val sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = sp.getString(KEY_RECENTS, null) ?: return mutableListOf()
        return try {
            val arr = JSONArray(raw)
            MutableList(arr.length()) { i -> arr.optString(i) }
        } catch (_: Throwable) {
            mutableListOf()
        }
    }

    private fun saveRecents(ctx: Context, list: List<String>) {
        val sp: SharedPreferences = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        sp.edit().putString(KEY_RECENTS, arr.toString()).apply()
    }

    fun create(
        service: InputMethodService, emojiByType: List<EmojiCategory>, onKeyPress: (String) -> Unit, onABC: () -> Unit, onBackspace: () -> Unit
    ): LinearLayout {

        val buttonHeight = KeyboardTheme.getButtonHeight()
        val tabOrder: IntArray = IntArray(emojiByType.size) { it }

        val root = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(KeyboardTheme.getColor(0).toColorInt())
        }

        root.addView(View(service).apply {
            setBackgroundColor(KeyboardTheme.getColor(5).toColorInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(service, 1f)
            )
        })

        val recents = loadRecents(service)
        val recentsRowHeight = buttonHeight

        val recentsScroll = HorizontalScrollView(service).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, recentsRowHeight
            )
        }
        val recentsContainer = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        recentsScroll.addView(recentsContainer)

        fun rebuildRecents() {
            recentsContainer.removeAllViews()
            if (recents.isEmpty()) {
                recentsContainer.gravity = Gravity.CENTER

                val screenWidth = service.resources.displayMetrics.widthPixels

                val tv = TextView(service).apply {
                    setTextColor(KeyboardTheme.getColor(3).toColorInt())
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonHeight / 3f)
                    text = "𐰁𐰶𐰺𐰶 𐰅𐰢𐰈𐰙𐰄𐰠𐰅𐰼 𐰢𐰃𐰣𐰑𐰁 𐰉𐰆𐰞𐱇"
                    typeface = KeyboardTheme.zamanchakTypeface(service)

                    gravity = Gravity.CENTER
                    textAlignment = View.TEXT_ALIGNMENT_CENTER

                    layoutParams = LinearLayout.LayoutParams(
                        screenWidth,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }

                recentsContainer.addView(tv)
            } else {
                recentsContainer.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                for (e in recents) {
                    val b = TextView(service).apply {
                        text = e
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonHeight / 2f)
                        gravity = Gravity.CENTER
                        setPadding(dp(service, 8f), 0, dp(service, 8f), 0)
                        setTextColor(KeyboardTheme.getColor(1).toColorInt())
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                    b.setOnClickListener {
                        Haptics.perform(it, HapticFeedbackConstants.KEYBOARD_TAP)
                        onKeyPress(e)
                    }
                    recentsContainer.addView(b)
                }
            }
        }

        rebuildRecents()
        root.addView(recentsScroll)

        val rowsCount = 4
        val gridAreaHeight = buttonHeight * 3

        val mainHScroll = HorizontalScrollView(service).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, gridAreaHeight
            )
        }
        val hStack = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        mainHScroll.addView(hStack)

        // Sections in display order. Positions are read from the views when needed, so nothing depends on layout listeners.
        val sectionViews = linkedMapOf<Int, View>()
        val glyphPaint = Paint()
        var selectedType = tabOrder.firstOrNull() ?: 0

        fun registerRecent(emoji: String) {
            recents.remove(emoji)
            recents.add(0, emoji)
            while (recents.size > RECENTS_LIMIT) recents.removeAt(recents.lastIndex)
            saveRecents(service, recents)
            rebuildRecents()
        }

        for (type in tabOrder) {
            val cat = emojiByType.firstOrNull { it.type == type } ?: continue
            // Newer emoji the phone's font can't draw would show as empty boxes, so leave them out.
            val drawable = cat.emojis.filter { glyphPaint.hasGlyph(it) }
            if (drawable.isEmpty()) continue

            val cellSide = max(1, floor(gridAreaHeight.toFloat() / rowsCount.toFloat()).toInt())

            val section = LinearLayout(service).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT
                ).apply { setMargins(dp(service, 2f), 0, dp(service, 2f), 0) }
                tag = "section_type_$type"
            }

            val table = TableLayout(service).apply {
                layoutParams = TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                isShrinkAllColumns = false
                isStretchAllColumns = false
            }

            val rows = Array(rowsCount) { mutableListOf<String>() }
            for ((idx, e) in drawable.withIndex()) {
                rows[idx % rowsCount].add(e)
            }

            for (r in 0 until rowsCount) {
                val tr = TableRow(service).apply {
                    layoutParams = TableRow.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, cellSide
                    )
                    gravity = Gravity.TOP
                }
                for (c in 0 until rows[r].size) {
                    val e = rows[r][c]
                    val btn = TextView(service).apply {
                        text = e
                        gravity = Gravity.CENTER
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, cellSide * 0.5f)
                        layoutParams = TableRow.LayoutParams(cellSide, cellSide)
                    }
                    btn.setOnClickListener {
                        Haptics.perform(it, HapticFeedbackConstants.KEYBOARD_TAP)
                        onKeyPress(e)
                        registerRecent(e)
                    }
                    tr.addView(btn)
                }
                table.addView(tr)
            }

            section.addView(table)

            sectionViews[type] = section
            hStack.addView(section)
        }

        root.addView(mainHScroll)

        val bottom = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, buttonHeight / 2
            )
        }

        bottom.addView(
            SystemKeyBuilder.systemButton_Text(
                service = service, text = "ABC", buttonHeight = buttonHeight, onClick = { onABC() }).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dpToPx(context, 50).toFloat()
                    setColor(KeyboardTheme.getSystemButtonStyle(context).fillColor.toColorInt())
                }

                if (Build.VERSION.SDK_INT >= 21) {
                    clipToOutline = true
                }

                layoutParams = LinearLayout.LayoutParams(
                    KeyboardTheme.getSystemButtonWidth(context), buttonHeight / 2, 0.05f
                ).apply {
                    marginStart = dpToPx(context, 4)
                    marginEnd = dpToPx(context, 4)
                }
            })

        var pickerDownX = 0f
        var pickerDragging = false
        var pickerScrubbing = false
        val visibleTypes = sectionViews.keys.toList()
        val chipSide = (buttonHeight / 2.5f).toInt()
        val chips = linkedMapOf<Int, TextView>()

        val picker = FrameLayout(service).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            isHapticFeedbackEnabled = true
        }
        val pill = View(service).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(service, 50).toFloat()
                // Same as iOS: the system blue at 18%.
                setColor(0x2E007AFF)
            }
            layoutParams = FrameLayout.LayoutParams(0, buttonHeight / 2, Gravity.CENTER_VERTICAL)
        }
        picker.addView(pill)

        val chipRow = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        for (type in visibleTypes) {
            val label = emojiByType.firstOrNull { it.type == type }?.displayName ?: "?"
            chipRow.addView(TextView(service).apply {
                text = label
                setTextColor(KeyboardTheme.getColor(1).toColorInt())
                setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonHeight / 4f)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, chipSide, 1f)
            }.also { chips[type] = it })
        }
        picker.addView(chipRow)

        fun cellWidth(): Float = if (visibleTypes.isEmpty()) 0f else picker.width.toFloat() / visibleTypes.size

        /** Moves the highlight to [type]. Only touches the existing views, nothing is rebuilt. */
        fun select(type: Int, animate: Boolean = true) {
            selectedType = type
            val index = visibleTypes.indexOf(type)
            chips.forEach { (t, chip) ->
                val on = t == type
                // iOS tints the selected symbol blue; emoji can't be tinted, so the others fade instead.
                chip.animate().alpha(if (on) 1f else 0.55f).scaleX(if (on) 1.12f else 1f).scaleY(if (on) 1.12f else 1f).setDuration(150).start()
            }
            if (index < 0) return
            val x = cellWidth() * index
            if (animate) pill.animate().translationX(x).setDuration(280).setInterpolator(OvershootInterpolator(0.8f)).start() else pill.translationX = x
        }

        picker.addOnLayoutChangeListener { _, l, _, r, _, _, _, _, _ ->
            val w = cellWidth()
            if (r - l > 0 && w > 0f) {
                pill.layoutParams = (pill.layoutParams as FrameLayout.LayoutParams).apply { width = w.toInt() }
                select(selectedType, animate = false)
            }
        }

        fun scrollToSection(type: Int, smooth: Boolean) {
            val left = sectionViews[type]?.left ?: return
            if (smooth) mainHScroll.smoothScrollTo(left, 0) else mainHScroll.scrollTo(left, 0)
        }

        val touchSlop = ViewConfiguration.get(service).scaledTouchSlop

        /** The finger's place across the whole screen sets the grid's scroll position, like a scrollbar handle. */
        fun scrub(rawX: Float) {
            val screenW = service.resources.displayMetrics.widthPixels.toFloat()
            val margin = dp(service, 12f)
            val fraction = ((rawX - margin) / (screenW - margin * 2)).coerceIn(0f, 1f)
            val maxScroll = (hStack.width - mainHScroll.width).coerceAtLeast(0)
            mainHScroll.scrollTo((fraction * maxScroll).toInt(), 0)
        }

        // ABC and delete slide away while scrubbing so the picker can use the whole width: the icons keep their size and spread out.
        fun setScrubbing(on: Boolean) {
            if (pickerScrubbing == on) return
            pickerScrubbing = on
            TransitionManager.beginDelayedTransition(bottom, AutoTransition().setDuration(180))
            bottom.getChildAt(0).visibility = if (on) View.GONE else View.VISIBLE
            bottom.getChildAt(bottom.childCount - 1).visibility = if (on) View.GONE else View.VISIBLE
            // The pill snaps between categories, so it is hidden while the finger drags.
            pill.animate().alpha(if (on) 0f else 1f).setDuration(120).start()
        }

        picker.setOnTouchListener { v, ev ->
            val w = cellWidth()
            if (w <= 0f || visibleTypes.isEmpty()) return@setOnTouchListener false
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                    pickerDownX = ev.rawX
                    pickerDragging = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (abs(ev.rawX - pickerDownX) > touchSlop) {
                        pickerDragging = true
                        setScrubbing(true)
                    }
                    if (pickerDragging) scrub(ev.rawX)
                }
                MotionEvent.ACTION_UP -> {
                    if (pickerDragging) {
                        scrub(ev.rawX)  // the release point counts too, so a drag to the end reaches the end
                    } else {
                        val type = visibleTypes[(ev.x / w).toInt().coerceIn(0, visibleTypes.size - 1)]
                        Haptics.perform(v, HapticFeedbackConstants.KEYBOARD_TAP)
                        scrollToSection(type, smooth = true)
                    }
                    pickerDragging = false
                    setScrubbing(false)
                }
                MotionEvent.ACTION_CANCEL -> {
                    pickerDragging = false
                    setScrubbing(false)
                }
            }
            true
        }

        bottom.addView(picker)

        bottom.addView(
            SystemKeyBuilder.systemButton_Icon(
                service = service, assetPath = KeyboardTheme.DELETE_ICON_FILE, buttonHeight = buttonHeight, onClick = { onBackspace() }).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dpToPx(context, 50).toFloat()
                    setColor(KeyboardTheme.getSystemButtonStyle(context).fillColor.toColorInt())
                }

                if (Build.VERSION.SDK_INT >= 21) {
                    clipToOutline = true
                }

                layoutParams = LinearLayout.LayoutParams(
                    KeyboardTheme.getSystemButtonWidth(context), buttonHeight / 2, 0.05f
                ).apply {
                    marginStart = dpToPx(context, 4)
                    marginEnd = dpToPx(context, 4)
                }
            })

        // Small to look at, big to hit: no pill behind them, but the touch area stays as it was.
        bottom.getChildAt(0).background = null
        bottom.getChildAt(bottom.childCount - 1).background = null

        root.addView(bottom)

        // The highlight always shows the section at the grid's leading edge, whoever is moving the grid.
        mainHScroll.setOnScrollChangeListener { _, scrollX, _, _, _ ->
            if (sectionViews.isNotEmpty()) {
                val leading = sectionViews.entries.lastOrNull { it.value.left <= scrollX + 1 }?.key ?: sectionViews.keys.first()
                if (leading != selectedType) {
                    if (pickerDragging) Haptics.perform(picker, HapticFeedbackConstants.KEYBOARD_TAP)
                    select(leading)
                }
            }
        }

        select(selectedType, animate = false)

        return root
    }

    private fun dp(ctx: Context, v: Float): Int = (ctx.resources.displayMetrics.density * v).toInt()
}