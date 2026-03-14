import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.R
import kotlinx.coroutines.launch

object LetterPrefs {
    private const val KEY = "LetterMemorizeCurrentIndex"

    fun save(context: Context, index: Int) {
        context.getSharedPreferences("letter_memorize", Context.MODE_PRIVATE).edit().putInt(KEY, index).apply()
    }

    fun load(context: Context): Int {
        return context.getSharedPreferences("letter_memorize", Context.MODE_PRIVATE).getInt(KEY, 0)
    }
}

@Composable
fun LetterMemorizeScreenCompose() {
    val context = LocalContext.current

    val imageResIds = listOf(
        R.drawable.lm_1,
        R.drawable.lm_2,
        R.drawable.lm_3,
        R.drawable.lm_4,
        R.drawable.lm_5,
        R.drawable.lm_6,
        R.drawable.lm_7,
        R.drawable.lm_8,
        R.drawable.lm_9,
        R.drawable.lm_10,
        R.drawable.lm_11,
        R.drawable.lm_12,
        R.drawable.lm_13,
        R.drawable.lm_14,
        R.drawable.lm_15,
    )

    val savedIndex = remember { LetterPrefs.load(context) }

    val pagerState = rememberPagerState(
        initialPage = savedIndex, pageCount = { imageResIds.size })

    val scope = rememberCoroutineScope()

    // Save index on page change
    LaunchedEffect(pagerState.currentPage) {
        LetterPrefs.save(context, pagerState.currentPage)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState, modifier = Modifier.fillMaxSize()
        ) { page ->
            Image(
                painter = painterResource(imageResIds[page]), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                enabled = pagerState.currentPage > 0, onClick = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Prev")
            }

            IconButton(
                enabled = pagerState.currentPage < imageResIds.size - 1, onClick = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next")
            }
        }
    }
}