package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MealAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("meal_title") ?: "Upcoming Meal"
        val time = intent.getStringExtra("meal_time") ?: ""
        val items = intent.getStringExtra("meal_items") ?: "Check the mess menu in MessMate"

        MealNotificationHelper.showMealNotification(context, title, time, items)

        // Reschedule next meal reminder
        if (MealNotificationHelper.isNotificationsEnabled(context)) {
            MealNotificationHelper.scheduleNextMealReminder(context)
        }
    }
}
