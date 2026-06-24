package com.cactus.app.ui.settings
import androidx.compose.foundation.background; import androidx.compose.foundation.clickable; import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*; import androidx.compose.foundation.rememberScrollState; import androidx.compose.foundation.shape.RoundedCornerShape; import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons; import androidx.compose.material.icons.filled.ArrowForwardIos; import androidx.compose.material.icons.filled.ChevronLeft; import androidx.compose.material.icons.filled.Info; import androidx.compose.material.icons.filled.Language; import androidx.compose.material.icons.filled.Mic; import androidx.compose.material.icons.filled.Visibility; import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*; import androidx.compose.runtime.*; import androidx.compose.ui.Alignment; import androidx.compose.ui.Modifier; import androidx.compose.ui.draw.clip; import androidx.compose.ui.text.font.FontWeight; import androidx.compose.ui.text.input.PasswordVisualTransformation; import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp; import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel; import androidx.lifecycle.ViewModel; import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cactus.app.data.prefs.SecurePreferences; import com.cactus.app.ui.theme.CactusColors; import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow; import javax.inject.Inject
@HiltViewModel
class SettingsViewModel @Inject constructor(private val securePreferences: SecurePreferences) : ViewModel() { val apiKey: StateFlow<String> = securePreferences.apiKey; fun setApiKey(value: String) = securePreferences.setApiKey(value) }
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val savedApiKey: String by viewModel.apiKey.collectAsStateWithLifecycle(); var apiKey by remember(savedApiKey) { mutableStateOf(savedApiKey) }; var apiKeyVisible by remember { mutableStateOf(false) }
    val isValid: Boolean = apiKey.startsWith("gsk_") && apiKey.length >= 20
    LaunchedEffect(apiKey) { if (apiKey != savedApiKey) { kotlinx.coroutines.delay(1000); viewModel.setApiKey(apiKey) } }
    Column(modifier = Modifier.fillMaxSize().background(CactusColors.Neutral50).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Back", tint = CactusColors.Neutral700, modifier = Modifier.size(28.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onBack))
            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("PREFERENCES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CactusColors.Neutral400, letterSpacing = 1.2.sp); Text("Settings", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CactusColors.Black) }
            Spacer(Modifier.size(28.dp))
        }
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            Text("API CONFIGURATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CactusColors.Neutral500, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp))
            Surface(shape = RoundedCornerShape(16.dp), color = CactusColors.White, border = androidx.compose.foundation.BorderStroke(1.dp, CactusColors.Neutral100)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Groq API Key", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CactusColors.Neutral900); Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, singleLine = true, placeholder = { Text("gsk_...", fontSize = 14.sp, color = CactusColors.Neutral500) }, textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = CactusColors.Neutral900), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CactusColors.Black, unfocusedBorderColor = CactusColors.Neutral200), visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) { Icon(if (apiKeyVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, contentDescription = null, tint = CactusColors.Neutral400, modifier = Modifier.size(18.dp)) } }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text(if (apiKey.isEmpty()) "Get a free API key at console.groq.com" else if (isValid) "API key saved automatically" else "Invalid format - must start with gsk_", fontSize = 11.sp, color = if (isValid) CactusColors.Blue600 else CactusColors.Neutral400)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("AI MODELS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CactusColors.Neutral500, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp))
            Surface(shape = RoundedCornerShape(16.dp), color = CactusColors.White, border = androidx.compose.foundation.BorderStroke(1.dp, CactusColors.Neutral100)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(CactusColors.Neutral100), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Mic, contentDescription = null, tint = CactusColors.Neutral700, modifier = Modifier.size(16.dp)) }; Spacer(Modifier.width(12.dp)); Column(modifier = Modifier.weight(1f)) { Text("Audio to Text Engine", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CactusColors.Neutral900); Text("Whisper Large V3", fontSize = 12.sp, color = CactusColors.Neutral500) }; Icon(Icons.Filled.ArrowForwardIos, contentDescription = null, tint = CactusColors.Neutral300, modifier = Modifier.size(12.dp)) }
                    HorizontalDivider(color = CactusColors.Neutral100)
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(CactusColors.Neutral100), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Language, contentDescription = null, tint = CactusColors.Neutral700, modifier = Modifier.size(16.dp)) }; Spacer(Modifier.width(12.dp)); Column(modifier = Modifier.weight(1f)) { Text("Text Translation & Accent", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CactusColors.Neutral900); Text("openai/gpt-oss-120b | American", fontSize = 12.sp, color = CactusColors.Neutral500) }; Icon(Icons.Filled.ArrowForwardIos, contentDescription = null, tint = CactusColors.Neutral300, modifier = Modifier.size(12.dp)) }
                }
            }
            Spacer(Modifier.height(24.dp))
            Surface(shape = RoundedCornerShape(16.dp), color = CactusColors.White, border = androidx.compose.foundation.BorderStroke(1.dp, CactusColors.Neutral100)) { Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(CactusColors.Neutral100), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Info, contentDescription = null, tint = CactusColors.Neutral700, modifier = Modifier.size(16.dp)) }; Spacer(Modifier.width(12.dp)); Text("About App", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CactusColors.Neutral900, modifier = Modifier.weight(1f)); Icon(Icons.Filled.ArrowForwardIos, contentDescription = null, tint = CactusColors.Neutral300, modifier = Modifier.size(12.dp)) } }
            Spacer(Modifier.height(24.dp))
        }
    }
}
