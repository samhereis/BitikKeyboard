package com.shoktuk.shoktukkeyboard.emoji

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.view.updateLayoutParams
import com.shoktuk.shoktukkeyboard.keyboard.SystemKeyBuilder
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme
import org.json.JSONArray
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

object EmojisViewBuilder {

    // region Persistence (Recents)
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
    // endregion

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

        // --- Divider (top) ---
        root.addView(View(service).apply {
            setBackgroundColor(KeyboardTheme.getColor(5).toColorInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(service, 1f)
            )
        })

        // --- Recents row ---
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
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        recentsScroll.addView(recentsContainer)

        fun rebuildRecents() {
            recentsContainer.removeAllViews()
            if (recents.isEmpty()) {
                val tv = TextView(service).apply {
                    setTextColor(KeyboardTheme.getColor(5).toColorInt())
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonHeight / 2f)
                    text = "𐰁𐰶𐰺𐰶 𐰅𐰢𐰈𐰙𐰄𐰠𐰅𐰼 𐰢𐰃𐰣𐰑𐰁 𐰉𐰆𐰞𐱇"
                    gravity = Gravity.CENTER_VERTICAL
                }
                recentsContainer.addView(tv)
            } else {
                for (e in recents) {
                    val b = TextView(service).apply {
                        text = e
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonHeight / 2f)
                        gravity = Gravity.CENTER
                        setPadding(dp(service, 8f), 0, dp(service, 8f), 0)
                        setTextColor(KeyboardTheme.getColor(1).toColorInt())
                    }
                    b.setOnClickListener {
                        it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
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

        val sectionLeftX = hashMapOf<Int, Int>()
        val sectionCenterX = hashMapOf<Int, Int>()

        var selectedType = tabOrder.firstOrNull() ?: 0

// Flicker control
        var isProgrammaticScroll = false
        var targetScrollX: Int? = null

        fun registerRecent(emoji: String) {
            recents.remove(emoji)
            recents.add(0, emoji)
            if (recents.size > RECENTS_LIMIT) {
                while (recents.size > RECENTS_LIMIT) recents.removeAt(recents.lastIndex)
            }
            saveRecents(service, recents)
            rebuildRecents()
        }

        for (type in tabOrder) {
            val cat = emojiByType.firstOrNull { it.type == type } ?: continue
            if (cat.emojis.isEmpty()) continue

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

            val perCol = ceil(cat.emojis.size / rowsCount.toFloat()).toInt().coerceAtLeast(1)
            val rows = Array(rowsCount) { mutableListOf<String>() }
            for ((idx, e) in cat.emojis.withIndex()) {
                val r = idx % rowsCount
                rows[r].add(e)
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
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, cellSide * 0.82f)
                        layoutParams = TableRow.LayoutParams(cellSide, cellSide)
                    }
                    btn.setOnClickListener {
                        it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onKeyPress(e)
                        registerRecent(e)
                    }
                    tr.addView(btn)
                }
                table.addView(tr)
            }

            section.addView(table)

