package com.cactus.app.data.network
import com.cactus.app.data.AppConfig; import com.cactus.app.data.prefs.SecurePreferences
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module; import dagger.Provides; import dagger.hilt.InstallIn; import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor; import okhttp3.MediaType.Companion.toMediaType; import okhttp3.MultipartBody; import okhttp3.OkHttpClient; import okhttp3.RequestBody.Companion.toRequestBody; import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit; import retrofit2.http.Body; import retrofit2.http.Multipart; import retrofit2.http.POST; import retrofit2.http.Part
import java.io.File; import java.util.concurrent.TimeUnit
import javax.inject.Inject; import javax.inject.Singleton
interface GroqApi {
    @Multipart @POST(AppConfig.GROQ_TRANSCRIPTIONS_PATH) suspend fun transcribe(@Part file: MultipartBody.Part, @Part("model") model: okhttp3.RequestBody, @Part("temperature") temperature: okhttp3.RequestBody, @Part("response_format") responseFormat: okhttp3.RequestBody): WhisperResponse
    @POST(AppConfig.GROQ_CHAT_COMPLETIONS_PATH) suspend fun chatCompletion(@Body request: ChatCompletionRequest): ChatCompletionResponse
}
@Singleton
class GroqApiService @Inject constructor(private val api: GroqApi, private val securePreferences: SecurePreferences) {
    suspend fun transcribeAudio(audioFile: File): WhisperResponse { val mediaType = "audio/m4a".toMediaType(); val filePart = MultipartBody.Part.createFormData("file", audioFile.name, okhttp3.RequestBody.create(mediaType, audioFile)); return api.transcribe(filePart, AppConfig.STT_MODEL.toRequestBody(), AppConfig.STT_TEMPERATURE.toString().toRequestBody(), AppConfig.STT_RESPONSE_FORMAT.toRequestBody()) }
    suspend fun translate(messages: List<ChatMessage>): ChatCompletionResponse = api.chatCompletion(ChatCompletionRequest(model = AppConfig.TRANSLATION_MODEL, messages = messages, temperature = AppConfig.TRANSLATION_TEMPERATURE, maxCompletionTokens = AppConfig.TRANSLATION_MAX_TOKENS, reasoningEffort = AppConfig.TRANSLATION_REASONING_EFFORT))
}
@Module @InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton fun provideJson(): Json = Json { ignoreUnknownKeys = true; coerceInputValues = true; encodeDefaults = true }
    @Provides @Singleton fun provideAuthInterceptor(prefs: SecurePreferences): Interceptor = Interceptor { chain -> chain.proceed(chain.request().newBuilder().apply { prefs.apiKey.value.takeIf { it.isNotEmpty() }?.let { addHeader("Authorization", "Bearer $it") } }.build()) }
    @Provides @Singleton fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient = OkHttpClient.Builder().addInterceptor(authInterceptor).addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }).connectTimeout(30, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS).build()
    @Provides @Singleton fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder().baseUrl(AppConfig.GROQ_BASE_URL).client(client).addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
    @Provides @Singleton fun provideGroqApi(retrofit: Retrofit): GroqApi = retrofit.create(GroqApi::class.java)
}
