package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.repository.MealRepository
import com.example.util.MealTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object MealNotificationHelper {
    const val CHANNEL_ID = "messmate_meal_reminders"
    const val PREFS_NAME = "messmate_prefs"
    const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val KEY_REMINDER_MINUTES = "reminder_minutes"
    const val KEY_MESS_NAME = "mess_name"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.meal_channel_name)
            val descriptionText = context.getString(R.string.meal_channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun isNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        if (enabled) {
            scheduleNextMealReminder(context)
        } else {
            cancelMealReminders(context)
        }
    }

    fun getReminderMinutes(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_REMINDER_MINUTES, 15)
    }

    fun setReminderMinutes(context: Context, minutes: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_REMINDER_MINUTES, minutes).apply()
        if (isNotificationsEnabled(context)) {
            scheduleNextMealReminder(context)
        }
    }

    fun getMessName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_MESS_NAME, "Hostel Mess") ?: "Hostel Mess"
    }

    fun setMessName(context: Context, name: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_MESS_NAME, name.ifBlank { "Hostel Mess" }).apply()
    }

    fun showMealNotification(
        context: Context,
        mealTitle: String,
        mealTime: String,
        items: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ Up Next: $mealTitle ($mealTime)")
            .setContentText(items)
            .setStyle(NotificationCompat.BigTextStyle().bigText(items))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, notification)
    }

    fun scheduleNextMealReminder(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val repository = MealRepository(db.mealDao())
                val allMeals = repository.getAllMealsSync()
                val currentDay = MealTimeUtils.getCurrentDayNumber()
                val currentMinutes = MealTimeUtils.getCurrentMinutesOfDay()
                val advanceMinutes = getReminderMinutes(context)

                // Look for next meal today where start - advanceMinutes > currentMinutes
                val todayMeals = allMeals.filter { it.dayOfWeek == currentDay }
                    .sortedBy { MealTimeUtils.parseTimeToMinutes(it.startTime) }

                var targetMeal = todayMeals.firstOrNull {
                    val startM = MealTimeUtils.parseTimeToMinutes(it.startTime)
                    startM - advanceMinutes > currentMinutes
                }

                var targetDayOffset = 0
                if (targetMeal == null) {
                    // Check tomorrow's first meal
                    val nextDay = if (currentDay == 7) 1 else currentDay + 1
                    targetMeal = allMeals.filter { it.dayOfWeek == nextDay }
                        .minByOrNull { MealTimeUtils.parseTimeToMinutes(it.startTime) }
                    targetDayOffset = 1
                }

                if (targetMeal != null) {
                    val startM = MealTimeUtils.parseTimeToMinutes(targetMeal.startTime)
                    val triggerMinutes = (startM - advanceMinutes).coerceAtLeast(0)
                    val triggerHour = triggerMinutes / 60
                    val triggerMin = triggerMinutes % 60

                    val targetCal = Calendar.getInstance().apply {
                        if (targetDayOffset > 0) {
                            add(Calendar.DAY_OF_YEAR, targetDayOffset)
                        }
                        set(Calendar.HOUR_OF_DAY, triggerHour)
                        set(Calendar.MINUTE, triggerMin)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    val intent = Intent(context, MealAlarmReceiver::class.java).apply {
                        putExtra("meal_title", targetMeal.mealType)
                        putExtra("meal_time", MealTimeUtils.formatTimeRange(targetMeal.startTime, targetMeal.endTime))
                        putExtra("meal_items", targetMeal.items)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        2001,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetCal.timeInMillis,
                        pendingIntent
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cancelMealReminders(context: Context) {
        val intent = Intent(context, MealAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2001,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
