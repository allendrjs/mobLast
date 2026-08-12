package org.rocs.osda.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.rocs.osda.mobile.ui.navigation.OsdaNavHost
import org.rocs.osda.mobile.ui.theme.OsdaMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as OsdaApplication
        setContent {
            OsdaMobileTheme {
                OsdaNavHost(app = app)
            }
        }
    }
}
