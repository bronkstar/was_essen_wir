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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wasessenwir.app.data.model.MealSlot
import com.wasessenwir.app.data.model.PlanEntry
import com.wasessenwir.app.ui.AppViewModel

@Composable
fun PlanScreen(viewModel: AppViewModel) {
    val planEntries by viewModel.planEntries.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val activeHouseholdId by viewModel.activeHouseholdId.collectAsState()

    var date by remember { mutableStateOf("") }
    var selectedMealSlot by remember { mutableStateOf(MealSlot.LUNCH) }
    var selectedRecipeId by remember { mutableStateOf<String?>(null) }
    var editingEntry by remember { mutableStateOf<PlanEntry?>(null) }

    val selectedRecipe = recipes.firstOrNull { it.id == selectedRecipeId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (activeHouseholdId == null) {
            Text(text = "Bitte zuerst ein Household aktiv setzen.")
            return
        }

        Text(text = "Plan entry", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Date (YYYY-MM-DD)") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Button(
                onClick = { selectedMealSlot = MealSlot.LUNCH },
                enabled = selectedMealSlot != MealSlot.LUNCH
            ) {
                Text(text = "Lunch")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { selectedMealSlot = MealSlot.DINNER },
                enabled = selectedMealSlot != MealSlot.DINNER
            ) {
                Text(text = "Dinner")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Selected recipe: ${selectedRecipe?.name ?: "-"}",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
        ) {
            items(recipes, key = { it.id }) { recipe ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = recipe.name,
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = { selectedRecipeId = recipe.id }) {
                            Text(text = "Select")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            Button(onClick = {
                val trimmedDate = date.trim()
                val recipeId = selectedRecipeId
                if (trimmedDate.isNotEmpty() && recipeId != null) {
                    if (editingEntry == null) {
                        viewModel.createPlanEntry(trimmedDate, selectedMealSlot, recipeId)
                    } else {
                        viewModel.updatePlanEntry(
                            editingEntry!!.copy(
                                date = trimmedDate,
                                mealSlot = selectedMealSlot,
                                recipeId = recipeId
                            )
                        )
                    }
                    date = ""
                    selectedRecipeId = null
                    selectedMealSlot = MealSlot.LUNCH
                    editingEntry = null
                }
            }) {
                Text(text = if (editingEntry == null) "Create" else "Save")
            }

            if (editingEntry != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    editingEntry = null
                    date = ""
                    selectedRecipeId = null
                    selectedMealSlot = MealSlot.LUNCH
                }) {
                    Text(text = "Cancel")
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
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "${entry.date} - ${entry.mealSlot}")
                        Text(text = "Recipe: $recipeName")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Button(onClick = {
                                editingEntry = entry
                                date = entry.date
                                selectedMealSlot = entry.mealSlot
                                selectedRecipeId = entry.recipeId
                            }) {
                                Text(text = "Edit")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { viewModel.deletePlanEntry(entry.id) }) {
                                Text(text = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
