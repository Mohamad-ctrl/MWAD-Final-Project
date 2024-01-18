package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.bumptech.glide.Glide
import com.google.gson.Gson

class ShowBicycle : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_bicycle)
        val brand = intent.getStringExtra("brand")
        val image = intent.getStringExtra("image")
        val model = intent.getStringExtra("model")
        val description = intent.getStringExtra("description")
        val price = intent.getIntExtra("price", 0)
        val StringPrice = price.toString()
        val imageUri: Uri? = if (!image.isNullOrEmpty()) Uri.parse(image) else null
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide();

        val bikeBrand = findViewById<TextView>(R.id.BrandTextShowBikeAct).apply {
            text = brand
        }
        val bikeModel = findViewById<TextView>(R.id.ModelTextShowBikeAct).apply {
            text = model
        }
        val bikeDescription = findViewById<TextView>(R.id.descriptionTextShowBikeAct).apply {
            text = description
        }
        val bikePrice = findViewById<TextView>(R.id.pricTextShowBikeAct).apply {
            text = " $StringPrice$"

        }

        val bikeImage = findViewById<ImageView>(R.id.bikeImageShowBikeAct)

        imageUri?.let {
            Glide.with(this)
                .load(it)
                .into(bikeImage)
        }

        }

    fun homeIcon(view: View) {
            val intent = Intent(this@ShowBicycle, MainActivity::class.java)
            startActivity(intent)
    }
    fun profileIcon(view: View) {
        val intent = Intent(this@ShowBicycle, Profile::class.java)
        startActivity(intent)
    }
    fun cartIcon(view: View){
        val intent = Intent(this@ShowBicycle, Cart::class.java)
        startActivity(intent)
    }
    private fun getCartItems(): Set<Bicycle> {
        val sharedPreferences = getSharedPreferences("Cart", Context.MODE_PRIVATE)
        val cartItemsSet = sharedPreferences.getStringSet("cart_items", emptySet()) ?: emptySet()
        return cartItemsSet.map { Gson().fromJson(it, Bicycle::class.java) }.toSet()
    }
    private fun saveCartItems(cartItems: Set<Bicycle>) {
        val sharedPreferences = getSharedPreferences("Cart", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("cart_items", cartItems.map { Gson().toJson(it) }.toSet())
        editor.apply()
    }

    private fun addToCart(item: Bicycle) {
        val cartItems = getCartItems().toMutableSet()
        val existingItem = cartItems.find { it.id == item.id }

        if (existingItem != null) {
            existingItem.quantity++
        } else {
            cartItems.add(item.copy(addedToCart = true, quantity = 1))
        }

        saveCartItems(cartItems)
        Toast.makeText(this@ShowBicycle, "Item has been added to cart", Toast.LENGTH_SHORT).show()
    }
}
