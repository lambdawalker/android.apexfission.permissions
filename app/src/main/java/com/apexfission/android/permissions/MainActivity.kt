package com.apexfission.android.permissions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.apexfission.android.permissions.demo.DemoHomeScreen
import com.apexfission.android.permissions.demo.PermissionCarouselDemo
import com.apexfission.android.permissions.demo.PlatformRecipesDemo
import com.apexfission.android.permissions.ui.theme.AndroidpermissionsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var destination by rememberSaveable { mutableStateOf(DemoDestination.Home) }
            BackHandler(destination != DemoDestination.Home) { destination = DemoDestination.Home }
            AndroidpermissionsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val modifier = Modifier.padding(innerPadding)

                    when (destination) {
                        DemoDestination.Home -> DemoHomeScreen(
                            onCarousel = { destination = DemoDestination.Carousel },
                            onRecipes = { destination = DemoDestination.Recipes },
                            modifier = modifier,
                        )
                        DemoDestination.Carousel -> PermissionCarouselDemo(
                            // The library screen handles system bars itself. Scaffold's innerPadding
                            // would add the status-bar inset a second time.
                            onBack = { destination = DemoDestination.Home },
                            modifier = modifier,
                        )
                        DemoDestination.Recipes -> PlatformRecipesDemo(
                            onBack = { destination = DemoDestination.Home }, modifier = modifier,
                        )
                    }
                }
            }
        }
    }
}

private enum class DemoDestination { Home, Carousel, Recipes }
