package com.shoktuk.shoktukkeyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import android.view.Gravity
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.shoktuk.shoktukkeyboard.project.systems.JSTranscriber

object TopRowBuilder {
    var onTypedListener: (() -> Unit)? = null

    fun createTopRow(
        service: InputMethodService, layout: KeyboardLayout, buttonHeight: Int, margin: Int, onModeChange: (String) -> Unit, onLangChange: () -> Unit
    ): LinearLayout {
        val jsTranscriber = JSTranscriber(service)
        val rowLayout = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = margin
                bottomMargin = margin
            }
        }

        // Button to change language (or to display language code)
        rowLayout.addView(
            createSystemAssetButton(
                service = service,
                assetPath = null,
                textToSet = layout.languageCode,
                buttonHeight = buttonHeight,
                margin = margin,
                onClick = { onLangChange() },
                buttonStyle = KeyboardTheme.getSystemButtonStyle(service)
            )
        )

        // Create the TextView that will display our two-floor text
        val lastWordTextView = createLastWordTextView(service, buttonHeight, margin)
        rowLayout.addView(lastWordTextView)

        // Set a listener to update the text whenever typing occurs
        onTypedListener = {
            updateLastWord(
                inputConnection = service.currentInputConnection, textView = lastWordTextView, transcriber = jsTranscriber
            )
        }

        // Button to change mode (e.g., switch to symbols)
        rowLayout.addView(
            createSystemAssetButton(
                service = service,
                assetPath = null,
                textToSet = "123",
                buttonHeight = buttonHeight,
                margin = margin,
                onClick = { onModeChange("symbols") },
                buttonStyle = KeyboardTheme.getSystemButtonStyle(service)
            )
        )

        // Initial update of the text in the TextView
        updateLastWord(
            inputConnection = service.currentInputConnection, textView = lastWordTextView, transcriber = jsTranscriber
        )

        return rowLayout
    }

    private fun createSystemAssetButton(
        service: InputMethodService, assetPath: String?, textToSet: String, buttonHeight: Int, margin: Int, onClick: () -> Unit, buttonStyle: ButtonStyle
    ): Button {
        return Button(service).apply {
            text = textToSet
            gravity = Gravity.CENTER
            textSize = buttonStyle.textSizeSp
            setTextColor(buttonStyle.textColor.toColorInt())
            background = KeyboardTheme.createDrawableFromStyle(service, buttonStyle)
            layoutParams = LinearLayout.LayoutParams(
                KeyboardTheme.getSystemButtonWidth(service), buttonHeight
            ).apply {
                marginStart = margin
                marginEnd = margin
            }
            setOnClickListener { onClick() }
        }
    }

    private fun createLastWordTextView(
        service: InputMethodService, buttonHeight: Int, margin: Int
    ): TextView {
        return TextView(service).apply {
            gravity = Gravity.CENTER
            // Use the desired font size of 14sp for a uniform appearance
            textSize = 14f
            // Adjust line spacing to bring the two floors closer together.
            // The extra spacing is reduced (can be negative) to pull the lines nearer.
            setLineSpacing(-4f, 1f)
            setTextColor(KeyboardTheme.getSystemButtonStyle(service).textColor.toColorInt())
            background = KeyboardTheme.createDrawableFromStyle(
                service, KeyboardTheme.getLetterButtonStyle(service)
            )
            layoutParams = LinearLayout.LayoutParams(0, buttonHeight, 1f).apply {
                marginStart = margin
                marginEnd = margin
            }
        }
    }

    private fun updateLastWord(
        inputConnection: InputConnection?,
        textView: TextView,
        transcriber: JSTranscriber
    ) {
        inputConnection?.getTextBeforeCursor(100, 0)?.let { textBeforeCursor ->
            // Extract the last word
            val lastWord = textBeforeCursor.split("[\\s:.,]+".toRegex()).lastOrNull() ?: ""
            // Fallback to original if no transcription is available.
            val transcription = transcriber.getTranscription(lastWord) ?: lastWord

            // Use the same transcription for both floors.
            val baseText = transcription
            val topText = transcription

            // Combine the two floors; newline separates top from bottom.
            val combinedText = "$topText\n$baseText"
            val spannable = SpannableStringBuilder(combinedText)

            // Apply SuperscriptSpan to shift the top line upward.
            spannable.setSpan(
                SuperscriptSpan(),
                0, topText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Use a RelativeSizeSpan of 1.0 to force the top line to be the same size as the bottom.
            spannable.setSpan(
                RelativeSizeSpan(1.0f),
                0, topText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Make every second character (odd indexes) in the top floor transparent.
            for (i in topText.indices) {
                if (i % 2 == 1) {
                    spannable.setSpan(
                        ForegroundColorSpan(0x00000000),
                        i, i + 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            textView.text = spannable
        }
    }
}