            section.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    section.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val left = section.left
                    val center = left + section.width / 2
                    sectionLeftX[type] = left
                    sectionCenterX[type] = center
                }
            })

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
            updateLayoutParams<LinearLayout.LayoutParams> { weight = 0f }
        })

        // Tabs (chips)
        val tabsScroll = HorizontalScrollView(service).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        }
        val tabsRow = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        tabsScroll.addView(tabsRow)

        fun scrollSectionToCenter(type: Int) {
            // If we know the exact center → align it to viewport center
            val knownCenter = sectionCenterX[type]
            val viewportW = mainHScroll.width
            if (knownCenter != null && viewportW > 0) {
                val target = (knownCenter - viewportW / 2).coerceAtLeast(0)
                mainHScroll.smoothScrollTo(target, 0)
                return
            }
            // Fallback: use left edge
            val left = sectionLeftX[type] ?: return
            mainHScroll.smoothScrollTo(left, 0)
        }

        fun rebuildTabs() {
            tabsRow.removeAllViews()
            for (type in tabOrder) {
                val label = emojiByType.firstOrNull { it.type == type }?.displayName ?: "?"
                val isSelected = (selectedType == type)
                val chip = TextView(service).apply {
                    text = label
                    setTextColor(KeyboardTheme.getColor(1).toColorInt())
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonHeight / 4f)
                    typeface = if (isSelected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                    gravity = Gravity.CENTER
                    val side = (buttonHeight / 2.5f).toInt()
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, side
                    ).apply {
                        setMargins(dp(service, 6f), dp(service, 2f), dp(service, 6f), dp(service, 2f))
                    }
                    setPadding(dp(service, 10f), 0, dp(service, 10f), 0)
                    alpha = if (isSelected) 1f else 0.6f
                }
                chip.setOnClickListener {
                    it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    selectedType = type
                    scrollSectionToCenter(type)   // ← center on tap
                    rebuildTabs()
                }
                tabsRow.addView(chip)
            }
            // center chips in the strip when content doesn't overflow
            tabsRow.gravity = Gravity.CENTER
        }
        rebuildTabs()
        bottom.addView(tabsScroll)

        bottom.addView(
            SystemKeyBuilder.systemButton_Icon(
            service = service, assetPath = KeyboardTheme.DELETE_ICON_FILE, buttonHeight = buttonHeight, onClick = {
                onBackspace()
            }).apply {
            updateLayoutParams<LinearLayout.LayoutParams> { weight = 0f }
        })

        root.addView(bottom)

        // --- Track current section using center proximity for accurate active tab
        mainHScroll.viewTreeObserver.addOnScrollChangedListener {
            if (sectionCenterX.isNotEmpty() && mainHScroll.width > 0) {
                val viewportCenter = mainHScroll.scrollX + mainHScroll.width / 2
                var nearestType = selectedType
                var best = Int.MAX_VALUE
                for ((type, center) in sectionCenterX) {
                    val d = abs(center - viewportCenter)
                    if (d < best) {
                        best = d
                        nearestType = type
                    }
                }
                if (nearestType != selectedType) {
                    selectedType = nearestType
                    rebuildTabs()
                }
            }
        }

        // Center initial section once layout is ready
        mainHScroll.post {
            scrollSectionToCenter(selectedType)
            rebuildTabs()
        }

        fun updateTabStyles() {
            for (type in tabOrder) {
                val chip = tabsRow.findViewWithTag<TextView>("tab_$type") ?: continue
                val isSelected = (type == selectedType)
                chip.typeface = if (isSelected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                chip.alpha = if (isSelected) 1f else 0.6f
                chip.setTextColor(KeyboardTheme.getColor(1).toColorInt())
            }
        }

        fun buildTabsOnce() {
            tabsRow.removeAllViews()
            for (type in tabOrder) {
                val label = emojiByType.firstOrNull { it.type == type }?.displayName ?: "?"
                val chip = TextView(service).apply {
                    text = label
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonHeight / 4f)
                    gravity = Gravity.CENTER
                    val side = (buttonHeight / 2.5f).toInt()
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, side
                    ).apply {
                        setMargins(dp(service, 6f), dp(service, 2f), dp(service, 6f), dp(service, 2f))
                    }
                    setPadding(dp(service, 10f), 0, dp(service, 10f), 0)
                    tag = "tab_$type"
                    isHapticFeedbackEnabled = true
                }
                chip.setOnClickListener {
                    it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    if (selectedType != type) {
                        selectedType = type
                        updateTabStyles()
                        scrollSectionToCenter(type) // programmatic scroll
                    } else {
                        // still center if user taps the already-selected tab
                        scrollSectionToCenter(type)
                    }
                }
                tabsRow.addView(chip)
            }
            tabsRow.gravity = Gravity.CENTER
            updateTabStyles()
        }

        return root
    }

    private fun dp(ctx: Context, v: Float): Int = (ctx.resources.displayMetrics.density * v).toInt()
}