package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ai.AiScannedMealItem
import com.example.data.ai.AiTimetableResult
import com.example.data.model.DayEnum
import com.example.data.model.MealType
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSecondaryContainer
import com.example.ui.theme.SleekTerracottaPrimary
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScanTimetableDialog(
    selectedImageUri: Uri?,
    isScanning: Boolean,
    statusText: String,
    scanResult: AiTimetableResult?,
    errorMessage: String?,
    geminiApiKey: String = "",
    onUpdateApiKey: (String) -> Unit = {},
    onSelectImageUri: (Uri?) -> Unit,
    onStartScan: (Uri?, String?) -> Unit,
    onLoadSampleTemplate: (String) -> Unit,
    onApplyResult: (AiTimetableResult, String?, Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onSelectImageUri(uri)
        }
    }

    var customNote by remember { mutableStateOf("") }
    var detectedMessName by remember(scanResult) { mutableStateOf(scanResult?.messName ?: "") }
    var importDishesToLibrary by remember { mutableStateOf(true) }
    var previewSelectedDay by remember { mutableIntStateOf(1) }

    // API Key configuration state
    var showApiKeyInput by remember { mutableStateOf(geminiApiKey.isBlank() || geminiApiKey == "MY_GEMINI_API_KEY") }
    var tempApiKeyInput by remember(geminiApiKey) { mutableStateOf(if (geminiApiKey != "MY_GEMINI_API_KEY") geminiApiKey else "") }
    var showKeyPassword by remember { mutableStateOf(false) }

    val isKeyConfigured = geminiApiKey.isNotBlank() && geminiApiKey != "MY_GEMINI_API_KEY"

    // Editable copy of scanned meals for manual tweaks before saving
    val editableMeals = remember(scanResult) {
        mutableStateListOf<AiScannedMealItem>().apply {
            scanResult?.meals?.let { addAll(it) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .testTag("ai_scan_timetable_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SleekPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SleekTerracottaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AI Timetable Scanner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "Extract schedule from image using Gemini Vision AI",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_ai_scan_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Content Body
            if (scanResult == null) {
                // Step 1: Upload & Image Configuration View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // API Key Config / Status Bar
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isKeyConfigured) SleekSecondaryContainer else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, if (isKeyConfigured) SleekBorder else SleekTerracottaPrimary.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isKeyConfigured) Icons.Default.CheckCircle else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (isKeyConfigured) Color(0xFF2E7D32) else SleekTerracottaPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isKeyConfigured) "Gemini Vision AI: Connected" else "Gemini API Key Required",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isKeyConfigured) Color(0xFF2E7D32) else SleekTerracottaPrimary
                                    )
                                }

                                Text(
                                    text = if (showApiKeyInput) "Hide" else if (isKeyConfigured) "Change Key" else "Enter Key",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTerracottaPrimary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { showApiKeyInput = !showApiKeyInput }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            AnimatedVisibility(
                                visible = showApiKeyInput,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Get your free Gemini API key from Google AI Studio (aistudio.google.com) to scan physical timetable photos:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SleekTextSecondary,
                                        lineHeight = 16.sp
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = tempApiKeyInput,
                                            onValueChange = { tempApiKeyInput = it },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("gemini_api_key_input"),
                                            placeholder = { Text("Paste AIzaSy... key here") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp),
                                            visualTransformation = if (showKeyPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                            trailingIcon = {
                                                IconButton(onClick = { showKeyPassword = !showKeyPassword }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = "Toggle Visibility",
                                                        tint = if (showKeyPassword) SleekTerracottaPrimary else SleekTextSecondary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = SleekBorder
                                            ),
                                            textStyle = MaterialTheme.typography.bodySmall
                                        )

                                        FilledTonalButton(
                                            onClick = {
                                                onUpdateApiKey(tempApiKeyInput.trim())
                                                showApiKeyInput = false
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            enabled = tempApiKeyInput.isNotBlank(),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Save")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Upload Drop Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!isScanning) {
                                    imagePickerLauncher.launch("image/*")
                                }
                            }
                            .testTag("upload_image_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedImageUri != null) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else SleekSecondaryContainer
                        ),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = if (selectedImageUri != null) SleekTerracottaPrimary else SleekBorder
                        )
                    ) {
                        if (selectedImageUri != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Selected timetable image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(14.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = SleekTerracottaPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Image Selected",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SleekTerracottaPrimary
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { imagePickerLauncher.launch("image/*") },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Change Photo", fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 30.dp, horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(SleekPrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = SleekTerracottaPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Tap to Upload Timetable Image",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Take a photo of your hostel notice board or upload timetable schedule",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Sample Presets for quick testing
                    Column {
                        Text(
                            text = "💡 Or Try Sample Timetable Schedules",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = false,
                                onClick = { onLoadSampleTemplate("North Indian Hostel Mess") },
                                label = { Text("🍛 North Indian Menu") },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SleekSecondaryContainer
                                )
                            )
                            FilterChip(
                                selected = false,
                                onClick = { onLoadSampleTemplate("South Indian Hostel Mess") },
                                label = { Text("🥞 South Indian Menu") },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SleekSecondaryContainer
                                )
                            )
                            FilterChip(
                                selected = false,
                                onClick = { onLoadSampleTemplate("IIT / NIT Campus Dining") },
                                label = { Text("🏛️ Campus Dining Menu") },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SleekSecondaryContainer
                                )
                            )
                        }
                    }

                    // Optional Notes
                    OutlinedTextField(
                        value = customNote,
                        onValueChange = { customNote = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_custom_note_input"),
                        label = { Text("Special notes or Mess Name (Optional)") },
                        placeholder = { Text("e.g. Block-B Hostel, Vegetarian only") },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SleekBorder
                        )
                    )

                    // Error Banner if any
                    errorMessage?.let { error ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Scanning Alert",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = error,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    // Scanning Progress Indicator
                    if (isScanning) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = SleekTerracottaPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = statusText.ifBlank { "Analyzing timetable image with Gemini Vision AI..." },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SleekTerracottaPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Scan Action Button
                    Button(
                        onClick = {
                            if (!isKeyConfigured && tempApiKeyInput.isNotBlank()) {
                                onUpdateApiKey(tempApiKeyInput.trim())
                            }
                            onStartScan(selectedImageUri, customNote)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_ai_scan_button"),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isScanning && (selectedImageUri != null || customNote.isNotBlank()),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SleekTerracottaPrimary
                        )
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Extracting with Gemini AI...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan & Extract with Gemini AI", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                // Step 2: Interactive Review & Confirm Extracted Timetable
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Success Banner
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SleekSecondaryContainer,
                        border = BorderStroke(1.dp, SleekBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SleekTerracottaPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${editableMeals.size} Meals Extracted (7 Days)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekTextPrimary
                                    )
                                }

                                TextButtonSimple(
                                    text = "Re-scan",
                                    onClick = {
                                        // Reset to upload view
                                        onSelectImageUri(null)
                                    }
                                )
                            }

                            if (scanResult.message.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = scanResult.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Mess Name Customizer
                    OutlinedTextField(
                        value = detectedMessName,
                        onValueChange = { detectedMessName = it },
                        label = { Text("Mess / Hostel Name") },
                        placeholder = { Text("e.g. Ganga Hostel Mess") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_mess_name_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SleekBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Day Preview Tabs
                    ScrollableTabRow(
                        selectedTabIndex = previewSelectedDay - 1,
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[previewSelectedDay - 1]),
                                color = SleekTerracottaPrimary,
                                height = 3.dp
                            )
                        },
                        divider = {}
                    ) {
                        DayEnum.entries.forEach { day ->
                            Tab(
                                selected = previewSelectedDay == day.dayNumber,
                                onClick = { previewSelectedDay = day.dayNumber },
                                text = {
                                    Text(
                                        text = day.shortName,
                                        fontWeight = if (previewSelectedDay == day.dayNumber) FontWeight.Bold else FontWeight.Normal,
                                        color = if (previewSelectedDay == day.dayNumber) SleekTerracottaPrimary else SleekTextSecondary
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Meals for selected day
                    val dayMeals = editableMeals.filter { it.dayOfWeek == previewSelectedDay }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(dayMeals, key = { "${it.dayOfWeek}_${it.mealType}" }) { meal ->
                            val mealTypeObj = MealType.fromString(meal.mealType)
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                border = BorderStroke(1.dp, SleekBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = mealTypeObj.emoji, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = mealTypeObj.displayName.uppercase(),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = SleekTerracottaPrimary
                                            )
                                        }

                                        Text(
                                            text = "${meal.startTime} - ${meal.endTime}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SleekTextSecondary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Editable Items
                                    OutlinedTextField(
                                        value = meal.items,
                                        onValueChange = { newItems ->
                                            val index = editableMeals.indexOfFirst {
                                                it.dayOfWeek == meal.dayOfWeek && it.mealType == meal.mealType
                                            }
                                            if (index != -1) {
                                                editableMeals[index] = meal.copy(items = newItems)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = SleekBorder.copy(alpha = 0.5f)
                                        ),
                                        textStyle = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        // Food Library Sync Option
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { importDishesToLibrary = !importDishesToLibrary }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = importDishesToLibrary,
                                    onCheckedChange = { importDishesToLibrary = it },
                                    colors = CheckboxDefaults.colors(checkedColor = SleekTerracottaPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Import dishes to Food Library catalog",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SleekTextPrimary
                                    )
                                    Text(
                                        text = "Automatically saves extracted dishes for quick menu customization later",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SleekTextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // Bottom Action Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val updatedResult = scanResult.copy(
                                    messName = detectedMessName.ifBlank { null },
                                    meals = editableMeals.toList()
                                )
                                onApplyResult(updatedResult, detectedMessName, importDishesToLibrary)
                            },
                            modifier = Modifier
                                .weight(1.8f)
                                .height(50.dp)
                                .testTag("apply_ai_timetable_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekTerracottaPrimary
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Apply to Timetable", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TextButtonSimple(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = SleekTerracottaPrimary,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
