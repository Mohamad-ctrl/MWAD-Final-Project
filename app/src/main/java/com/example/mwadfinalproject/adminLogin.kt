package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class adminLogin : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("bicycles")
    @SuppressLint("MissingInflatedId", "SetTextI18n")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)
        val InsertBtn = findViewById<Button>(R.id.InsertBtn)
        val Price = findViewById<EditText>(R.id.Price)
        val Model = findViewById<EditText>(R.id.Model)
        val Brand = findViewById<EditText>(R.id.Brand)

        InsertBtn.setOnClickListener{
            val pricestr = Price.text.toString()
            val priceInt: Int = pricestr.toIntOrNull() ?: 0

            if (Price.text.isNotEmpty() && Model.text.isNotEmpty() && Brand.text.isNotEmpty()) {
                writeDataToDatabase(Brand.text.toString(), Model.text.toString(), priceInt)
            }
        }
    }
    private fun writeDataToDatabase(brand: String, model: String, price: Int) {
        // Create a unique ID for the bicycle
        val bicycleId = myRef.push().key

        // Sample bicycle data
        val bicycle = HashMap<String, Any>()
        bicycle["brand"] = brand
        bicycle["model"] = model
        bicycle["price"] = price

        // Write the bicycle data to the database under a unique ID
        bicycleId?.let {
            myRef.child(it).setValue(bicycle)
                .addOnSuccessListener {
                    Log.d("MainActivity", "Bicycle data saved to database")
                    // Once data is written, read it
                     readDataFromDatabase()
                }
                .addOnFailureListener {
                    Log.e("MainActivity", "Error writing bicycle data to database", it)
                }
        }
    }

        private fun readDataFromDatabase() {
        // Read data from the database
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    // Retrieve bicycle data
                    val brand = snapshot.child("brand").getValue(String::class.java)
                    val model = snapshot.child("model").getValue(String::class.java)
                    val price = snapshot.child("price").getValue(Double::class.java)

                    // Log retrieved data
                    Log.d("MainActivity", "Brand: $brand, Model: $model, Price: $price")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("MainActivity", "Error reading data from database", databaseError.toException())
            }
        })
    }

}