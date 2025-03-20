import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

@Preview(showBackground = true)
@Composable
fun OriginalTamgasViewPreview() {
    ShoktukKeyboardTheme {
        OriginalTamgasView()
    }
}

@Composable
fun OriginalTamgasView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ConsonantsView()
        VowelsAndCharactersView()
    }
}