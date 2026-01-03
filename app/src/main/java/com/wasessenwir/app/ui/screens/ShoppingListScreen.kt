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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wasessenwir.app.R
import com.wasessenwir.app.data.model.ShoppingItem
import com.wasessenwir.app.data.model.ShoppingList
import com.wasessenwir.app.ui.AppViewModel
import com.wasessenwir.app.ui.components.UnitDropdown
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(viewModel: AppViewModel) {
    val shoppingLists by viewModel.shoppingLists.collectAsState()
    val activeHouseholdId by viewModel.activeHouseholdId.collectAsState()

    var selectedListId by remember { mutableStateOf<String?>(null) }
    var itemName by remember { mutableStateOf("") }
    var itemAmount by remember { mutableStateOf("") }
    var itemUnit by remember { mutableStateOf("g") }

    val isoFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    val displayFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    val sortedLists = remember(shoppingLists) { shoppingLists.sortedByDescending { it.createdAt } }

    LaunchedEffect(sortedLists) {
        if (sortedLists.isEmpty()) {
            selectedListId = null
        } else if (selectedListId == null || sortedLists.none { it.id == selectedListId }) {
            selectedListId = sortedLists.first().id
        }
    }

    val selectedList = sortedLists.firstOrNull { it.id == selectedListId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        if (activeHouseholdId == null) {
            Text(text = stringResource(R.string.needs_active_household))
            return
        }

        Text(text = stringResource(R.string.shopping_title), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (sortedLists.isEmpty()) {
            Text(text = stringResource(R.string.shopping_no_lists))
            return
        }

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            val label = selectedList?.let { listLabel(it, isoFormatter, displayFormatter) } ?: "-"
            OutlinedTextField(
                value = label,
                onValueChange = { },
                readOnly = true,
                label = { Text(text = stringResource(R.string.shopping_select_list_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                sortedLists.forEach { list ->
                    DropdownMenuItem(
                        text = { Text(text = listLabel(list, isoFormatter, displayFormatter)) },
                        onClick = {
                            selectedListId = list.id
                            expanded = false
                        }
                    )
                }
            }
        }

        if (selectedList == null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = stringResource(R.string.shopping_no_selection))
            return
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = stringResource(R.string.shopping_unit_hint), style = MaterialTheme.typography.bodySmall)

        val checkedItems = selectedList.items.filter { it.checked }
        if (checkedItems.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(R.string.shopping_summary_title), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    checkedItems.forEach { item ->
                        Text(text = formatItem(item))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = stringResource(R.string.shopping_items_title), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            itemsIndexed(selectedList.items) { index, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = formatItem(item),
                            modifier = Modifier.weight(1f)
                        )
                        Column(modifier = Modifier.padding(end = 8.dp)) {
                            Text(
                                text = stringResource(R.string.label_have_it),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Checkbox(
                                checked = item.haveIt,
                                onCheckedChange = { checked ->
                                    val updated = selectedList.items.toMutableList()
                                    updated[index] = item.copy(haveIt = checked)
                                    viewModel.updateShoppingList(selectedList.copy(items = updated))
                                }
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.label_checked),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Checkbox(
                                checked = item.checked,
                                onCheckedChange = { checked ->
                                    val updated = selectedList.items.toMutableList()
                                    updated[index] = item.copy(checked = checked)
                                    viewModel.updateShoppingList(selectedList.copy(items = updated))
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = stringResource(R.string.shopping_add_item_title), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = itemName,
            onValueChange = { itemName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.item_name_label)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            OutlinedTextField(
                value = itemAmount,
                onValueChange = { itemAmount = it },
                modifier = Modifier.weight(2f),
                label = { Text(text = stringResource(R.string.ingredient_amount_label)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            UnitDropdown(
                value = itemUnit,
                onValueChange = { itemUnit = it },
                label = stringResource(R.string.ingredient_unit_label),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = {
            val trimmed = itemName.trim()
            if (trimmed.isNotEmpty() && selectedList != null) {
                val amount = itemAmount.toDoubleOrNull() ?: 0.0
                val unit = itemUnit.trim()
                val existingIndex = selectedList.items.indexOfFirst {
                    it.name.equals(trimmed, ignoreCase = true) && it.unit.equals(unit, ignoreCase = true)
                }
                val updated = selectedList.items.toMutableList()
                if (existingIndex >= 0) {
                    val existing = updated[existingIndex]
                    updated[existingIndex] = existing.copy(amount = existing.amount + amount)
                } else {
                    updated.add(ShoppingItem(trimmed, amount, unit, haveIt = false, checked = false))
                }
                viewModel.updateShoppingList(selectedList.copy(items = updated))
                itemName = ""
                itemAmount = ""
                itemUnit = "g"
            }
        }) {
            Text(text = stringResource(R.string.button_add_item))
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row {
            OutlinedButton(onClick = { viewModel.deleteShoppingList(selectedList.id) }) {
                Text(text = stringResource(R.string.button_delete))
            }
        }
    }
}

private fun listLabel(
    list: ShoppingList,
    isoFormatter: DateTimeFormatter,
    displayFormatter: DateTimeFormatter
): String {
    val parsed = runCatching { LocalDate.parse(list.weekStart, isoFormatter) }.getOrNull()
    val weekLabel = parsed?.format(displayFormatter) ?: list.weekStart
    return "${weekLabel} (${list.items.size})"
}

private fun formatItem(item: ShoppingItem): String {
    val (displayAmount, displayUnit) = normalizeQuantity(item.amount, item.unit)
    return "${formatAmount(displayAmount)} ${displayUnit} ${item.name}"
}

private fun normalizeQuantity(amount: Double, unit: String): Pair<Double, String> {
    val lower = unit.trim().lowercase(Locale.US)
    return when (lower) {
        "g" -> if (amount >= 1000.0) amount / 1000.0 to "kg" else amount to "g"
        "kg" -> amount to "kg"
        "ml" -> if (amount >= 1000.0) amount / 1000.0 to "l" else amount to "ml"
        "l" -> amount to "l"
        else -> amount to unit
    }
}

private fun formatAmount(amount: Double): String {
    val rounded = String.format(Locale.US, "%.2f", amount)
    return rounded.trimEnd('0').trimEnd('.')
}
