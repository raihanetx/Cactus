package com.cactus.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cactus.app.service.PlayerHolder
import com.cactus.app.ui.home.HomeScreen
import com.cactus.app.ui.navigation.Route
import com.cactus.app.ui.player.LocalPlayerHolder
import com.cactus.app.ui.player.PlayerShell
import com.cactus.app.ui.settings.SettingsScreen
import com.cactus.app.ui.theme.CactusTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var playerHolder: PlayerHolder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { CactusTheme { CompositionLocalProvider(LocalPlayerHolder provides playerHolder) { Surface(modifier = Modifier.fillMaxSize()) { AppNavGraph() } } } }
    }
}
@Composable
private fun AppNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Route.Home.path) {
        composable(Route.Home.path) { HomeScreen(onVideoClick = { video -> VideoUriHolder.uriByVideoId[video.id] = android.net.Uri.parse("content://media/external/video/media/${video.mediaStoreId}"); VideoUriHolder.titleByVideoId[video.id] = video.displayName; VideoUriHolder.pathByVideoId[video.id] = video.filePath; navController.navigate(Route.Player.build(video.id)) }, onSettingsClick = { navController.navigate(Route.Settings.path) }) }
        composable(Route.Player.path) { backStackEntry -> val videoId = backStackEntry.arguments?.getString("videoId")?.toLongOrNull(); val videoUri = videoId?.let { VideoUriHolder.uriByVideoId[it] }; val videoTitle = videoId?.let { VideoUriHolder.titleByVideoId[it] } ?: "Cactus"; PlayerShell(videoId = videoId, videoTitle = videoTitle, videoUri = videoUri, onDismiss = { navController.popBackStack() }) }
        composable(Route.Settings.path) { SettingsScreen(onBack = { navController.popBackStack() }) }
    }
}
object VideoUriHolder { val uriByVideoId = mutableMapOf<Long, android.net.Uri>(); val titleByVideoId = mutableMapOf<Long, String>(); val pathByVideoId = mutableMapOf<Long, String>() }
