package com.example.lobbyapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private val targetUrl = "https://google.com" // Замените на нужный URL
    private val refreshIntervalMillis = 60 * 60 * 1000L // 1 час

    // Разрешения, которые необходимо запросить
    private val requiredPermissions = arrayOf(
        Manifest.permission.INTERNET
    )

    private fun disableBatteryOptimization() {
        // Отключение оптимизации батареи для приложения
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    // Лаунчер для запроса разрешения на наложение
    private val overlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Проверяем разрешение после возврата из настроек
        if (!Settings.canDrawOverlays(this)) {
            // Разрешение не предоставлено, закройте приложение или уведомите пользователя
            finish()
        }
    }

    // Проверка разрешения SYSTEM_ALERT_WINDOW для работы в фоновом режиме
    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    // Обработчик для обновления страницы
    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            webView.reload()
            handler.postDelayed(this, refreshIntervalMillis)
        }
    }

    // Лаунчер для запроса разрешений
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value == true }) {
            setupWebView()
        } else {
            // Если разрешения не получены, закроем приложение
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Отключение оптимизации батареи
        disableBatteryOptimization()

        // Проверка разрешения на SYSTEM_ALERT_WINDOW
        checkOverlayPermission()

        // Проверка и запрос разрешений
        if (!hasPermissions()) {
            requestPermissions()
        } else {
            setupWebView()
        }

        // Запуск ForegroundService
        val serviceIntent = Intent(this, ForegroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun hasPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(requiredPermissions)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.loadsImagesAutomatically = true
            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            loadUrl(targetUrl)
        }
        setContentView(webView)

        // Запуск обновления страницы каждый час
        handler.postDelayed(refreshRunnable, refreshIntervalMillis)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(refreshRunnable)
    }

    // Блокировка кнопки "Назад"
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // Ничего не делаем для блокировки кнопки "Назад"
    }
}
