package com.shoktuk.bitikkeyboard

import android.os.Bundle
import android.widget.EditText
import android.view.inputmethod.EditorInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.shoktuk.bitikkeyboard.ui.theme.BitikKeyboardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BitikKeyboardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Test your custom keyboard below:",
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        // This AndroidView wraps an EditText that will trigger your IME.
                        AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
                            EditText(context).apply {
                                hint = "Enter text here..."
                                inputType = EditorInfo.TYPE_CLASS_TEXT
                            }
                        })
                    }
                }
            }
        }
    }
}
