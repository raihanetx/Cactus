package com.cactus.app.service
import android.app.*
import android.content.Intent; import android.content.pm.ServiceInfo
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat; import android.support.v4.media.session.MediaSessionCompat; import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat; import androidx.core.app.ServiceCompat
import androidx.media3.session.MediaSession
import android.graphics.Color; import android.widget.RemoteViews
import com.cactus.app.MainActivity; import com.cactus.app.R; import com.cactus.app.util.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
@AndroidEntryPoint
class PlaybackService : Service() {
    @Inject lateinit var playerHolder: PlayerHolder
    private var mediaSession: MediaSession? = null
    private var mediaSessionCompat: MediaSessionCompat? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var notifPollJob: Job? = null
    override fun onCreate() {
        super.onCreate(); createNotificationChannel()
        try { mediaSession = MediaSession.Builder(this, playerHolder.exoPlayerForSession()).build(); mediaSessionCompat = MediaSessionCompat(this, "CactusPlayback").apply { setCallback(object : MediaSessionCompat.Callback() { override fun onPlay() { playerHolder.togglePlayPause() }; override fun onPause() { playerHolder.togglePlayPause() }; override fun onSeekTo(pos: Long) { playerHolder.seekTo(pos) } }); isActive = true } } catch (_: Exception) {}
        startNotifPolling()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try { ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK) } catch (e: Exception) { stopSelf(); return START_NOT_STICKY }
        intent?.action?.let { action ->
            when (action) {
                ACTION_TOGGLE, ACTION_SEEK_BACK, ACTION_SEEK_FORWARD, ACTION_STOP -> { playerHolder.handleAction(action); updateNotification() }
                ACTION_SEEK_TO -> { intent.getLongExtra(EXTRA_SEEK_POSITION, 0L).let { playerHolder.seekTo(it); updateNotification() } }
            }
        }
        return START_NOT_STICKY
    }
    private fun startNotifPolling() { notifPollJob?.cancel(); notifPollJob = scope.launch { while (true) { delay(500); updateMediaSession(); updateNotification() } } }
    private fun updateMediaSession() {
        val s = playerHolder.state.value; val session = mediaSessionCompat ?: return
        val stateBuilder = PlaybackStateCompat.Builder().setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_SEEK_TO).setState(if (s.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED, s.currentPositionMs, if (s.isPlaying) 1.0f else 0f, SystemClock.elapsedRealtime())
        session.setPlaybackState(stateBuilder.build())
        val metadata = MediaMetadataCompat.Builder().putString(MediaMetadataCompat.METADATA_KEY_TITLE, s.title.ifBlank { "Word Forward" }).putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Word Forward").putLong(MediaMetadataCompat.METADATA_KEY_DURATION, s.durationMs).build()
        session.setMetadata(metadata)
    }
    private fun buildNotification(): Notification {
        val s = playerHolder.state.value
        val contentIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val togglePi = PendingIntent.getService(this, 1, Intent(this, PlaybackService::class.java).setAction(ACTION_TOGGLE), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val seekBackPi = PendingIntent.getService(this, 2, Intent(this, PlaybackService::class.java).apply { action = ACTION_SEEK_BACK }, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val seekForwardPi = PendingIntent.getService(this, 3, Intent(this, PlaybackService::class.java).apply { action = ACTION_SEEK_FORWARD }, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val stopPi = PendingIntent.getService(this, 4, Intent(this, PlaybackService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        fun RemoteViews.populate() {
            setInt(R.id.notification_root_collapsed, "setBackgroundColor", Color.WHITE)
            setTextViewText(R.id.track_title, s.title.ifBlank { "Word Forward" })
            setTextViewText(R.id.time_current, TimeUtils.formatDurationShort(s.currentPositionMs))
            setTextViewText(R.id.time_duration, TimeUtils.formatDurationShort(s.durationMs))
            val fraction = if (s.durationMs > 0) (s.currentPositionMs.toFloat() / s.durationMs.toFloat()).coerceIn(0f, 1f) else 0f
            setProgressBar(R.id.progress_bar, 1000, (fraction * 1000).toInt(), false)
            setTextViewText(R.id.btn_play_pause_text, if (s.isPlaying) "Pause" else "Play")
            setOnClickPendingIntent(R.id.btn_play_pause, togglePi)
            setOnClickPendingIntent(R.id.btn_backward, seekBackPi)
            setOnClickPendingIntent(R.id.btn_forward, seekForwardPi)
            val zoneFractions = listOf(0.10f, 0.30f, 0.50f, 0.70f, 0.90f)
            val zoneIds = listOf(R.id.zone_1, R.id.zone_2, R.id.zone_3, R.id.zone_4, R.id.zone_5)
            zoneFractions.forEachIndexed { i, f ->
                val pos = (s.durationMs * f).toLong()
                val pi = PendingIntent.getService(this@PlaybackService, 10 + i,
                    Intent(this@PlaybackService, PlaybackService::class.java).apply {
                        action = ACTION_SEEK_TO; putExtra(EXTRA_SEEK_POSITION, pos)
                    }, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                setOnClickPendingIntent(zoneIds[i], pi)
            }
        }
        val notifView = RemoteViews(packageName, R.layout.notification_glass_collapsed).apply { populate(); setOnClickPendingIntent(R.id.notification_root_collapsed, contentIntent) }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(s.title.ifBlank { "Word Forward" })
            .setContentIntent(contentIntent).setOngoing(s.isPlaying).setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS).setDeleteIntent(stopPi)
            .setCustomContentView(notifView)
            .build()
    }
    private fun updateNotification() { (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIFICATION_ID, buildNotification()) }
    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Word Forward", NotificationManager.IMPORTANCE_LOW).apply { setShowBadge(false); enableLights(false); enableVibration(false); lockscreenVisibility = Notification.VISIBILITY_PRIVATE }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }
    override fun onTaskRemoved(rootIntent: Intent?) { playerHolder.release(); super.onTaskRemoved(rootIntent); stopSelf() }
    override fun onDestroy() { notifPollJob?.cancel(); mediaSession?.release(); mediaSession = null; mediaSessionCompat?.release(); mediaSessionCompat = null; super.onDestroy() }
    override fun onBind(intent: Intent?): android.os.IBinder? = null
    companion object {
        const val ACTION_TOGGLE = "com.cactus.app.TOGGLE"; const val ACTION_SEEK_BACK = "com.cactus.app.SEEK_BACK"; const val ACTION_SEEK_FORWARD = "com.cactus.app.SEEK_FORWARD"; const val ACTION_STOP = "com.cactus.app.STOP"
        const val ACTION_SEEK_TO = "com.cactus.app.SEEK_TO"; const val EXTRA_SEEK_POSITION = "seek_position"
        const val NOTIFICATION_ID = 1; const val CHANNEL_ID = "playback_channel_v34"
    }
}
