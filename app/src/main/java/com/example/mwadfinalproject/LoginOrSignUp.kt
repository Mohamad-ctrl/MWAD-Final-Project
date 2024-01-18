package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.ActionBar

class LoginOrSignUp : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_or_sign_up)
        val LoginBtn = findViewById<Button>(R.id.LoginBtnLoginOrSignUpAct)
        val SignUpBtn = findViewById<Button>(R.id.SignUpBtnLoginOrSignUpAct)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide();

        LoginBtn.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
        SignUpBtn.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
    }


    fun homeIcon(view: View) {
        val intent = Intent(this@LoginOrSignUp, MainActivity::class.java)
        startActivity(intent)
    }
    fun profileIcon(view: View) {
        val intent = Intent(this@LoginOrSignUp, Profile::class.java)
        startActivity(intent)
    }
    fun cartIcon(view: View){
        val intent = Intent(this@LoginOrSignUp, Cart::class.java)
        startActivity(intent)
    }
}