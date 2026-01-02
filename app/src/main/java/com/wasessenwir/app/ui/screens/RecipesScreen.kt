package com.wasessenwir.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wasessenwir.app.data.model.Ingredient
import com.wasessenwir.app.data.model.Recipe
import com.wasessenwir.app.ui.AppViewModel

@Composable
fun RecipesScreen(viewModel: AppViewModel) {
    val recipes by viewModel.recipes.collectAsState()
    val activeHouseholdId by viewModel.activeHouseholdId.collectAsState()

    var name by remember { mutableStateOf("") }
    var servingsText by remember { mutableStateOf("2") }
    var ingredientName by remember { mutableStateOf("") }
    var ingredientAmount by remember { mutableStateOf("") }
    var ingredientUnit by remember { mutableStateOf("") }
    val draftIngredients = remember { mutableStateListOf<Ingredient>() }

    var editingRecipe by remember { mutableStateOf<Recipe?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (activeHouseholdId == null) {
            Text(text = "Bitte zuerst ein Household aktiv setzen.")
            return
        }

        Text(text = "Recipe", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Name") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = servingsText,
            onValueChange = { servingsText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Servings") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Ingredients", style = MaterialTheme.typography.labelMedium)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = ingredientName,
            onValueChange = { ingredientName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Ingredient name") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            OutlinedTextField(
                value = ingredientAmount,
                onValueChange = { ingredientAmount = it },
                modifier = Modifier.weight(1f),
                label = { Text(text = "Amount") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = ingredientUnit,
                onValueChange = { ingredientUnit = it },
                modifier = Modifier.weight(1f),
                label = { Text(text = "Unit") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            val trimmed = ingredientName.trim()
            if (trimmed.isNotEmpty()) {
                val amount = ingredientAmount.toDoubleOrNull() ?: 0.0
                draftIngredients.add(Ingredient(trimmed, amount, ingredientUnit.trim()))
                ingredientName = ""
                ingredientAmount = ""
                ingredientUnit = ""
            }
        }) {
            Text(text = "Add ingredient")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (draftIngredients.isNotEmpty()) {
            draftIngredients.forEach { ingredient ->
                Text(text = "- ${ingredient.amount} ${ingredient.unit} ${ingredient.name}")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            Button(onClick = {
                val trimmed = name.trim()
                val servings = servingsText.toIntOrNull() ?: 0
                if (trimmed.isNotEmpty()) {
                    if (editingRecipe == null) {
                        viewModel.createRecipe(trimmed, servings, draftIngredients.toList())
                    } else {
                        val existing = editingRecipe!!
                        viewModel.updateRecipe(
                            existing.copy(
                                name = trimmed,
                                servings = servings,
                                ingredients = draftIngredients.toList()
                            )
                        )
                    }
                    name = ""
                    servingsText = "2"
                    draftIngredients.clear()
                    editingRecipe = null
                }
            }) {
                Text(text = if (editingRecipe == null) "Create" else "Save")
            }

            if (editingRecipe != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    editingRecipe = null
                    name = ""
                    servingsText = "2"
                    draftIngredients.clear()
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
            items(recipes, key = { it.id }) { recipe ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = recipe.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Servings: ${recipe.servings}")
                        Text(text = "Ingredients: ${recipe.ingredients.size}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Button(onClick = {
                                editingRecipe = recipe
                                name = recipe.name
                                servingsText = recipe.servings.toString()
                                draftIngredients.clear()
                                draftIngredients.addAll(recipe.ingredients)
                            }) {
                                Text(text = "Edit")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { viewModel.deleteRecipe(recipe.id) }) {
                                Text(text = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
