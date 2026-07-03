package com.example.recipeswap.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeswap.data.model.Ingredient
import com.example.recipeswap.data.model.Recipe
import com.example.recipeswap.data.repository.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeViewModel : ViewModel() {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filteredRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val filteredRecipes: StateFlow<List<Recipe>> = _filteredRecipes

    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe

    private val _portions = MutableStateFlow(2)
    val portions: StateFlow<Int> = _portions

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail

    private val _userPassword = MutableStateFlow("")
    val userPassword: StateFlow<String> = _userPassword

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _favoriteRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val favoriteRecipes: StateFlow<List<Recipe>> = _favoriteRecipes

    init {
        fetchRecipesFromApi()
    }

    private fun fetchRecipesFromApi() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = RetrofitClient.apiService.getRecipes()

                val mappedRecipes = response.meals.map { mealDto ->
                    Recipe(
                        id = mealDto.idMeal,
                        title = mealDto.strMeal,
                        description = "Delicious student recipe: ${mealDto.strMeal}",
                        imageUrl = mealDto.strMealThumb,
                        ingredients = emptyList(),
                        instructions = emptyList()
                    )
                }
                _recipes.value = mappedRecipes
                updateFilteredRecipes()
            } catch (e: Exception) {
                _recipes.value = emptyList()
                updateFilteredRecipes()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        updateFilteredRecipes()
    }

    private fun updateFilteredRecipes() {
        val query = _searchQuery.value.lowercase()
        _filteredRecipes.value = if (query.isEmpty()) {
            _recipes.value
        } else {
            _recipes.value.filter {
                it.title.lowercase().contains(query)
            }
        }
    }

    fun selectRecipe(recipe: Recipe) {
        _selectedRecipe.value = recipe
        _portions.value = 2
        fetchRecipeDetails(recipe.id)
    }

    private fun fetchRecipeDetails(recipeId: String) {
        viewModelScope.launch {
            try {
                _error.value = null
                val response = RetrofitClient.apiService.getRecipeDetails(recipeId)
                val meal = response.meals?.firstOrNull() ?: return@launch

                val ingredientsList = mutableListOf<Ingredient>()
                
                val apiIngredients = listOf(
                    meal.strIngredient1 to meal.strMeasure1,
                    meal.strIngredient2 to meal.strMeasure2,
                    meal.strIngredient3 to meal.strMeasure3,
                    meal.strIngredient4 to meal.strMeasure4,
                    meal.strIngredient5 to meal.strMeasure5,
                    meal.strIngredient6 to meal.strMeasure6,
                    meal.strIngredient7 to meal.strMeasure7,
                    meal.strIngredient8 to meal.strMeasure8,
                    meal.strIngredient9 to meal.strMeasure9,
                    meal.strIngredient10 to meal.strMeasure10,
                    meal.strIngredient11 to meal.strMeasure11,
                    meal.strIngredient12 to meal.strMeasure12,
                    meal.strIngredient13 to meal.strMeasure13,
                    meal.strIngredient14 to meal.strMeasure14,
                    meal.strIngredient15 to meal.strMeasure15,
                    meal.strIngredient16 to meal.strMeasure16,
                    meal.strIngredient17 to meal.strMeasure17,
                    meal.strIngredient18 to meal.strMeasure18,
                    meal.strIngredient19 to meal.strMeasure19,
                    meal.strIngredient20 to meal.strMeasure20,
                )

                for ((name, measure) in apiIngredients) {
                    if (!name.isNullOrBlank()) {
                        val (qty, unit) = parseMeasure(measure)
                        ingredientsList.add(Ingredient(name, qty, unit))
                    }
                }

                val instructionList = meal.strInstructions
                    ?.split(Regex("""(?:\r?\n)+|(?<=[.!?])\s+(?=[A-Z])"""))
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?.map { step ->
                        step.replaceFirst(Regex("""^(?i:step\s*\d+[:.]?\s*|\d+[:.]?\s*)"""), "").trim()
                    }
                    ?.filter { it.isNotEmpty() } ?: emptyList()

                _selectedRecipe.value = _selectedRecipe.value?.copy(
                    instructions = instructionList,
                    ingredients = ingredientsList
                )
            } catch (e: Exception) {
                _error.value = "Failed to load recipe details"
                e.printStackTrace()
            }
        }
    }

    private fun parseMeasure(measure: String?): Pair<Double, String> {
        if (measure.isNullOrBlank()) return 0.5 to ""
        val trimmed = measure.trim().lowercase()

        val regex = """^(\d+\s+)?(\d+/\d+|\d+(\.\d+)?)(.*)$""".toRegex()
        val match = regex.find(trimmed)

        if (match != null) {
            val wholePartStr = match.groupValues[1].trim()
            val wholePart = wholePartStr.toDoubleOrNull() ?: 0.0
            val fractionOrDecimal = match.groupValues[2]
            val unit = match.groupValues[4].trim()

            val value = if (fractionOrDecimal.contains('/')) {
                val parts = fractionOrDecimal.split('/')
                val numerator = parts[0].toDoubleOrNull() ?: 0.0
                val denominator = parts[1].toDoubleOrNull() ?: 1.0
                numerator / denominator
            } else {
                fractionOrDecimal.toDoubleOrNull() ?: 0.0
            }

            val finalQty = if (wholePart + value > 0) (wholePart + value) / 2.0 else 0.5
            return finalQty to unit
        }

        return 0.5 to trimmed
    }

    fun incrementPortions() {
        if (_portions.value < 20) _portions.value += 1
    }

    fun decrementPortions() {
        if (_portions.value > 1) _portions.value -= 1
    }

    fun login(name: String, email: String, password: String) {
        _userName.value = name
        _userEmail.value = email
        _userPassword.value = password
        _isLoggedIn.value = true
    }

    fun logout() {
        _isLoggedIn.value = false
        _userName.value = ""
        _userEmail.value = ""
        _userPassword.value = ""
    }

    fun toggleFavorite(recipe: Recipe) {
        if (_favoriteRecipes.value.any { it.id == recipe.id }) {
            _favoriteRecipes.value = _favoriteRecipes.value.filter { it.id != recipe.id }
        } else {
            _favoriteRecipes.value = _favoriteRecipes.value + recipe
        }
    }
}