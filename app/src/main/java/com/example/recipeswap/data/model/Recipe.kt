package com.example.recipeswap.data.model

data class Ingredient(
    val name: String,
    val baseQuantity: Double,
    val unit: String
)

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val ingredients: List<Ingredient>,
    val instructions: List<String>
)
