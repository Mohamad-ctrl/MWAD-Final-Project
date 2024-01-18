package com.example.mwadfinalproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.ActionBar

class AfterCheckOut : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after_check_out)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide();
        val OrdersPagebtn = findViewById<Button>(R.id.OrdersPage)
        val continueShoppingBtn = findViewById<Button>(R.id.continueShoppingBtn)

        OrdersPagebtn.setOnClickListener{
            startActivity(Intent(this, Order::class.java))
        }
        continueShoppingBtn.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    fun homeIcon(view: View) {
        val intent = Intent(this@AfterCheckOut, MainActivity::class.java)
        startActivity(intent)
    }

    fun profileIcon(view: View) {
        val intent = Intent(this@AfterCheckOut, Profile::class.java)
        startActivity(intent)
    }

    fun cartIcon(view: View) {
        val intent = Intent(this@AfterCheckOut, Cart::class.java)

    }
}