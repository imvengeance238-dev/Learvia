package com.learvia.app.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.learvia.app.R
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object Notifications {
    private const val CHANNEL = "study"

    fun show(ctx: Context, id: Int, title: String, body: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, ctx.getString(R.string.reminders), NotificationManager.IMPORTANCE_DEFAULT))
        val launch = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
        val pi = PendingIntent.getActivity(ctx, 0, launch, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val n = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title).setContentText(body)
            .setContentIntent(pi).setAutoCancel(true).build()
        NotificationManagerCompat.from(ctx).notify(id, n)
    }
}

class ReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val c = applicationContext
        Notifications.show(c, 1, c.getString(R.string.reminder_title), c.getString(R.string.reminder_body))
        return Result.success()
    }
}

/** Daily reminder through WorkManager (inexact timing by design; no exact-alarm permission needed). */
object Reminders {
    private const val NAME = "daily_reminder"

    fun schedule(ctx: Context, hour: Int) {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(hour, 0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val req = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(Duration.between(now, next).toMillis(), TimeUnit.MILLISECONDS).build()
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.UPDATE, req)
    }

    fun cancel(ctx: Context) { WorkManager.getInstance(ctx).cancelUniqueWork(NAME) }
}

class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("learvia_prefs", Context.MODE_PRIVATE)
    var reminderEnabled: Boolean
        get() = sp.getBoolean("reminder_enabled", false)
        set(v) = sp.edit().putBoolean("reminder_enabled", v).apply()
    var reminderHour: Int
        get() = sp.getInt("reminder_hour", 20)
        set(v) = sp.edit().putInt("reminder_hour", v).apply()
    var seekSeconds: Int
        get() = sp.getInt("seek_seconds", 10)
        set(v) = sp.edit().putInt("seek_seconds", v).apply()
}
