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
import androidx.compose.foundation.clickable
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusEvent
import kotlinx.coroutines.launch
import com.wasessenwir.app.R
import com.wasessenwir.app.data.model.Ingredient
import com.wasessenwir.app.data.model.MealType
import com.wasessenwir.app.data.model.Recipe
import com.wasessenwir.app.ui.AppViewModel
import com.wasessenwir.app.ui.components.PrimaryButton
import com.wasessenwir.app.ui.components.UnitDropdown
import com.wasessenwir.app.ui.theme.CyanPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(viewModel: AppViewModel) {
    val recipes by viewModel.recipes.collectAsState()
    val activeHouseholdId by viewModel.activeHouseholdId.collectAsState()

    var name by remember { mutableStateOf("") }
    var servingsText by remember { mutableStateOf("2") }
    var mealType by remember { mutableStateOf(MealType.BOTH) }
    var ingredientName by remember { mutableStateOf("") }
    var ingredientAmount by remember { mutableStateOf("") }
    var ingredientUnit by remember { mutableStateOf("g") }
    val draftIngredients = remember { mutableStateListOf<Ingredient>() }

    var editingRecipe by remember { mutableStateOf<Recipe?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var listFilter by remember { mutableStateOf<MealType?>(null) }
    val focusScope = rememberCoroutineScope()
    val nameFocus = remember { BringIntoViewRequester() }
    val servingsFocus = remember { BringIntoViewRequester() }
    val ingredientNameFocus = remember { BringIntoViewRequester() }
    val ingredientAmountFocus = remember { BringIntoViewRequester() }
    val searchFocus = remember { BringIntoViewRequester() }

    val recentRecipes = recipes.sortedByDescending { it.updatedAt }.take(3)
    val ingredientDefaults = remember(recipes) {
        val byName = LinkedHashMap<String, Ingredient>()
        recipes.sortedByDescending { it.updatedAt }.forEach { recipe ->
            recipe.ingredients.forEach { ingredient ->
                val key = ingredient.name.trim().lowercase()
                if (key.isNotEmpty() && !byName.containsKey(key)) {
                    byName[key] = ingredient
                }
            }
        }
        byName
    }
    val ingredientSuggestions = if (ingredientName.trim().length >= 2) {
        val query = ingredientName.trim()
        ingredientDefaults.values
            .filter { matchesIngredientQuery(it.name, query) }
            .take(6)
    } else {
        emptyList()
    }
    val filteredRecipes = recipes.filter { recipe ->
        val matchesQuery = searchQuery.isBlank() || recipe.name.contains(searchQuery, ignoreCase = true) ||
            recipe.ingredients.any { it.name.contains(searchQuery, ignoreCase = true) }
        val matchesFilter = listFilter == null || recipe.mealType == listFilter
        matchesQuery && matchesFilter
    }

    if (activeHouseholdId == null) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text(text = stringResource(R.string.needs_active_household))
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = stringResource(R.string.recipe_create_title), style = MaterialTheme.typography.titleMedium)
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(nameFocus)
                    .onFocusEvent { state ->
                        if (state.isFocused) {
                            focusScope.launch { nameFocus.bringIntoView() }
                        }
                    },
                label = { Text(text = stringResource(R.string.recipe_name_label)) }
            )
        }

        item {
            OutlinedTextField(
                value = servingsText,
                onValueChange = { servingsText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(servingsFocus)
                    .onFocusEvent { state ->
                        if (state.isFocused) {
                            focusScope.launch { servingsFocus.bringIntoView() }
                        }
                    },
                label = { Text(text = stringResource(R.string.recipe_servings_label)) }
            )
        }

        item {
            Text(text = stringResource(R.string.recipe_meal_type_label), style = MaterialTheme.typography.labelMedium)
        }

        item {
            Row {
                SegmentedChoice(
                    text = stringResource(R.string.recipe_meal_lunch),
                    selected = mealType == MealType.LUNCH,
                    onClick = { mealType = MealType.LUNCH }
                )
                Spacer(modifier = Modifier.width(8.dp))
                SegmentedChoice(
                    text = stringResource(R.string.recipe_meal_dinner),
                    selected = mealType == MealType.DINNER,
                    onClick = { mealType = MealType.DINNER }
                )
                Spacer(modifier = Modifier.width(8.dp))
                SegmentedChoice(
                    text = stringResource(R.string.recipe_meal_both),
                    selected = mealType == MealType.BOTH,
                    onClick = { mealType = MealType.BOTH }
                )
            }
        }

        item {
            Text(text = stringResource(R.string.ingredients_title), style = MaterialTheme.typography.labelMedium)
        }

        item {
            Column {
                if (ingredientSuggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            ingredientSuggestions.forEach { suggestion ->
                                Text(
                                    text = suggestion.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            ingredientName = suggestion.name
                                            ingredientUnit = suggestion.unit
                                            if (suggestion.amount > 0.0) {
                                                ingredientAmount = suggestion.amount.toString()
                                            }
                                        }
                                        .padding(vertical = 8.dp, horizontal = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = ingredientName,
                    onValueChange = { ingredientName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(ingredientNameFocus)
                        .onFocusEvent { state ->
                            if (state.isFocused) {
                                focusScope.launch { ingredientNameFocus.bringIntoView() }
                            }
                        },
                    label = { Text(text = stringResource(R.string.ingredient_name_label)) }
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = ingredientAmount,
                    onValueChange = { ingredientAmount = it },
                    modifier = Modifier
                        .weight(1f)
                        .bringIntoViewRequester(ingredientAmountFocus)
                        .onFocusEvent { state ->
                            if (state.isFocused) {
                                focusScope.launch { ingredientAmountFocus.bringIntoView() }
                            }
                        },
                    label = {
                        Text(
                            text = stringResource(R.string.ingredient_amount_label),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                UnitDropdown(
                    value = ingredientUnit,
                    onValueChange = { ingredientUnit = it },
                    label = stringResource(R.string.ingredient_unit_label),
                    modifier = Modifier.width(120.dp)
                )
            }
        }

        item {
            OutlinedButton(onClick = {
                val trimmed = ingredientName.trim()
                if (trimmed.isNotEmpty()) {
                    val amount = ingredientAmount.toDoubleOrNull() ?: 0.0
                    draftIngredients.add(Ingredient(trimmed, amount, ingredientUnit.trim()))
                    ingredientName = ""
                    ingredientAmount = ""
                    ingredientUnit = "g"
                }
            }) {
                Text(text = stringResource(R.string.button_add_ingredient))
            }
        }

        if (draftIngredients.isNotEmpty()) {
            items(draftIngredients) { ingredient ->
                Text(text = "- ${ingredient.amount} ${ingredient.unit} ${ingredient.name}")
            }
        }

        item {
            Row {
                PrimaryButton(
                    text = stringResource(R.string.button_save_recipe),
                    onClick = {
                        val trimmed = name.trim()
                        val servings = servingsText.toIntOrNull() ?: 0
                        if (trimmed.isNotEmpty()) {
                            if (editingRecipe == null) {
                                viewModel.createRecipe(trimmed, servings, draftIngredients.toList(), mealType)
                            } else {
                                val existing = editingRecipe!!
                                viewModel.updateRecipe(
                                    existing.copy(
                                        name = trimmed,
                                        servings = servings,
                                        ingredients = draftIngredients.toList(),
                                        mealType = mealType
                                    )
                                )
                            }
                            name = ""
                            servingsText = "2"
                            draftIngredients.clear()
                            mealType = MealType.BOTH
                            editingRecipe = null
                        }
                    }
                )

                if (editingRecipe != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = {
                        editingRecipe = null
                        name = ""
                        servingsText = "2"
                        draftIngredients.clear()
                        mealType = MealType.BOTH
                    }) {
                        Text(text = stringResource(R.string.button_cancel))
                    }
                }
            }
        }

        if (recentRecipes.isNotEmpty()) {
            item {
                Text(text = stringResource(R.string.recipe_recent_title), style = MaterialTheme.typography.titleMedium)
            }
            items(recentRecipes) { recipe ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = recipe.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = stringResource(R.string.recipe_servings_value, recipe.servings))
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = {
                            editingRecipe = recipe
                            name = recipe.name
                            servingsText = recipe.servings.toString()
                            draftIngredients.clear()
                            draftIngredients.addAll(recipe.ingredients)
                            mealType = recipe.mealType
                        }) {
                            Text(text = stringResource(R.string.button_edit))
                        }
                    }
                }
            }
        }

        item {
            Text(text = stringResource(R.string.recipe_list_title), style = MaterialTheme.typography.titleMedium)
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(searchFocus)
                    .onFocusEvent { state ->
                        if (state.isFocused) {
                            focusScope.launch { searchFocus.bringIntoView() }
                        }
                    },
                label = { Text(text = stringResource(R.string.recipe_search_label)) }
            )
        }

        item {
            Row {
                SegmentedChoice(
                    text = stringResource(R.string.recipe_filter_all),
                    selected = listFilter == null,
                    onClick = { listFilter = null }
                )
                Spacer(modifier = Modifier.width(8.dp))
                SegmentedChoice(
                    text = stringResource(R.string.recipe_meal_lunch),
                    selected = listFilter == MealType.LUNCH,
                    onClick = { listFilter = MealType.LUNCH }
                )
                Spacer(modifier = Modifier.width(8.dp))
                SegmentedChoice(
                    text = stringResource(R.string.recipe_meal_dinner),
                    selected = listFilter == MealType.DINNER,
                    onClick = { listFilter = MealType.DINNER }
                )
            }
        }

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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = recipe.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = stringResource(R.string.recipe_servings_value, recipe.servings))
                        Text(
                            text = stringResource(
                                R.string.recipe_ingredients_count,
                                recipe.ingredients.size
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            OutlinedButton(onClick = {
                                editingRecipe = recipe
                                name = recipe.name
                                servingsText = recipe.servings.toString()
                                draftIngredients.clear()
                                draftIngredients.addAll(recipe.ingredients)
                                mealType = recipe.mealType
                            }) {
                                Text(text = stringResource(R.string.button_edit))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(onClick = { viewModel.deleteRecipe(recipe.id) }) {
                                Text(text = stringResource(R.string.button_delete))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentedChoice(
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

private fun matchesIngredientQuery(name: String, query: String): Boolean {
    val normalizedName = name.lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()
    val normalizedQuery = query.lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()
    if (normalizedQuery.isEmpty()) {
        return true
    }
    val nameTokens = normalizedName.split(Regex("\\s+"))
    val queryTokens = normalizedQuery.split(Regex("\\s+"))
    return queryTokens.all { q ->
        nameTokens.any { token -> token.contains(q) }
    }
}
