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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun ShoppingListScreen(viewModel: AppViewModel) {
    val shoppingLists by viewModel.shoppingLists.collectAsState()
    val activeHouseholdId by viewModel.activeHouseholdId.collectAsState()

    var weekStart by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var itemAmount by remember { mutableStateOf("") }
    var itemUnit by remember { mutableStateOf("") }
    val draftItems = remember { mutableStateListOf<ShoppingItem>() }

    var editingList by remember { mutableStateOf<ShoppingList?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (activeHouseholdId == null) {
            Text(text = stringResource(R.string.needs_active_household))
            return
        }

        Text(text = stringResource(R.string.shopping_title), style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = weekStart,
            onValueChange = { weekStart = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.week_start_label)) }
        )

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

        Button(onClick = {
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
            Button(onClick = {
                val trimmedWeek = weekStart.trim()
                if (trimmedWeek.isNotEmpty()) {
                    if (editingList == null) {
                        viewModel.createShoppingList(trimmedWeek, draftItems.toList())
                    } else {
                        val existing = editingList!!
                        viewModel.updateShoppingList(
                            existing.copy(
                                weekStart = trimmedWeek,
                                items = draftItems.toList()
                            )
                        )
                    }
                    weekStart = ""
                    draftItems.clear()
                    editingList = null
                }
            }) {
                Text(
                    text = stringResource(
                        if (editingList == null) R.string.button_create else R.string.button_save
                    )
                )
            }

            if (editingList != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    editingList = null
                    weekStart = ""
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
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = stringResource(R.string.shopping_week_label, list.weekStart))
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
                            Button(onClick = {
                                editingList = list
                                weekStart = list.weekStart
                                draftItems.clear()
                                draftItems.addAll(list.items)
                            }) {
                                Text(text = stringResource(R.string.button_edit))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { viewModel.deleteShoppingList(list.id) }) {
                                Text(text = stringResource(R.string.button_delete))
                            }
                        }
                    }
                }
            }
        }
    }
}
