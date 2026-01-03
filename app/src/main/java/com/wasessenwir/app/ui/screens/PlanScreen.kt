package com.wasessenwir.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wasessenwir.app.R
import com.wasessenwir.app.data.model.MealSlot
import com.wasessenwir.app.data.model.MealType
import com.wasessenwir.app.data.model.Recipe
import com.wasessenwir.app.ui.AppViewModel
import com.wasessenwir.app.ui.components.PrimaryButton
import com.wasessenwir.app.ui.theme.CyanPrimary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(viewModel: AppViewModel) {
    val planEntries by viewModel.planEntries.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val activeHouseholdId by viewModel.activeHouseholdId.collectAsState()

    var startDisplay by remember { mutableStateOf("") }
    var startIso by remember { mutableStateOf("") }
    var endDisplay by remember { mutableStateOf("") }
    var endIso by remember { mutableStateOf("") }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var planningActive by remember { mutableStateOf(false) }
    var showPlannerSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var mealFilter by remember { mutableStateOf(MealType.BOTH) }

    val selectedRecipeIds = remember { mutableStateListOf<String>() }
    val slotSelections = remember { mutableStateMapOf<String, String?>() }
    var initializedRangeKey by remember { mutableStateOf("") }

    val startPickerState = rememberDatePickerState()
    val endPickerState = rememberDatePickerState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isoFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    val displayFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    val rangeDates = remember(startIso, endIso, planningActive) {
        if (!planningActive || startIso.isBlank() || endIso.isBlank()) {
            emptyList()
        } else {
            val start = runCatching { LocalDate.parse(startIso, isoFormatter) }.getOrNull()
            val end = runCatching { LocalDate.parse(endIso, isoFormatter) }.getOrNull()
            if (start == null || end == null || end.isBefore(start)) {
                emptyList()
            } else {
                val dates = mutableListOf<LocalDate>()
                var cursor = start!!
                val endDate = end!!
                while (!cursor.isAfter(endDate)) {
                    dates.add(cursor)
                    cursor = cursor.plusDays(1)
                }
                dates
            }
        }
    }

    val mealFilteredRecipes = recipes.filter { recipe ->
        when (mealFilter) {
            MealType.LUNCH -> recipe.mealType == MealType.LUNCH || recipe.mealType == MealType.BOTH
            MealType.DINNER -> recipe.mealType == MealType.DINNER || recipe.mealType == MealType.BOTH
            MealType.BOTH -> true
        }
    }

    val recipeById = remember(recipes) { recipes.associateBy { it.id } }
    val recentRecipes = mealFilteredRecipes.sortedByDescending { it.updatedAt }.take(5)
    val filteredRecipes = mealFilteredRecipes.filter { recipe ->
        searchQuery.isBlank() || recipe.name.contains(searchQuery, ignoreCase = true)
    }

    val entryByKey = remember(planEntries) {
        planEntries.associateBy { entryKey(it.date, it.mealSlot) }
    }

    val rangeKey = "$startIso|$endIso"
    if (planningActive && rangeKey != initializedRangeKey && rangeDates.isNotEmpty()) {
        slotSelections.clear()
        selectedRecipeIds.clear()
        rangeDates.forEach { date ->
            val dateIso = date.format(isoFormatter)
            val lunchEntry = entryByKey[entryKey(dateIso, MealSlot.LUNCH)]
            val dinnerEntry = entryByKey[entryKey(dateIso, MealSlot.DINNER)]
            if (lunchEntry != null) {
                slotSelections[entryKey(dateIso, MealSlot.LUNCH)] = lunchEntry.recipeId
                if (!selectedRecipeIds.contains(lunchEntry.recipeId)) {
                    selectedRecipeIds.add(lunchEntry.recipeId)
                }
            }
            if (dinnerEntry != null) {
                slotSelections[entryKey(dateIso, MealSlot.DINNER)] = dinnerEntry.recipeId
                if (!selectedRecipeIds.contains(dinnerEntry.recipeId)) {
                    selectedRecipeIds.add(dinnerEntry.recipeId)
                }
            }
        }
        initializedRangeKey = rangeKey
    }

    val selectedRecipes = recipes.filter { selectedRecipeIds.contains(it.id) }
    val lunchMissing = rangeDates.filter { date ->
        val key = entryKey(date.format(isoFormatter), MealSlot.LUNCH)
        slotSelections[key].isNullOrBlank()
    }
    val completionLabel = if (rangeDates.isNotEmpty()) {
        stringResource(
            R.string.plan_completeness_label,
            rangeDates.size - lunchMissing.size,
            rangeDates.size
        )
    } else {
        ""
    }
    val rangeIsoDates = remember(rangeDates) { rangeDates.map { it.format(isoFormatter) } }
    val rangeEntries = remember(planEntries, rangeIsoDates) {
        if (rangeIsoDates.isEmpty()) {
            emptyList()
        } else {
            val dateSet = rangeIsoDates.toSet()
            planEntries.filter { dateSet.contains(it.date) }
        }
    }
    val lunchCount = rangeEntries.count { it.mealSlot == MealSlot.LUNCH }
    val dinnerCount = rangeEntries.count { it.mealSlot == MealSlot.DINNER }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        if (activeHouseholdId == null) {
            Text(text = stringResource(R.string.needs_active_household))
            return
        }

        Text(text = stringResource(R.string.plan_entry_title), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.plan_range_title), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Column(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = startDisplay,
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(R.string.plan_range_start_label)) },
                        readOnly = true
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showStartPicker = true }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = endDisplay,
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(R.string.plan_range_end_label)) },
                        readOnly = true
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showEndPicker = true }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryButton(
            text = stringResource(R.string.button_create_list),
            onClick = {
                if (startIso.isNotBlank() && endIso.isNotBlank()) {
                    if (!planningActive) {
                        planningActive = true
                        initializedRangeKey = ""
                    }
                    showPlannerSheet = true
                }
            }
        )

        if (planningActive && rangeDates.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(R.string.plan_list_overview_title), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.plan_list_range_label, startDisplay, endDisplay),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.plan_lunch_progress_label, lunchCount, rangeDates.size),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = stringResource(R.string.plan_dinner_progress_label, dinnerCount, rangeDates.size),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    rangeDates.forEach { date ->
                        val dateIso = date.format(isoFormatter)
                        val lunchId = entryByKey[entryKey(dateIso, MealSlot.LUNCH)]?.recipeId
                        val dinnerId = entryByKey[entryKey(dateIso, MealSlot.DINNER)]?.recipeId
                        val lunchName = lunchId?.let { recipeById[it]?.name } ?: "-"
                        val dinnerName = dinnerId?.let { recipeById[it]?.name } ?: "-"

                        Column(modifier = Modifier.padding(bottom = 8.dp)) {
                            Text(text = date.format(displayFormatter), style = MaterialTheme.typography.labelLarge)
                            Text(
                                text = stringResource(R.string.plan_lunch_summary, lunchName),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = stringResource(R.string.plan_dinner_summary, dinnerName),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        PrimaryButton(
                            text = stringResource(R.string.plan_add_to_shopping_list),
                            onClick = {
                                if (startIso.isNotBlank()) {
                                    viewModel.createShoppingListFromPlan(startIso, rangeEntries, recipes)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { showPlannerSheet = true }) {
                            Text(text = stringResource(R.string.button_edit))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                rangeEntries.forEach { entry ->
                                    viewModel.deletePlanEntry(entry.id)
                                }
                                planningActive = false
                                showPlannerSheet = false
                                startIso = ""
                                endIso = ""
                                startDisplay = ""
                                endDisplay = ""
                                searchQuery = ""
                                mealFilter = MealType.BOTH
                                initializedRangeKey = ""
                                selectedRecipeIds.clear()
                                slotSelections.clear()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(text = stringResource(R.string.button_delete))
                        }
                    }
                }
            }
        }
    }

    if (showPlannerSheet && planningActive && rangeDates.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { showPlannerSheet = false },
            sheetState = sheetState
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(text = stringResource(R.string.plan_recipe_pick_title), style = MaterialTheme.typography.titleMedium)
                }
                item {
                    Row {
                        SegmentedChoicePlan(
                            text = stringResource(R.string.meal_all),
                            selected = mealFilter == MealType.BOTH,
                            onClick = { mealFilter = MealType.BOTH }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SegmentedChoicePlan(
                            text = stringResource(R.string.meal_lunch),
                            selected = mealFilter == MealType.LUNCH,
                            onClick = { mealFilter = MealType.LUNCH }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SegmentedChoicePlan(
                            text = stringResource(R.string.meal_dinner),
                            selected = mealFilter == MealType.DINNER,
                            onClick = { mealFilter = MealType.DINNER }
                        )
                    }
                }
                if (recentRecipes.isNotEmpty()) {
                    item {
                        Text(text = stringResource(R.string.recipe_recent_title), style = MaterialTheme.typography.titleMedium)
                    }
                    items(recentRecipes, key = { "recent-${it.id}" }) { recipe ->
                        RecipeToggleRow(
                            recipe = recipe,
                            selected = selectedRecipeIds.contains(recipe.id),
                            onToggle = { toggleRecipe(selectedRecipeIds, recipe.id) }
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(R.string.recipe_search_label)) }
                    )
                }
                if (filteredRecipes.isEmpty()) {
                    item {
                        Text(text = stringResource(R.string.recipe_search_empty))
                    }
                } else {
                    items(filteredRecipes, key = { "all-${it.id}" }) { recipe ->
                        RecipeToggleRow(
                            recipe = recipe,
                            selected = selectedRecipeIds.contains(recipe.id),
                            onToggle = { toggleRecipe(selectedRecipeIds, recipe.id) }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = stringResource(R.string.plan_assignment_title), style = MaterialTheme.typography.titleMedium)
                }
                if (completionLabel.isNotBlank()) {
                    item {
                        Text(text = completionLabel, style = MaterialTheme.typography.bodySmall)
                    }
                }
                items(rangeDates, key = { it.format(isoFormatter) }) { date ->
                    val dateIso = date.format(isoFormatter)
                    val dateLabel = date.format(displayFormatter)
                    val lunchKey = entryKey(dateIso, MealSlot.LUNCH)
                    val dinnerKey = entryKey(dateIso, MealSlot.DINNER)
                    val lunchSelection = slotSelections[lunchKey]
                    val dinnerSelection = slotSelections[dinnerKey]
                    val missingLunch = lunchSelection.isNullOrBlank()

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = dateLabel, style = MaterialTheme.typography.titleMedium)
                            if (missingLunch) {
                                Text(
                                    text = stringResource(R.string.plan_missing_lunch),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            RecipeDropdown(
                                label = stringResource(R.string.plan_meal_lunch_label),
                                recipes = selectedRecipes,
                                selectedId = lunchSelection,
                                onSelect = { slotSelections[lunchKey] = it }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            RecipeDropdown(
                                label = stringResource(R.string.plan_meal_dinner_label),
                                recipes = selectedRecipes,
                                selectedId = dinnerSelection,
                                onSelect = { slotSelections[dinnerKey] = it }
                            )
                        }
                    }
                }
                item {
                    Row {
                        PrimaryButton(
                            text = stringResource(R.string.plan_save_button),
                            onClick = {
                                val entriesByKey = planEntries.associateBy { entryKey(it.date, it.mealSlot) }
                                rangeDates.forEach { date ->
                                    val dateIso = date.format(isoFormatter)
                                    listOf(MealSlot.LUNCH, MealSlot.DINNER).forEach { slot ->
                                        val key = entryKey(dateIso, slot)
                                        val selectedId = slotSelections[key]
                                        val existing = entriesByKey[key]
                                        if (selectedId.isNullOrBlank()) {
                                            if (existing != null) {
                                                viewModel.deletePlanEntry(existing.id)
                                            }
                                        } else {
                                            if (existing == null) {
                                                viewModel.createPlanEntry(dateIso, slot, selectedId)
                                            } else if (existing.recipeId != selectedId) {
                                                viewModel.updatePlanEntry(existing.copy(recipeId = selectedId))
                                            }
                                        }
                                    }
                                }
                                showPlannerSheet = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { showPlannerSheet = false }) {
                            Text(text = stringResource(R.string.button_close))
                        }
                    }
                }
            }
        }
    }

    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = startPickerState.selectedDateMillis
                    if (millis != null) {
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        startIso = localDate.format(isoFormatter)
                        startDisplay = localDate.format(displayFormatter)
                    }
                    showStartPicker = false
                }) {
                    Text(text = stringResource(R.string.button_apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text(text = stringResource(R.string.button_cancel))
                }
            }
        ) {
            DatePicker(state = startPickerState)
        }
    }

    if (showEndPicker) {
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = endPickerState.selectedDateMillis
                    if (millis != null) {
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        endIso = localDate.format(isoFormatter)
                        endDisplay = localDate.format(displayFormatter)
                    }
                    showEndPicker = false
                }) {
                    Text(text = stringResource(R.string.button_apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text(text = stringResource(R.string.button_cancel))
                }
            }
        ) {
            DatePicker(state = endPickerState)
        }
    }
}

@Composable
private fun RecipeToggleRow(
    recipe: Recipe,
    selected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            Text(
                text = recipe.name,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(onClick = onToggle) {
                Text(
                    text = if (selected) {
                        stringResource(R.string.button_remove_from_list)
                    } else {
                        stringResource(R.string.button_add_to_list)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeDropdown(
    label: String,
    recipes: List<Recipe>,
    selectedId: String?,
    onSelect: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = recipes.firstOrNull { it.id == selectedId }?.name

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedName ?: "-",
            onValueChange = { },
            readOnly = true,
            label = { Text(text = label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.plan_no_selection)) },
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )
            recipes.forEach { recipe ->
                DropdownMenuItem(
                    text = { Text(text = recipe.name) },
                    onClick = {
                        onSelect(recipe.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun entryKey(dateIso: String, slot: MealSlot): String {
    return "$dateIso|${slot.name}"
}

private fun toggleRecipe(selectedIds: MutableList<String>, recipeId: String) {
    if (selectedIds.contains(recipeId)) {
        selectedIds.remove(recipeId)
    } else {
        selectedIds.add(recipeId)
    }
}

@Composable
private fun SegmentedChoicePlan(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) CyanPrimary else Color.Transparent,
            contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
