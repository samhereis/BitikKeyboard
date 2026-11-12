package com.shoktuk.shoktukkeyboard.keyboard

import android.view.View
import java.util.WeakHashMap

class OnTypedListener {
    private val listeners = WeakHashMap<View, () -> Unit>()

    fun addOnTypedListener(view: View, listener: () -> Unit) {
        listeners[view] = listener
    }

    fun removeOnTypedListener(view: View) {
        listeners.remove(view)
    }

    fun removeAll() {
        listeners.clear()
    }

    fun invoke() {
        cleanupNulls()
        listeners.values.forEach { it.invoke() }
    }

    fun cleanupNulls() {
        val it = listeners.entries.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if (e.key == null || e.value == null) it.remove()
        }
    }
}
