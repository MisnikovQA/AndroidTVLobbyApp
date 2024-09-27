package com.example.lobbyapp

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat

class ForegroundService : Service() {

    private val CHANNEL_ID = "ForegroundServiceChannel"

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        // Интент для возвращения приложения на передний план при нажатии на уведомление
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("LobbyApp")
            .setContentText("Сервис работает")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Замените на свою иконку
            .setContentIntent(pendingIntent) // Добавляем intent
            .build()

        startForeground(1, notification)

        // Проверка разрешений на доступ к статистике
        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission()
        } else {
            // Запуск потока для проверки активности приложения
            Thread {
                while (true) {
                    val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                    val endTime = System.currentTimeMillis()
                    val beginTime = endTime - 1000 * 10 // 10 секунд

                    val stats: List<UsageStats> = usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        beginTime, endTime
                    )

                    Log.d("ForegroundService", "Получено количество stats: ${stats.size}")

                    if (stats.isNotEmpty()) {
                        val currentApp = stats.maxByOrNull { it.lastTimeUsed }?.packageName
                        Log.d("ForegroundService", "Текущее приложение: $currentApp")

                        // Если текущее приложение не наше, возвращаем его на передний план
                        if (currentApp != packageName) {
                            Log.d("ForegroundService", "Возвращаем приложение на передний план")
                            // Запускаем MainActivity, чтобы вернуть приложение на передний план
                            val intent = Intent(this, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                            startActivity(intent)
                        }
                    }

                    Thread.sleep(10000) // Проверка каждые 5 секунд
                }
            }.start()
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For API level 34 and above
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        } else {
            // For lower API levels
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }
}
