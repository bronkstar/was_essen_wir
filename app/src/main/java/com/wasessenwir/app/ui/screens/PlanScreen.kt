package com.wasessenwir.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wasessenwir.app.data.model.MealSlot
import com.wasessenwir.app.data.model.MealType
import com.wasessenwir.app.data.model.PlanEntry
import com.wasessenwir.app.ui.AppViewModel
import com.wasessenwir.app.R
import com.wasessenwir.app.ui.components.PrimaryButton
import com.wasessenwir.app.ui.theme.CyanPrimary
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.foundation.clickable
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

    var dateDisplay by remember { mutableStateOf("") }
    var dateIso by remember { mutableStateOf("") }
    var selectedMealSlot by remember { mutableStateOf(MealSlot.LUNCH) }
    var mealFilter by remember { mutableStateOf(MealType.BOTH) }
    var selectedRecipeId by remember { mutableStateOf<String?>(null) }
    var editingEntry by remember { mutableStateOf<PlanEntry?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val datePickerState = rememberDatePickerState()
    val isoFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    val displayFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    val mealFilteredRecipes = recipes.filter { recipe ->
        when (mealFilter) {
            MealType.LUNCH -> recipe.mealType == MealType.LUNCH || recipe.mealType == MealType.BOTH
            MealType.DINNER -> recipe.mealType == MealType.DINNER || recipe.mealType == MealType.BOTH
            MealType.BOTH -> true
        }
    }
    val recentRecipes = mealFilteredRecipes.sortedByDescending { it.updatedAt }.take(5)
    val filteredRecipes = mealFilteredRecipes.filter { recipe ->
        searchQuery.isBlank() || recipe.name.contains(searchQuery, ignoreCase = true)
    }
    val selectedRecipe = recipes.firstOrNull { it.id == selectedRecipeId }

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

        OutlinedTextField(
            value = dateDisplay,
            onValueChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            label = { Text(text = stringResource(R.string.plan_date_label)) },
            readOnly = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = { showDatePicker = true }) {
            Text(text = stringResource(R.string.button_pick_date))
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                onClick = {
                    mealFilter = MealType.LUNCH
                    selectedMealSlot = MealSlot.LUNCH
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            SegmentedChoicePlan(
                text = stringResource(R.string.meal_dinner),
                selected = mealFilter == MealType.DINNER,
                onClick = {
                    mealFilter = MealType.DINNER
                    selectedMealSlot = MealSlot.DINNER
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (recentRecipes.isNotEmpty()) {
            Text(text = stringResource(R.string.recipe_recent_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                recentRecipes.forEach { recipe ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = recipe.name,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(onClick = { selectedRecipeId = recipe.id }) {
                                Text(text = stringResource(R.string.button_select))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.recipe_search_label)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(
                R.string.selected_recipe_label,
                selectedRecipe?.name ?: "-"
            ),
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
        ) {
            if (filteredRecipes.isEmpty()) {
                item {
                    Text(text = stringResource(R.string.recipe_search_empty))
                }
            } else {
                items(filteredRecipes, key = { it.id }) { recipe ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = recipe.name,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(onClick = { selectedRecipeId = recipe.id }) {
                                Text(text = stringResource(R.string.button_select))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            PrimaryButton(
                text = stringResource(R.string.button_save_only),
                onClick = {
                    val recipeId = selectedRecipeId
                    if (dateIso.isNotEmpty() && recipeId != null) {
                    if (editingEntry == null) {
                        viewModel.createPlanEntry(dateIso, selectedMealSlot, recipeId)
                    } else {
                        viewModel.updatePlanEntry(
                            editingEntry!!.copy(
                                date = dateIso,
                                mealSlot = selectedMealSlot,
                                recipeId = recipeId
                            )
                        )
                    }
                    dateIso = ""
                    dateDisplay = ""
                    selectedRecipeId = null
                    selectedMealSlot = MealSlot.LUNCH
                    mealFilter = MealType.BOTH
                    searchQuery = ""
                    editingEntry = null
                    }
                }
            )

            if (editingEntry != null) {
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = {
                    editingEntry = null
                    dateIso = ""
                    dateDisplay = ""
                    selectedRecipeId = null
                    selectedMealSlot = MealSlot.LUNCH
                    mealFilter = MealType.BOTH
                    searchQuery = ""
                }) {
                    Text(text = stringResource(R.string.button_cancel))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(planEntries, key = { it.id }) { entry ->
                val recipeName = recipes.firstOrNull { it.id == entry.recipeId }?.name ?: "-"
                val parsed = runCatching { LocalDate.parse(entry.date, isoFormatter) }.getOrNull()
                val dateLabel = parsed?.format(displayFormatter) ?: entry.date
                val mealLabel = if (entry.mealSlot == MealSlot.LUNCH) {
                    stringResource(R.string.meal_lunch)
                } else {
                    stringResource(R.string.meal_dinner)
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "$dateLabel - $mealLabel")
                        Text(text = stringResource(R.string.plan_recipe_label, recipeName))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            OutlinedButton(onClick = {
                                editingEntry = entry
                                dateIso = entry.date
                                val editParsed = runCatching {
                                    LocalDate.parse(entry.date, isoFormatter)
                                }.getOrNull()
                                dateDisplay = editParsed?.format(displayFormatter) ?: entry.date
                                selectedMealSlot = entry.mealSlot
                                mealFilter = when (entry.mealSlot) {
                                    MealSlot.LUNCH -> MealType.LUNCH
                                    MealSlot.DINNER -> MealType.DINNER
                                }
                                selectedRecipeId = entry.recipeId
                            }) {
                                Text(text = stringResource(R.string.button_edit))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(onClick = { viewModel.deletePlanEntry(entry.id) }) {
                                Text(text = stringResource(R.string.button_delete))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        dateIso = localDate.format(isoFormatter)
                        dateDisplay = localDate.format(displayFormatter)
                    }
                    showDatePicker = false
                }) {
                    Text(text = stringResource(R.string.button_apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = stringResource(R.string.button_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
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
