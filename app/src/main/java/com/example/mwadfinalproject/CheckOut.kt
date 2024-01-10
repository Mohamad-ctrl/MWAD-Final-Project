package com.example.mwadfinalproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar

class CheckOut : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out)
        val receivedCartItems: ArrayList<Bicycle>? = intent.getSerializableExtra("cartItems") as? ArrayList<Bicycle>
        val actionBar: ActionBar? = supportActionBar
        receivedCartItems?.let { cartItems ->
            // Now you can use cartItems in this activity
            // Example: Display the size of the received cart items
            Toast.makeText(this, "Received ${cartItems.size} items", Toast.LENGTH_SHORT).show()
        }
        actionBar?.hide();
    }
    fun homeIcon(view: View) {
        val intent = Intent(this@CheckOut, MainActivity::class.java)
        startActivity(intent)
    }
    fun profileIcon(view: View) {
        val intent = Intent(this@CheckOut, Profile::class.java)
        startActivity(intent)
    }
    fun cartIcon(view: View){
        val intent = Intent(this@CheckOut, Cart::class.java)
        startActivity(intent)
    }
}