import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shoktuk.shoktukkeyboard.R
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

@Preview(showBackground = true)
@Composable
fun ModernizedTamgasViewPreview() {
    ShoktukKeyboardTheme {
        ModernizedTamgasView()
    }
}

@Composable
fun ModernizedTamgasView() {
    var context = LocalContext.current

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text("- " + "mb_change_1".localized("loc_modernBitik", context) + ":")
            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text("• NÇ 𐰩 -> U")
            }
        }
        Spacer(modifier = Modifier.height(15.dp))

        Card(
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(25.dp), border = BorderStroke(1.dp, Color.Red)
        ) {
            Column(
                modifier = Modifier
                    .background(Color.Red.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Text(
                    text = "mb_altayVariant".localized("loc_modernBitik", context), fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color.Yellow
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("mb_altayHardTamgas".localized("loc_modernBitik", context), color = Color.Yellow)
                Image(
                    painter = painterResource(id = R.drawable.altay_hard),
                    contentDescription = "mb_altayHardTamgas".localized("loc_modernBitik", context),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("mb_altaySoftTamgas".localized("loc_modernBitik", context), color = Color.Yellow)
                Image(
                    painter = painterResource(id = R.drawable.altay_soft),
                    contentDescription = "mb_altaySoftTamgas".localized("loc_modernBitik", context),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(15.dp))

        Card(
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(25.dp), border = BorderStroke(1.dp, Color.Gray)
        ) {
            Column(
                modifier = Modifier
                    .background(Color.Cyan.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Text(
                    text = "mb_orhonVariant".localized("loc_modernBitik", context), fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("mb_orhonHardTamgas".localized("loc_modernBitik", context), color = Color.White)
                Image(
                    painter = painterResource(id = R.drawable.orhon_hard),
                    contentDescription = "mb_orhonHardTamgas".localized("loc_modernBitik", context),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("mb_orhonSoftTamgas".localized("loc_modernBitik", context), color = Color.White)
                Image(
                    painter = painterResource(id = R.drawable.orhon_soft),
                    contentDescription = "mb_orhonSoftTamgas".localized("loc_modernBitik", context),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
