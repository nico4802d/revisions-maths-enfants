package com.nico.revisionsmaths

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nico.revisionsmaths.ui.nav.AppNavigation
import com.nico.revisionsmaths.ui.theme.RevisionsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RevisionsTheme {
                AppNavigation()
            }
        }
    }
}
