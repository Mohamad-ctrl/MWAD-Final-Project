package com.example.mwadfinalproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth

class Profile : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var authListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                intent = Intent(this@Profile, LoginOrSignUp::class.java)
                startActivity(intent)
            }
        }

    }
    override fun onStart() {
        super.onStart()
        // Add the AuthStateListener when the activity starts
        auth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        // Remove the AuthStateListener when the activity stops
        auth.removeAuthStateListener(authListener)
    }
}