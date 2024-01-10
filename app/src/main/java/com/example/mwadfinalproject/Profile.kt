package com.example.mwadfinalproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBar
import com.google.firebase.auth.FirebaseAuth

class Profile : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var authListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val SignOutBtn = findViewById<LinearLayout>(R.id.SignOutProfileAct)
        val ManageAccBtn = findViewById<LinearLayout>(R.id.ManageAccountProfileAct)

        ManageAccBtn.setOnClickListener{
            startActivity(Intent(this, ManageAccount::class.java))
        }

        auth = FirebaseAuth.getInstance()
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            val actionBar: ActionBar? = supportActionBar
            actionBar?.hide();
            if (user == null) {
                intent = Intent(this@Profile, LoginOrSignUp::class.java)
                startActivity(intent)
            }
        }
        SignOutBtn.setOnClickListener {
//            // Sign the user out
            auth.signOut()
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

        fun homeIcon(view: View) {
            val intent = Intent(this@Profile, MainActivity::class.java)
            startActivity(intent)
    }
//    fun profileIcon(view: View) {
//        val intent = Intent(this@Profile, Profile::class.java)
//        startActivity(intent)
//    }
    fun cartIcon(view: View){
        val intent = Intent(this@Profile, Cart::class.java)
        startActivity(intent)
    }
}