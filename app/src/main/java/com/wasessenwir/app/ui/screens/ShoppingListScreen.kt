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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wasessenwir.app.data.model.ShoppingItem
import com.wasessenwir.app.data.model.ShoppingList
import com.wasessenwir.app.ui.AppViewModel
import com.wasessenwir.app.R
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.wasessenwir.app.ui.components.PrimaryButton
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(viewModel: AppViewModel) {
    val shoppingLists by viewModel.shoppingLists.collectAsState()
    val planEntries by viewModel.planEntries.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val activeHouseholdId by viewModel.activeHouseholdId.collectAsState()

    var weekStartIso by remember { mutableStateOf("") }
    var weekStartDisplay by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var itemAmount by remember { mutableStateOf("") }
    var itemUnit by remember { mutableStateOf("") }
    val draftItems = remember { mutableStateListOf<ShoppingItem>() }

    var editingList by remember { mutableStateOf<ShoppingList?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val isoFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    val displayFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

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

        OutlinedTextField(
            value = weekStartDisplay,
            onValueChange = { },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.week_start_label)) },
            readOnly = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            OutlinedButton(onClick = { showDatePicker = true }) {
                Text(text = stringResource(R.string.button_pick_date))
            }
            Spacer(modifier = Modifier.width(8.dp))
            PrimaryButton(
                text = stringResource(R.string.button_generate_from_plan),
                onClick = {
                    if (weekStartIso.isNotEmpty()) {
                        viewModel.createShoppingListFromPlan(weekStartIso, planEntries, recipes)
                    }
                }
            )
        }

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
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(R.string.ingredient_amount_label)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = itemUnit,
                onValueChange = { itemUnit = it },
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(R.string.ingredient_unit_label)) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = {
            val trimmed = itemName.trim()
            if (trimmed.isNotEmpty()) {
                val amount = itemAmount.toDoubleOrNull() ?: 0.0
                draftItems.add(ShoppingItem(trimmed, amount, itemUnit.trim(), haveIt = false, checked = false))
                itemName = ""
                itemAmount = ""
                itemUnit = ""
            }
        }) {
            Text(text = stringResource(R.string.button_add_item))
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (draftItems.isNotEmpty()) {
            draftItems.forEach { item ->
                Text(text = "- ${item.amount} ${item.unit} ${item.name}")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            PrimaryButton(
                text = stringResource(
                    if (editingList == null) R.string.button_create else R.string.button_save
                ),
                onClick = {
                    if (weekStartIso.isNotEmpty()) {
                    if (editingList == null) {
                        viewModel.createShoppingList(weekStartIso, draftItems.toList())
                    } else {
                        val existing = editingList!!
                        viewModel.updateShoppingList(
                            existing.copy(
                                weekStart = weekStartIso,
                                items = draftItems.toList()
                            )
                        )
                    }
                    weekStartIso = ""
                    weekStartDisplay = ""
                    draftItems.clear()
                    editingList = null
                    }
                }
            )

            if (editingList != null) {
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = {
                    editingList = null
                    weekStartIso = ""
                    weekStartDisplay = ""
                    draftItems.clear()
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
            items(shoppingLists, key = { it.id }) { list ->
                val parsed = runCatching { LocalDate.parse(list.weekStart, isoFormatter) }.getOrNull()
                val weekLabel = parsed?.format(displayFormatter) ?: list.weekStart
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = stringResource(R.string.shopping_week_label, weekLabel))
                        Spacer(modifier = Modifier.height(8.dp))
                        list.items.forEachIndexed { index, item ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "${item.amount} ${item.unit} ${item.name}",
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
                                            val updated = list.items.toMutableList()
                                            updated[index] = item.copy(haveIt = checked)
                                            viewModel.updateShoppingList(list.copy(items = updated))
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
                                            val updated = list.items.toMutableList()
                                            updated[index] = item.copy(checked = checked)
                                            viewModel.updateShoppingList(list.copy(items = updated))
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            OutlinedButton(onClick = {
                                editingList = list
                                weekStartIso = list.weekStart
                                weekStartDisplay = weekLabel
                                draftItems.clear()
                                draftItems.addAll(list.items)
                            }) {
                                Text(text = stringResource(R.string.button_edit))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(onClick = { viewModel.deleteShoppingList(list.id) }) {
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
                        weekStartIso = localDate.format(isoFormatter)
                        weekStartDisplay = localDate.format(displayFormatter)
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
