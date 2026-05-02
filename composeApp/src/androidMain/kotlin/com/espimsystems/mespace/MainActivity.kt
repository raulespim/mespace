package com.espimsystems.mespace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.espimsystems.mespace.core.database.AndroidDatabaseDriverFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val databaseDriverFactory = remember {
                AndroidDatabaseDriverFactory(
                    context = applicationContext,
                )
            }

            App(
                databaseDriverFactory = databaseDriverFactory,
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    AppPreview()
}
