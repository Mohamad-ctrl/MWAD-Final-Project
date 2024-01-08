package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

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
        val imageUri = Uri.parse(image)

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

        Glide.with(this)
            .load(imageUri)
            .into(bikeImage)

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
}
