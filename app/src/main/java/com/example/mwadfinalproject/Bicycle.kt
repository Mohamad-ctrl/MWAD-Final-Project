package com.example.mwadfinalproject

import android.graphics.ColorSpace.Model

data class Bicycle(
    val id: String, // Unique identifier for each item
    val brand: String,
    val model: String,
    val description: String,
    val price: Int,
    val imageResId: String,
    var addedToCart: Boolean,
    var quantity: Int
)
