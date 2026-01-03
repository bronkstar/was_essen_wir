package com.wasessenwir.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource
import com.wasessenwir.app.ui.AppViewModel
import androidx.compose.ui.unit.dp
import com.wasessenwir.app.R
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.padding
import com.wasessenwir.app.ui.theme.CyanPrimary
import com.wasessenwir.app.ui.theme.InactiveTab

@Composable
fun AppScreen(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        R.string.tab_households,
        R.string.tab_recipes,
        R.string.tab_plan,
        R.string.tab_shopping
    )
    val authError by viewModel.authError.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        if (authError != null) {
            Text(
                text = authError ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = CyanPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CyanPrimary
                )
            },
            modifier = Modifier.height(48.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = stringResource(title),
                            color = if (selectedTab == index) CyanPrimary else InactiveTab,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> HouseholdsScreen(viewModel)
            1 -> RecipesScreen(viewModel)
            2 -> PlanScreen(viewModel)
            else -> ShoppingListScreen(viewModel)
        }
    }
}
