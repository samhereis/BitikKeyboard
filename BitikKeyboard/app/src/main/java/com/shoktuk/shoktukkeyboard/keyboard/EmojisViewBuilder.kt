package com.shoktuk.shoktukkeyboard.emoji

import Haptics
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.Build
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

                    gravity = Gravity.CENTER
                    textAlignment = View.TEXT_ALIGNMENT_CENTER

                    layoutParams = LinearLayout.LayoutParams(
                        screenWidth,  // force width = screen width
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

        val sectionLeftX = hashMapOf<Int, Int>()
        val sectionCenterX = hashMapOf<Int, Int>()
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
            val knownCenter = sectionCenterX[type]
            val viewportW = mainHScroll.width
            if (knownCenter != null && viewportW > 0) {
                val target = (knownCenter - viewportW / 2).coerceAtLeast(0)
                mainHScroll.smoothScrollTo(target, 0)
                return
            }
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
                    alpha = if (isSelected) 1f else 0.25f
                    tag = "tab_$type"
                    isHapticFeedbackEnabled = true
                }
                chip.setOnClickListener {
                    Haptics.perform(it, HapticFeedbackConstants.KEYBOARD_TAP)
                    selectedType = type
                    scrollSectionToCenter(type)
                    rebuildTabs()
                }
                tabsRow.addView(chip)
            }
            tabsRow.gravity = Gravity.CENTER
        }

        rebuildTabs()
        bottom.addView(tabsScroll)

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

        root.addView(bottom)

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

        mainHScroll.post {
            scrollSectionToCenter(selectedType)
            rebuildTabs()
        }

        return root
    }

    private fun dp(ctx: Context, v: Float): Int = (ctx.resources.displayMetrics.density * v).toInt()
}