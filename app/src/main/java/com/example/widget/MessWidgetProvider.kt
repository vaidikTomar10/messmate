package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.repository.MealRepository
import com.example.util.MealTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val repository = MealRepository(db.mealDao(), db.foodDao())
                repository.ensureDefaultDataPopulated()
                val allMeals = repository.getAllMealsSync()
                val state = MealTimeUtils.calculateActiveMealState(allMeals)

                for (appWidgetId in appWidgetIds) {
                    val views = buildRemoteViews(context, state)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun buildRemoteViews(context: Context, state: com.example.data.model.ActiveMealState): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_mess_layout)

        val active = state.activeMeal
        val next = state.nextMeal

        if (active != null) {
            views.setTextViewText(R.id.widget_status_badge, "ACTIVE NOW")
            views.setTextViewText(
                R.id.widget_meal_title,
                "${active.mealType.emoji} ${active.mealType.displayName.uppercase()}"
            )
            views.setTextViewText(
                R.id.widget_meal_time,
                MealTimeUtils.formatTimeRange(active.startTime, active.endTime)
            )
            views.setTextViewText(
                R.id.widget_meal_items,
                if (active.rawItems.isNotBlank()) active.rawItems else "No menu items specified"
            )
        } else if (next != null) {
            views.setTextViewText(R.id.widget_status_badge, "UPCOMING")
            views.setTextViewText(
                R.id.widget_meal_title,
                "${next.mealType.emoji} ${next.mealType.displayName.uppercase()}"
            )
            views.setTextViewText(
                R.id.widget_meal_time,
                MealTimeUtils.formatTimeRange(next.startTime, next.endTime)
            )
            views.setTextViewText(
                R.id.widget_meal_items,
                if (next.rawItems.isNotBlank()) next.rawItems else "No menu items specified"
            )
        } else {
            views.setTextViewText(R.id.widget_status_badge, "UP TO DATE")
            views.setTextViewText(R.id.widget_meal_title, "🍽️ MessMate")
            views.setTextViewText(R.id.widget_meal_time, "")
            views.setTextViewText(R.id.widget_meal_items, "No meals scheduled")
        }

        // Next meal preview footer
        if (active != null && next != null) {
            views.setTextViewText(
                R.id.widget_next_meal_text,
                "Next: ${next.mealType.emoji} ${next.mealType.displayName} at ${MealTimeUtils.format12Hour(next.startTime)}"
            )
        } else if (next != null && !state.isCurrentlyActive) {
            views.setTextViewText(
                R.id.widget_next_meal_text,
                state.statusText
            )
        } else {
            views.setTextViewText(
                R.id.widget_next_meal_text,
                "Tap to view weekly schedule"
            )
        }

        // PendingIntent to launch app on tap
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        return views
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val componentName = ComponentName(context, MessWidgetProvider::class.java)
                    val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                    if (appWidgetIds.isNotEmpty()) {
                        val db = AppDatabase.getDatabase(context)
                        val repository = MealRepository(db.mealDao(), db.foodDao())
                        val allMeals = repository.getAllMealsSync()
                        val state = MealTimeUtils.calculateActiveMealState(allMeals)

                        val provider = MessWidgetProvider()
                        for (id in appWidgetIds) {
                            val views = provider.buildRemoteViews(context, state)
                            appWidgetManager.updateAppWidget(id, views)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
