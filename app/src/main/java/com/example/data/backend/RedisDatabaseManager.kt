package com.example.data.backend

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class RedisConnectionState(
    val isConnected: Boolean = true,
    val endpoint: String = "redis://127.0.0.1:6379 (Local In-Memory Cache)",
    val isCloudConnected: Boolean = false,
    val cachedKeysCount: Int = 0,
    val totalSyncOperations: Int = 0,
    val lastSyncTime: String = "Never",
    val recentCommands: List<String> = emptyList(),
    val isSyncing: Boolean = false,
    val databaseStats: String = "Ready"
)

class RedisDatabaseManager private constructor(private val context: Context) {
    private val TAG = "RedisDatabaseManager"
    private val scope = CoroutineScope(Dispatchers.IO)

    // In-memory Redis Key-Value and Hash store for local ultra-fast response
    private val stringStore = ConcurrentHashMap<String, String>()
    private val hashStore = ConcurrentHashMap<String, ConcurrentHashMap<String, String>>()
    private val listStore = ConcurrentHashMap<String, MutableList<String>>()

    private val _connectionState = MutableStateFlow(RedisConnectionState())
    val connectionState: StateFlow<RedisConnectionState> = _connectionState.asStateFlow()

    private val commandLogs = mutableListOf<String>()

    init {
        logCommand("REDIS INIT: Server ready. Memory database initialized.")
        // Preload default schema
        set("system:status", "ONLINE")
        set("focus:lock:status", "UNLOCKED")
    }

    private fun logCommand(cmd: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val entry = "[$time] $cmd"
        commandLogs.add(0, entry)
        if (commandLogs.size > 20) commandLogs.removeAt(commandLogs.size - 1)
        _connectionState.value = _connectionState.value.copy(
            recentCommands = commandLogs.toList(),
            cachedKeysCount = stringStore.size + hashStore.size + listStore.size
        )
    }

    // Redis Core Commands
    fun set(key: String, value: String) {
        stringStore[key] = value
        logCommand("SET $key -> ${value.take(30)}...")
    }

    fun get(key: String): String? {
        val v = stringStore[key]
        logCommand("GET $key -> ${if (v != null) "FOUND" else "NIL"}")
        return v
    }

    fun hset(key: String, field: String, value: String) {
        val map = hashStore.getOrPut(key) { ConcurrentHashMap() }
        map[field] = value
        logCommand("HSET $key $field -> $value")
    }

    fun hget(key: String, field: String): String? {
        return hashStore[key]?.get(field)
    }

    fun hgetall(key: String): Map<String, String> {
        return hashStore[key]?.toMap() ?: emptyMap()
    }

    fun lpush(key: String, value: String) {
        val list = listStore.getOrPut(key) { mutableListOf() }
        list.add(0, value)
        logCommand("LPUSH $key (${list.size} items)")
    }

    fun publish(channel: String, message: String) {
        logCommand("PUBLISH $channel: $message")
    }

    // Comprehensive Database & Redis Synchronization
    suspend fun syncDatabaseWithRedis(database: AppDatabase, settings: UserSettingsEntity?) = withContext(Dispatchers.IO) {
        _connectionState.value = _connectionState.value.copy(isSyncing = true)
        try {
            logCommand("SYNC: Starting local Room Database -> Redis sync pipeline...")

            val subjects = database.subjectDao().getAllSubjectsList()
            val sessions = database.timetableDao().getAllSessionsList()
            val focusRecords = database.focusSessionDao().getAllRecordsList()

            // 1. Sync User Profile Hash
            val userName = settings?.userName ?: "Scholar"
            val userEmail = settings?.userEmail ?: "user@gmail.com"
            val isGoogle = if (settings?.isGoogleSignedIn == true) "1" else "0"

            hset("user:profile", "name", userName)
            hset("user:profile", "email", userEmail)
            hset("user:profile", "google_auth", isGoogle)
            hset("user:profile", "daily_goal_mins", (settings?.dailyGoalMinutes ?: 360).toString())

            // 2. Sync Subjects to Redis Hash
            subjects.forEach { sub ->
                hset("subjects:all", sub.id.toString(), sub.name)
            }

            // 3. Sync Timetable Sessions to Redis List & Key-Value
            set("timetable:total_count", sessions.size.toString())
            sessions.forEach { s ->
                val sessionJson = """{"id":${s.id},"subject":"${s.subjectName}","start":"${s.startTime}","end":"${s.endTime}","day":${s.dayOfWeek}}"""
                set("timetable:session:${s.id}", sessionJson)
                lpush("timetable:day:${s.dayOfWeek}", sessionJson)
            }

            // 4. Update Redis Lock Status
            set("focus:distraction_shield:status", settings?.focusProtectionLevel ?: "ENHANCED")
            set("focus:distraction_shield:blocked_apps_count", "8") // Instagram, YouTube, TikTok, Facebook, Twitter, Reddit, Snapchat, Netflix

            // 5. If Cloud Redis REST URL is configured, push there as well
            val prefs = context.getSharedPreferences("redis_config", Context.MODE_PRIVATE)
            val cloudUrl = prefs.getString("custom_redis_url", null)
            val cloudToken = prefs.getString("custom_redis_token", null)
            var cloudSuccess = false

            if (!cloudUrl.isNullOrBlank()) {
                cloudSuccess = executeHttpRedisCommand(cloudUrl, cloudToken, "SET", listOf("focusin:last_sync", System.currentTimeMillis().toString()))
            }

            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val statsSummary = "${sessions.size} sessions, ${subjects.size} subjects, ${focusRecords.size} records"

            _connectionState.value = _connectionState.value.copy(
                isSyncing = false,
                lastSyncTime = timestamp,
                totalSyncOperations = _connectionState.value.totalSyncOperations + 1,
                cachedKeysCount = stringStore.size + hashStore.size + listStore.size,
                databaseStats = statsSummary,
                isCloudConnected = cloudSuccess,
                endpoint = if (cloudSuccess) cloudUrl ?: _connectionState.value.endpoint else _connectionState.value.endpoint
            )

            logCommand("SYNC COMPLETE: $statsSummary synced into Redis cache successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Sync to Redis failed", e)
            _connectionState.value = _connectionState.value.copy(
                isSyncing = false,
                lastSyncTime = "Error at ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}"
            )
            logCommand("SYNC ERROR: ${e.localizedMessage}")
        }
    }

    // Optional Upstash / Redis HTTP REST execution
    private fun executeHttpRedisCommand(urlStr: String, token: String?, command: String, args: List<String>): Boolean {
        return try {
            val endpoint = if (urlStr.endsWith("/")) urlStr else "$urlStr/"
            val url = URL(endpoint + command + "/" + args.joinToString("/"))
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            if (!token.isNullOrBlank()) {
                conn.setRequestProperty("Authorization", "Bearer $token")
            }
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val code = conn.responseCode
            conn.disconnect()
            code in 200..299
        } catch (e: Exception) {
            Log.w(TAG, "Cloud Redis HTTP request failed: ${e.message}")
            false
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: RedisDatabaseManager? = null

        fun getInstance(context: Context): RedisDatabaseManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RedisDatabaseManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
