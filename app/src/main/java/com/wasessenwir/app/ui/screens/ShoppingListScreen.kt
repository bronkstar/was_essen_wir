package com.wasessenwir.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(viewModel: AppViewModel) {
    val shoppingLists by viewModel.shoppingLists.collectAsState()
    val activeHouseholdId by viewModel.activeHouseholdId.collectAsState()

    var selectedListId by remember { mutableStateOf<String?>(null) }

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

    if (activeHouseholdId == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(text = stringResource(R.string.needs_active_household))
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = stringResource(R.string.shopping_title), style = MaterialTheme.typography.titleMedium)
        }

        if (sortedLists.isEmpty()) {
            item {
                Text(text = stringResource(R.string.shopping_no_lists))
            }
            return@LazyColumn
        }

        item {
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
        }

        if (selectedList == null) {
            item {
                Text(text = stringResource(R.string.shopping_no_selection))
            }
            return@LazyColumn
        }

        item {
            Text(text = stringResource(R.string.shopping_unit_hint), style = MaterialTheme.typography.bodySmall)
        }

        item {
            Text(text = stringResource(R.string.shopping_items_title), style = MaterialTheme.typography.titleMedium)
        }

        itemsIndexed(selectedList.items.take(3)) { _, item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Text(text = formatItem(item))
                }
            }
        }

        if (selectedList.items.size > 3) {
            item {
                Text(text = stringResource(R.string.shopping_more_items, selectedList.items.size - 3))
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = stringResource(R.string.shopping_summary_title), style = MaterialTheme.typography.titleMedium)
        }

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

        item {
            Row {
                OutlinedButton(onClick = { viewModel.deleteShoppingList(selectedList.id) }) {
                    Text(text = stringResource(R.string.button_delete))
                }
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
