package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("bicycles")
    private lateinit var auth: FirebaseAuth
    private lateinit var authListener: FirebaseAuth.AuthStateListener


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val login = findViewById<Button>(R.id.LoginMainAct)
        val signUp = findViewById<Button>(R.id.SignUpMainAct)
        auth = FirebaseAuth.getInstance()
        val welcomeText = findViewById<TextView>(R.id.welcomeTextMainAct)
        val signOutButton: Button = findViewById(R.id.signOutButtonMainAct)

        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                // User is signed out, update UI accordingly
                welcomeText.text = "Welcome, Guest"
                signUp.visibility = View.VISIBLE
                login.visibility = View.VISIBLE
                signOutButton.visibility = View.GONE
            } else {
                // User is signed in, update UI with the signed-in user's information
                user.reload().addOnCompleteListener {
                    val updatedUser = FirebaseAuth.getInstance().currentUser
                    val userName = updatedUser?.displayName ?: "User" // Default value if username is null
                    welcomeText.text = "Welcome, $userName"
                    signUp.visibility = View.GONE
                    login.visibility = View.GONE
                    signOutButton.visibility = View.VISIBLE
                }
            }
        }

        signOutButton.setOnClickListener {
            // Sign the user out
            auth.signOut()
        }

        signUp.setOnClickListener{
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        login.setOnClickListener{
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authListener)
    }
}
//    private fun writeDataToDatabase() {
//        // Create a unique ID for the bicycle
//        val bicycleId = myRef.push().key
//
//        // Sample bicycle data
//        val bicycle = HashMap<String, Any>()
//        bicycle["brand"] = "XYZ"
//        bicycle["model"] = "2023 MXL"
//        bicycle["price"] = 300
//
//        // Write the bicycle data to the database under a unique ID
//        bicycleId?.let {
//            myRef.child(it).setValue(bicycle)
//                .addOnSuccessListener {
//                    Log.d("MainActivity", "Bicycle data saved to database")
//                    // Once data is written, read it
////                    readDataFromDatabase()
//                }
//                .addOnFailureListener {
//                    Log.e("MainActivity", "Error writing bicycle data to database", it)
//                }
//        }
//    }

//    private fun readDataFromDatabase() {
//        // Read data from the database
//        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (snapshot in dataSnapshot.children) {
//                    // Retrieve bicycle data
//                    val brand = snapshot.child("brand").getValue(String::class.java)
//                    val model = snapshot.child("model").getValue(String::class.java)
//                    val price = snapshot.child("price").getValue(Double::class.java)
//
//                    // Log retrieved data
//                    Log.d("MainActivity", "Brand: $brand, Model: $model, Price: $price")
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.e("MainActivity", "Error reading data from database", databaseError.toException())
//            }
//        })
//    }