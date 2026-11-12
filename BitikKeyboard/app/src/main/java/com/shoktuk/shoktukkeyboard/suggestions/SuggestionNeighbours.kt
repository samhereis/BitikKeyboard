package com.shoktuk.shoktukkeyboard.suggestions

import com.shoktuk.shoktukkeyboard.keyboard.KeyboardLayout

/** Which letters sit next to each other on the current layout (the slips of the finger the engine forgives). */
object SuggestionNeighbours {
    @Volatile
    var pairs: Set<Long> = emptySet()
        private set

    fun update(layout: KeyboardLayout) {
        val rows = layout.rows.map { r -> r.filter { it.name != "Shift" && it.name != "Del" && it.name != "kg" && it.name != "kgKey" && it.lowercase.length == 1 } }
        val widest = (rows.maxOfOrNull { it.size } ?: 0).toFloat()
        if (widest == 0f) { pairs = emptySet(); return }
        class P(val c: Char, val x: Float, val y: Float)
        val points = ArrayList<P>()
        for ((ri, row) in rows.withIndex()) for ((ci, key) in row.withIndex()) {
            points.add(P(key.lowercase[0], ci + 0.5f + (widest - row.size) / 2f, ri * 1.15f))
        }
        val out = HashSet<Long>()
        for (a in points) for (b in points) {
            if (a.c == b.c) continue
            val dx = a.x - b.x
            val dy = a.y - b.y
            if (dx * dx + dy * dy < 2.2f * 2.2f) out.add(WordSuggestionEngine.pair(a.c, b.c))
        }
        pairs = out
    }
}
