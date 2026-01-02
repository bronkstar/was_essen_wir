package com.wasessenwir.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.wasessenwir.app.data.model.Household
import com.wasessenwir.app.data.model.Ingredient
import com.wasessenwir.app.data.model.MealSlot
import com.wasessenwir.app.data.model.PlanEntry
import com.wasessenwir.app.data.model.Recipe
import com.wasessenwir.app.data.model.ShoppingItem
import com.wasessenwir.app.data.model.ShoppingList
import com.wasessenwir.app.data.repo.FirestoreRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(
    private val repository: FirestoreRepository = FirestoreRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    private val _activeHouseholdId = MutableStateFlow<String?>(null)
    val activeHouseholdId: StateFlow<String?> = _activeHouseholdId

    val households: StateFlow<List<Household>> = _currentUserId
        .filterNotNull()
        .flatMapLatest { repository.observeHouseholds(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recipes: StateFlow<List<Recipe>> = _activeHouseholdId
        .filterNotNull()
        .flatMapLatest { repository.observeRecipes(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val planEntries: StateFlow<List<PlanEntry>> = _activeHouseholdId
        .filterNotNull()
        .flatMapLatest { repository.observePlanEntries(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val shoppingLists: StateFlow<List<ShoppingList>> = _activeHouseholdId
        .filterNotNull()
        .flatMapLatest { repository.observeShoppingLists(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            ensureSignedIn()
        }
    }

    fun setActiveHousehold(id: String?) {
        _activeHouseholdId.value = id
    }

    fun createHousehold(name: String) {
        val userId = _currentUserId.value ?: return
        viewModelScope.launch {
            val householdId = repository.createHousehold(name, userId)
            _activeHouseholdId.value = householdId
        }
    }

    fun updateHouseholdName(householdId: String, name: String) {
        viewModelScope.launch {
            repository.updateHouseholdName(householdId, name)
        }
    }

    fun deleteHousehold(householdId: String) {
        viewModelScope.launch {
            repository.deleteHousehold(householdId)
            if (_activeHouseholdId.value == householdId) {
                _activeHouseholdId.value = null
            }
        }
    }

    fun createRecipe(name: String, servings: Int, ingredients: List<Ingredient>) {
        val householdId = _activeHouseholdId.value ?: return
        viewModelScope.launch {
            repository.createRecipe(householdId, name, servings, ingredients)
        }
    }

    fun updateRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.updateRecipe(recipe)
        }
    }

    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            repository.deleteRecipe(recipeId)
        }
    }

    fun createPlanEntry(date: String, mealSlot: MealSlot, recipeId: String) {
        val householdId = _activeHouseholdId.value ?: return
        viewModelScope.launch {
            repository.createPlanEntry(householdId, date, mealSlot, recipeId)
        }
    }

    fun updatePlanEntry(planEntry: PlanEntry) {
        viewModelScope.launch {
            repository.updatePlanEntry(planEntry)
        }
    }

    fun deletePlanEntry(planEntryId: String) {
        viewModelScope.launch {
            repository.deletePlanEntry(planEntryId)
        }
    }

    fun createShoppingList(weekStart: String, items: List<ShoppingItem>) {
        val householdId = _activeHouseholdId.value ?: return
        viewModelScope.launch {
            repository.createShoppingList(householdId, weekStart, items)
        }
    }

    fun updateShoppingList(shoppingList: ShoppingList) {
        viewModelScope.launch {
            repository.updateShoppingList(shoppingList)
        }
    }

    fun deleteShoppingList(shoppingListId: String) {
        viewModelScope.launch {
            repository.deleteShoppingList(shoppingListId)
        }
    }

    private suspend fun ensureSignedIn() {
        try {
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
            }
            _currentUserId.value = auth.currentUser?.uid
            _authError.value = null
        } catch (ex: Exception) {
            _authError.value = "Firebase Auth fehlgeschlagen: ${ex.message ?: ex::class.java.simpleName}"
        }
    }
}
