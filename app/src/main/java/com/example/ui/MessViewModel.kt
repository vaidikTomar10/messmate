package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.FoodItemEntity
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

    private val _foodCategoryFilter = MutableStateFlow<String?>(null)
    val foodCategoryFilter: StateFlow<String?> = _foodCategoryFilter.asStateFlow()

    private val _foodSearchQuery = MutableStateFlow("")
    val foodSearchQuery: StateFlow<String> = _foodSearchQuery.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(false)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _reminderMinutes = MutableStateFlow(15)
    val reminderMinutes: StateFlow<Int> = _reminderMinutes.asStateFlow()

    private val _messName = MutableStateFlow("Hostel Mess")
    val messName: StateFlow<String> = _messName.asStateFlow()

    private val _geminiApiKey = MutableStateFlow("")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    // AI Timetable Scanner State
    private val _showAiScanDialog = MutableStateFlow(false)
    val showAiScanDialog: StateFlow<Boolean> = _showAiScanDialog.asStateFlow()

    private val _isAiScanning = MutableStateFlow(false)
    val isAiScanning: StateFlow<Boolean> = _isAiScanning.asStateFlow()

    private val _aiScanStatusText = MutableStateFlow("")
    val aiScanStatusText: StateFlow<String> = _aiScanStatusText.asStateFlow()

    private val _aiScanResult = MutableStateFlow<com.example.data.ai.AiTimetableResult?>(null)
    val aiScanResult: StateFlow<com.example.data.ai.AiTimetableResult?> = _aiScanResult.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<android.net.Uri?>(null)
    val selectedImageUri: StateFlow<android.net.Uri?> = _selectedImageUri.asStateFlow()

    private val _aiScanError = MutableStateFlow<String?>(null)
    val aiScanError: StateFlow<String?> = _aiScanError.asStateFlow()

    // Clock ticker flow to refresh active meal detection every 30 seconds
    private val tickerFlow = MutableStateFlow(System.currentTimeMillis())

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MealRepository(db.mealDao(), db.foodDao())

        // Initial setup
        MealNotificationHelper.createNotificationChannel(application)
        _notificationsEnabled.value = MealNotificationHelper.isNotificationsEnabled(application)
        _reminderMinutes.value = MealNotificationHelper.getReminderMinutes(application)
        _messName.value = MealNotificationHelper.getMessName(application)
        _geminiApiKey.value = MealNotificationHelper.getGeminiApiKey(application)

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

    val allFoodItems: StateFlow<List<FoodItemEntity>> = repository.allFoodItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCategories: StateFlow<List<String>> = repository.allCategories
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

    fun setFoodCategoryFilter(category: String?) {
        _foodCategoryFilter.value = category
    }

    fun setFoodSearchQuery(query: String) {
        _foodSearchQuery.value = query
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

    fun addFoodItem(name: String, category: String, iconEmoji: String = "🍽️") {
        viewModelScope.launch {
            repository.insertFoodItem(name, category, iconEmoji)
        }
    }

    fun deleteFoodItem(id: Long) {
        viewModelScope.launch {
            repository.deleteFoodItem(id)
        }
    }

    fun addFoodToTimetable(dayOfWeek: Int, mealType: String, foodName: String) {
        viewModelScope.launch {
            repository.addFoodItemToMeal(dayOfWeek, mealType, foodName)
            MessWidgetProvider.updateAllWidgets(context)
        }
    }

    fun resetFoodLibrary() {
        viewModelScope.launch {
            repository.resetFoodLibrary()
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

    fun updateGeminiApiKey(apiKey: String) {
        _geminiApiKey.value = apiKey.trim()
        MealNotificationHelper.setGeminiApiKey(context, apiKey.trim())
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

    // AI Timetable Scanner Actions
    fun openAiScanDialog() {
        _showAiScanDialog.value = true
        _aiScanError.value = null
    }

    fun closeAiScanDialog() {
        _showAiScanDialog.value = false
        _isAiScanning.value = false
        _aiScanError.value = null
    }

    fun setSelectedImageUri(uri: android.net.Uri?) {
        _selectedImageUri.value = uri
        _aiScanError.value = null
    }

    fun clearAiScan() {
        _selectedImageUri.value = null
        _aiScanResult.value = null
        _aiScanError.value = null
        _isAiScanning.value = false
    }

    fun scanTimetableImage(context: Context, uri: android.net.Uri?, customNote: String? = null) {
        if (uri == null) {
            _aiScanError.value = "Please select or upload a timetable image first."
            return
        }

        val key = _geminiApiKey.value.ifBlank { MealNotificationHelper.getGeminiApiKey(context) }
        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            _aiScanError.value = "Gemini API key is required for AI image scanning. Please enter your Gemini API key or use a sample timetable template."
            return
        }

        viewModelScope.launch {
            _isAiScanning.value = true
            _aiScanError.value = null
            _aiScanStatusText.value = "Loading & processing image..."

            val bitmap = com.example.data.ai.TimetableAiService.loadScaledBitmap(context, uri)
            if (bitmap == null) {
                _isAiScanning.value = false
                _aiScanError.value = "Failed to load image. Please select a valid photo."
                return@launch
            }

            _aiScanStatusText.value = "Gemini Vision AI is analyzing days, meal columns & food items..."
            val result = com.example.data.ai.TimetableAiService.analyzeTimetableImage(bitmap, key, customNote)

            result.onSuccess { scanResult ->
                _aiScanResult.value = scanResult
                _isAiScanning.value = false
                _aiScanStatusText.value = "Extraction complete! Review your schedule below."
            }.onFailure { error ->
                _isAiScanning.value = false
                _aiScanError.value = error.message ?: "AI Scanning failed. Please check your image or API key."
            }
        }
    }

    fun loadSampleTimetableTemplate(templateTitle: String) {
        viewModelScope.launch {
            _isAiScanning.value = true
            _aiScanStatusText.value = "Generating timetable schedule with AI..."
            delay(800)
            val demoResult = com.example.data.ai.TimetableAiService.getSmartDemoResult(templateTitle)
            _aiScanResult.value = demoResult
            _isAiScanning.value = false
            _aiScanStatusText.value = "Sample timetable loaded and ready for review!"
        }
    }

    fun applyScannedTimetable(
        result: com.example.data.ai.AiTimetableResult,
        customMessName: String?,
        importDishesToLibrary: Boolean
    ) {
        viewModelScope.launch {
            repository.applyScannedTimetable(
                scannedMeals = result.meals,
                extractedDishes = result.extractedDishes,
                importDishesToLibrary = importDishesToLibrary
            )

            // Update mess name if provided or extracted
            val newName = customMessName?.takeIf { it.isNotBlank() } ?: result.messName
            if (!newName.isNullOrBlank()) {
                updateMessName(newName.trim())
            }

            MessWidgetProvider.updateAllWidgets(context)
            if (_notificationsEnabled.value) {
                MealNotificationHelper.scheduleNextMealReminder(context)
            }

            closeAiScanDialog()
            clearAiScan()
        }
    }
}
