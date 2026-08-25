package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.MealEntity
import com.example.data.model.ActiveMealState
import com.example.data.model.DayEnum
import com.example.data.model.MealType
import com.example.data.repository.MealRepository
import com.example.notification.MealNotificationHelper
import com.example.util.MealTimeUtils
import com.example.widget.MessWidgetProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MessViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MealRepository
    private val context: Context get() = getApplication()

    private val _selectedDay = MutableStateFlow(MealTimeUtils.getCurrentDayNumber())
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMealFilter = MutableStateFlow<MealType?>(null)
    val selectedMealFilter: StateFlow<MealType?> = _selectedMealFilter.asStateFlow()

    private val _editingMeal = MutableStateFlow<MealEntity?>(null)
    val editingMeal: StateFlow<MealEntity?> = _editingMeal.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(false)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _reminderMinutes = MutableStateFlow(15)
    val reminderMinutes: StateFlow<Int> = _reminderMinutes.asStateFlow()

    private val _messName = MutableStateFlow("Hostel Mess")
    val messName: StateFlow<String> = _messName.asStateFlow()

    // Clock ticker flow to refresh active meal detection every 30 seconds
    private val tickerFlow = MutableStateFlow(System.currentTimeMillis())

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MealRepository(db.mealDao())

        // Initial setup
        MealNotificationHelper.createNotificationChannel(application)
        _notificationsEnabled.value = MealNotificationHelper.isNotificationsEnabled(application)
        _reminderMinutes.value = MealNotificationHelper.getReminderMinutes(application)
        _messName.value = MealNotificationHelper.getMessName(application)

        viewModelScope.launch {
            repository.ensureDefaultDataPopulated()
            MessWidgetProvider.updateAllWidgets(context)
        }

        // Ticker loop
        viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                tickerFlow.value = System.currentTimeMillis()
            }
        }
    }

    val allMeals: StateFlow<List<MealEntity>> = repository.allMeals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeMealState: StateFlow<ActiveMealState> = combine(allMeals, tickerFlow) { meals, _ ->
        MealTimeUtils.calculateActiveMealState(meals)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ActiveMealState(null, null, "Loading menu...", "", false)
    )

    fun selectDay(day: Int) {
        _selectedDay.value = day
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setMealFilter(filter: MealType?) {
        _selectedMealFilter.value = filter
    }

    fun openEditMeal(meal: MealEntity) {
        _editingMeal.value = meal
    }

    fun closeEditMeal() {
        _editingMeal.value = null
    }

    fun saveMealEdit(
        mealId: Long,
        items: String,
        startTime: String,
        endTime: String,
        specialNote: String
    ) {
        viewModelScope.launch {
            val existing = repository.getMealById(mealId)
            if (existing != null) {
                val updated = existing.copy(
                    items = items.trim(),
                    startTime = startTime.trim(),
                    endTime = endTime.trim(),
                    specialNote = specialNote.trim()
                )
                repository.updateMeal(updated)
                closeEditMeal()
                MessWidgetProvider.updateAllWidgets(context)
                if (_notificationsEnabled.value) {
                    MealNotificationHelper.scheduleNextMealReminder(context)
                }
            }
        }
    }

    fun toggleFavorite(mealId: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(mealId, !currentStatus)
        }
    }

    fun resetMenuToDefaults() {
        viewModelScope.launch {
            repository.resetToDefaultMenu()
            MessWidgetProvider.updateAllWidgets(context)
            if (_notificationsEnabled.value) {
                MealNotificationHelper.scheduleNextMealReminder(context)
            }
        }
    }

    fun setNotificationsToggle(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        MealNotificationHelper.setNotificationsEnabled(context, enabled)
    }

    fun updateReminderMinutes(minutes: Int) {
        _reminderMinutes.value = minutes
        MealNotificationHelper.setReminderMinutes(context, minutes)
    }

    fun updateMessName(name: String) {
        _messName.value = name
        MealNotificationHelper.setMessName(context, name)
    }

    fun shareDayMenu(dayNumber: Int) {
        viewModelScope.launch {
            val dayMeals = repository.getMealsForDaySync(dayNumber)
            val dayName = DayEnum.fromDayNumber(dayNumber).fullName
            val sb = StringBuilder()
            sb.append("🍽️ *${_messName.value}* - $dayName Timetable\n\n")

            for (meal in dayMeals) {
                val type = MealType.fromString(meal.mealType)
                val timeRange = MealTimeUtils.formatTimeRange(meal.startTime, meal.endTime)
                sb.append("${type.emoji} *${type.displayName.uppercase()}* ($timeRange)\n")
                sb.append("${meal.items}\n")
                if (meal.specialNote.isNotBlank()) {
                    sb.append("📌 _Note: ${meal.specialNote}_\n")
                }
                sb.append("\n")
            }
            sb.append("📱 Shared via MessMate App")

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, sb.toString())
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share $dayName Menu").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(shareIntent)
        }
    }
}
