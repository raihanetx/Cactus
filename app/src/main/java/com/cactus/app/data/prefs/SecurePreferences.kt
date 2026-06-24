package com.cactus.app.data.prefs
import android.content.Context; import androidx.security.crypto.EncryptedSharedPreferences; import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext; import kotlinx.coroutines.flow.MutableStateFlow; import kotlinx.coroutines.flow.StateFlow; import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject; import javax.inject.Singleton
@Singleton
class SecurePreferences @Inject constructor(@ApplicationContext context: Context) {
    private val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    private val prefs = EncryptedSharedPreferences.create(context, "cactus_secure_prefs", masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    private val _apiKey = MutableStateFlow(prefs.getString(KEY_API_KEY, "") ?: "")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()
    fun setApiKey(value: String) { prefs.edit().putString(KEY_API_KEY, value).apply(); _apiKey.value = value }
    fun isApiKeyValid(): Boolean = _apiKey.value.startsWith("gsk_") && _apiKey.value.length >= 20
    companion object { private const val KEY_API_KEY = "groq_api_key" }
}
