package com.wasessenwir.app.data.repo

import com.google.firebase.firestore.FirebaseFirestore
import com.wasessenwir.app.data.model.Household
import com.wasessenwir.app.data.model.Ingredient
import com.wasessenwir.app.data.model.MealSlot
import com.wasessenwir.app.data.model.PlanEntry
import com.wasessenwir.app.data.model.Recipe
import com.wasessenwir.app.data.model.ShoppingItem
import com.wasessenwir.app.data.model.ShoppingList
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val households = firestore.collection("households")
    private val recipes = firestore.collection("recipes")
    private val planEntries = firestore.collection("planEntries")
    private val shoppingLists = firestore.collection("shoppingLists")

    suspend fun createHousehold(name: String, userId: String): String {
        val now = System.currentTimeMillis()
        val doc = households.document()
        val data = mapOf(
            "name" to name,
            "members" to listOf(userId),
            "createdAt" to now,
            "updatedAt" to now
        )
        doc.set(data).await()
        return doc.id
    }

    suspend fun updateHouseholdName(householdId: String, name: String) {
        households.document(householdId)
            .update(mapOf("name" to name, "updatedAt" to System.currentTimeMillis()))
            .await()
    }

    suspend fun deleteHousehold(householdId: String) {
        households.document(householdId).delete().await()
    }

    fun observeHouseholds(userId: String): Flow<List<Household>> = callbackFlow {
        val registration = households
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val members = doc.get("members") as? List<*> ?: emptyList<String>()
                    val createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: 0L
                    val updatedAt = (doc.get("updatedAt") as? Number)?.toLong() ?: 0L
                    Household(
                        id = doc.id,
                        name = name,
                        members = members.filterIsInstance<String>(),
                        createdAt = createdAt,
                        updatedAt = updatedAt
                    )
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun createRecipe(householdId: String, name: String, servings: Int, ingredients: List<Ingredient>): String {
        val now = System.currentTimeMillis()
        val doc = recipes.document()
        val data = mapOf(
            "householdId" to householdId,
            "name" to name,
            "servings" to servings,
            "ingredients" to ingredients.map { ingredientToMap(it) },
            "createdAt" to now,
            "updatedAt" to now
        )
        doc.set(data).await()
        return doc.id
    }

    suspend fun updateRecipe(recipe: Recipe) {
        val data = mapOf(
            "householdId" to recipe.householdId,
            "name" to recipe.name,
            "servings" to recipe.servings,
            "ingredients" to recipe.ingredients.map { ingredientToMap(it) },
            "updatedAt" to System.currentTimeMillis()
        )
        recipes.document(recipe.id).update(data).await()
    }

    suspend fun deleteRecipe(recipeId: String) {
        recipes.document(recipeId).delete().await()
    }

    fun observeRecipes(householdId: String): Flow<List<Recipe>> = callbackFlow {
        val registration = recipes
            .whereEqualTo("householdId", householdId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val servings = (doc.get("servings") as? Number)?.toInt() ?: 0
                    val ingredients = (doc.get("ingredients") as? List<*>)
                        ?.mapNotNull { it as? Map<*, *> }
                        ?.map { mapToIngredient(it) }
                        ?: emptyList()
                    val createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: 0L
                    val updatedAt = (doc.get("updatedAt") as? Number)?.toLong() ?: 0L
                    val household = doc.getString("householdId") ?: householdId
                    Recipe(
                        id = doc.id,
                        householdId = household,
                        name = name,
                        servings = servings,
                        ingredients = ingredients,
                        createdAt = createdAt,
                        updatedAt = updatedAt
                    )
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun createPlanEntry(householdId: String, date: String, mealSlot: MealSlot, recipeId: String): String {
        val doc = planEntries.document()
        val data = mapOf(
            "householdId" to householdId,
            "date" to date,
            "mealSlot" to mealSlot.name,
            "recipeId" to recipeId
        )
        doc.set(data).await()
        return doc.id
    }

    suspend fun updatePlanEntry(planEntry: PlanEntry) {
        val data = mapOf(
            "householdId" to planEntry.householdId,
            "date" to planEntry.date,
            "mealSlot" to planEntry.mealSlot.name,
            "recipeId" to planEntry.recipeId
        )
        planEntries.document(planEntry.id).update(data).await()
    }

    suspend fun deletePlanEntry(planEntryId: String) {
        planEntries.document(planEntryId).delete().await()
    }

    fun observePlanEntries(householdId: String): Flow<List<PlanEntry>> = callbackFlow {
        val registration = planEntries
            .whereEqualTo("householdId", householdId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    val date = doc.getString("date") ?: return@mapNotNull null
                    val recipeId = doc.getString("recipeId") ?: return@mapNotNull null
                    val mealSlotRaw = doc.getString("mealSlot") ?: MealSlot.LUNCH.name
                    val mealSlot = parseMealSlot(mealSlotRaw)
                    val household = doc.getString("householdId") ?: householdId
                    PlanEntry(
                        id = doc.id,
                        householdId = household,
                        date = date,
                        mealSlot = mealSlot,
                        recipeId = recipeId
                    )
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun createShoppingList(householdId: String, weekStart: String, items: List<ShoppingItem>): String {
        val now = System.currentTimeMillis()
        val doc = shoppingLists.document()
        val data = mapOf(
            "householdId" to householdId,
            "weekStart" to weekStart,
            "items" to items.map { shoppingItemToMap(it) },
            "createdAt" to now
        )
        doc.set(data).await()
        return doc.id
    }

    suspend fun updateShoppingList(shoppingList: ShoppingList) {
        val data = mapOf(
            "householdId" to shoppingList.householdId,
            "weekStart" to shoppingList.weekStart,
            "items" to shoppingList.items.map { shoppingItemToMap(it) }
        )
        shoppingLists.document(shoppingList.id).update(data).await()
    }

    suspend fun deleteShoppingList(shoppingListId: String) {
        shoppingLists.document(shoppingListId).delete().await()
    }

    fun observeShoppingLists(householdId: String): Flow<List<ShoppingList>> = callbackFlow {
        val registration = shoppingLists
            .whereEqualTo("householdId", householdId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    val weekStart = doc.getString("weekStart") ?: return@mapNotNull null
                    val listItems = (doc.get("items") as? List<*>)
                        ?.mapNotNull { it as? Map<*, *> }
                        ?.map { mapToShoppingItem(it) }
                        ?: emptyList()
                    val createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: 0L
                    val household = doc.getString("householdId") ?: householdId
                    ShoppingList(
                        id = doc.id,
                        householdId = household,
                        weekStart = weekStart,
                        items = listItems,
                        createdAt = createdAt
                    )
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    private fun ingredientToMap(ingredient: Ingredient): Map<String, Any> = mapOf(
        "name" to ingredient.name,
        "amount" to ingredient.amount,
        "unit" to ingredient.unit
    )

    private fun mapToIngredient(map: Map<*, *>): Ingredient {
        val name = map["name"] as? String ?: ""
        val amount = (map["amount"] as? Number)?.toDouble() ?: 0.0
        val unit = map["unit"] as? String ?: ""
        return Ingredient(name = name, amount = amount, unit = unit)
    }

    private fun shoppingItemToMap(item: ShoppingItem): Map<String, Any> = mapOf(
        "name" to item.name,
        "amount" to item.amount,
        "unit" to item.unit,
        "haveIt" to item.haveIt,
        "checked" to item.checked
    )

    private fun mapToShoppingItem(map: Map<*, *>): ShoppingItem {
        val name = map["name"] as? String ?: ""
        val amount = (map["amount"] as? Number)?.toDouble() ?: 0.0
        val unit = map["unit"] as? String ?: ""
        val haveIt = map["haveIt"] as? Boolean ?: false
        val checked = map["checked"] as? Boolean ?: false
        return ShoppingItem(name = name, amount = amount, unit = unit, haveIt = haveIt, checked = checked)
    }

    private fun parseMealSlot(value: String): MealSlot = try {
        MealSlot.valueOf(value)
    } catch (ex: IllegalArgumentException) {
        MealSlot.LUNCH
    }
}
