package com.cactus.app.data
object AppConfig {
    const val STT_MODEL = "whisper-large-v3"; const val STT_TEMPERATURE = 0.0f; const val STT_RESPONSE_FORMAT = "verbose_json"
    const val TRANSLATION_MODEL = "openai/gpt-oss-120b"; const val TRANSLATION_TEMPERATURE = 0.2f; const val TRANSLATION_MAX_TOKENS = 8192
    const val TRANSLATION_REASONING_EFFORT = "medium"; const val TRANSLATION_BATCH_SIZE = 40
    const val GROQ_BASE_URL = "https://api.groq.com/openai/v1/"; const val GROQ_TRANSCRIPTIONS_PATH = "audio/transcriptions"; const val GROQ_CHAT_COMPLETIONS_PATH = "chat/completions"
    const val SKIP_AMOUNT_MS = 15_000L
}
