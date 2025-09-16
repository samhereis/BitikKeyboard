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
        var buttonHeight = (KeyboardTheme.getButtonHeight())
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
            setBackgroundColor(KeyboardTheme.getColor(5).toColorInt()) // use your palette
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

        // --- Emoji sections: horizontal scroll of 4-row grids ---
        val rowsCount = 4
        val gridAreaHeight = buttonHeight * 3 // mirrors SwiftUI .frame(height: KeyboardStyle.rowHeight * 3)
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

        // Keep section left positions so we can jump by tabs
        val sectionLeftX = hashMapOf<Int, Int>()
        var selectedType = tabOrder.firstOrNull() ?: 0

        fun registerRecent(emoji: String) {
            recents.remove(emoji)
            recents.add(0, emoji)
            if (recents.size > RECENTS_LIMIT) {
                while (recents.size > RECENTS_LIMIT) recents.removeAt(recents.lastIndex)
            }
            saveRecents(service, recents)
            rebuildRecents()
        }

        // Build each section (4-row grid packed vertically, repeated horizontally)
        for (type in tabOrder) {
            val cat = emojiByType.firstOrNull { it.type == type } ?: continue
            if (cat.emojis.isEmpty()) continue

            // cell side: divide available height by rowsCount
            val cellSide = max(1, floor(gridAreaHeight.toFloat() / rowsCount.toFloat()).toInt())

            val section = LinearLayout(service).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT
                ).apply {
                    setMargins(dp(service, 2f), 0, dp(service, 2f), 0)
                }
                // Tag by type for later lookup
                tag = "section_type_$type"
            }

            // create a grid with rowsCount rows; we’ll fill columns as needed
            // simplest portable approach: use TableLayout of rows
            val table = TableLayout(service).apply {
                layoutParams = TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                isShrinkAllColumns = false
                isStretchAllColumns = false
            }

            // Split emojis into rows (row-major fill)
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
            // measure after layout to record leftX; we can compute on first layout pass
            section.viewTreeObserver.addOnGlobalLayoutListener {
                val x = section.left
                sectionLeftX[type] = x
            }

            hStack.addView(section)
        }

        root.addView(mainHScroll)

        // --- Bottom bar: ABC | tabs | backspace ---
        val bottom = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, buttonHeight / 2
            )
        }

        // ABC
        bottom.addView(
            SystemKeyBuilder.systemButton_Text(
                service = service, text = "ABC", buttonHeight = buttonHeight / 2, onClick = {
                    onABC()
                }).apply {
                updateLayoutParams<LinearLayout.LayoutParams> {
                    weight = 0f
                }
                scaleX = 1.5f
                scaleY = 1.5f
            })

        // Tabs (chips)
        val tabsScroll = HorizontalScrollView(service).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        }
        val tabsRow = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        tabsScroll.addView(tabsRow)

        fun rebuildTabs() {
            tabsRow.removeAllViews()
            for (type in tabOrder) {
                val label = emojiByType.firstOrNull { it.type == type }?.displayName ?: "?"
                val chip = TextView(service).apply {
                    text = label
                    setTextColor(KeyboardTheme.getColor(1).toColorInt())
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonHeight / 4f)
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    val side = (buttonHeight / 2.5f).toInt()
                    layoutParams = LinearLayout.LayoutParams(side, side).apply {
                        setMargins(dp(service, 4f), dp(service, 2f), dp(service, 4f), dp(service, 2f))
                    }
                    setPadding(0, 0, 0, 0)
                    alpha = if (selectedType == type) 1f else 0.6f
                }
                chip.setOnClickListener {
                    it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    selectedType = type
                    sectionLeftX[type]?.let { left ->
                        mainHScroll.smoothScrollTo(left, 0)
                    }
                    rebuildTabs()
                }
                tabsRow.addView(chip)
            }
        }
        rebuildTabs()
        bottom.addView(tabsScroll)

        bottom.addView(
            SystemKeyBuilder.systemButton_Icon(
                service = service, assetPath = KeyboardTheme.DELETE_ICON_FILE, buttonHeight = buttonHeight / 2, onClick = {
                    onBackspace()
                }).apply {
                updateLayoutParams<LinearLayout.LayoutParams> {
                    weight = 0f
                }
                scaleX = 1.5f
                scaleY = 1.5f
            })

        root.addView(bottom)

        mainHScroll.viewTreeObserver.addOnScrollChangedListener {
            if (sectionLeftX.isNotEmpty()) {
                val scrollX = mainHScroll.scrollX
                var nearestType = selectedType
                var nearestDx = Int.MAX_VALUE
                for ((type, left) in sectionLeftX) {
                    val dx = max(0, left - scrollX)
                    if (dx < nearestDx) {
                        nearestDx = dx
                        nearestType = type
                    }
                }
                if (nearestType != selectedType) {
                    selectedType = nearestType
                    rebuildTabs()
                }
            }
        }

        return root
    }

    private fun dp(ctx: Context, v: Float): Int = (ctx.resources.displayMetrics.density * v).toInt()
}