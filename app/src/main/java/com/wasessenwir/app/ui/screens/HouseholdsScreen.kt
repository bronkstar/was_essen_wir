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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wasessenwir.app.ui.AppViewModel
import com.wasessenwir.app.R

@Composable
fun HouseholdsScreen(viewModel: AppViewModel) {
    val households by viewModel.households.collectAsState()
    val activeHouseholdId by viewModel.activeHouseholdId.collectAsState()

    var name by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(
                R.string.active_household,
                activeHouseholdId ?: "-"
            ),
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    text = stringResource(
                        if (editingId == null) {
                            R.string.household_name_label
                        } else {
                            R.string.household_rename_label
                        }
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Button(
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isNotEmpty()) {
                        if (editingId == null) {
                            viewModel.createHousehold(trimmed)
                        } else {
                            viewModel.updateHouseholdName(editingId!!, trimmed)
                        }
                        name = ""
                        editingId = null
                    }
                }
            ) {
                Text(
                    text = stringResource(
                        if (editingId == null) R.string.button_create else R.string.button_save
                    )
                )
            }

            if (editingId != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    editingId = null
                    name = ""
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
            items(households, key = { it.id }) { household ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = household.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = stringResource(R.string.household_id_label, household.id),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = stringResource(
                                R.string.household_members_label,
                                household.members.size
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Button(onClick = { viewModel.setActiveHousehold(household.id) }) {
                                Text(text = stringResource(R.string.button_set_active))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                editingId = household.id
                                name = household.name
                            }) {
                                Text(text = stringResource(R.string.button_edit))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { viewModel.deleteHousehold(household.id) }) {
                                Text(text = stringResource(R.string.button_delete))
                            }
                        }
                    }
                }
            }
        }
    }
}
