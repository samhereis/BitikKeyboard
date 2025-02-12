package com.shoktuk.bitikkeyboard

import android.os.Bundle
import android.provider.CalendarContract.Colors
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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.shoktuk.bitikkeyboard.ui.theme.BitikKeyboardTheme

class MainActivity : ComponentActivity()
{
   override fun onCreate(savedInstanceState: Bundle?)
   {
      super.onCreate(savedInstanceState)
      enableEdgeToEdge()
      setContent {
         BitikKeyboardTheme {

            MyScreen()
         }
      }
   }
}

@Composable
fun MyScreen()
{
   var text by remember { mutableStateOf("") }

   Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
      Column(modifier = Modifier
         .padding(innerPadding)
         .padding(16.dp)) {
         Text(text = "Test your custom keyboard below:", modifier = Modifier.padding(bottom = 8.dp))
         TextField(onValueChange = { value -> text = value }, value = text, modifier = Modifier.fillMaxSize())
      }
   }
}