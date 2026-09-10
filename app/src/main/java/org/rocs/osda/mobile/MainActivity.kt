package org.rocs.osda.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.rocs.osda.mobile.ui.navigation.OsdaNavHost
import org.rocs.osda.mobile.ui.theme.OsdaMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as OsdaApplication
        setContent {
            // App-only preference (set via the Appearance switch on
            // Profile) -- not the device's system dark-mode setting.
            val darkMode by app.themePreferences.darkModeFlow.collectAsState(initial = false)
            OsdaMobileTheme(darkTheme = darkMode) {
                OsdaNavHost(app = app)
            }
        }
    }
}
