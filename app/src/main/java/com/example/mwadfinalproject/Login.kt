package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.Profile
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance() // Initialize FirebaseDatabase instance
        val LoginBtn = findViewById<Button>(R.id.LoginBtnLoginAct)
        val loginFiled = findViewById<EditText>(R.id.EmailLoginAct)
        val passwordFiled = findViewById<EditText>(R.id.passwordLoginAct)
        val errorText = findViewById<TextView>(R.id.ErrorTextLogin)
        LoginBtn.setOnClickListener{
            if ((loginFiled.text.toString() == "admin") && (passwordFiled.text.toString() == "admin")) {
                val user = auth.currentUser
                val Intent = Intent(this, adminLogin::class.java)
                startActivity(Intent)
            } else {
                if (isEmailValid(loginFiled.text.toString().trim()))
                    loginUser(loginFiled.text.toString().trim(), passwordFiled.text.toString())
                else
                    errorText.text = "Please enter a valid email"
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        val errorText = findViewById<TextView>(R.id.ErrorTextLogin)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && !user.isEmailVerified) {
                        user.sendEmailVerification()
                            .addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    errorText.text = "Please check your email to verify your email"
                                    Log.d("LoginActivity", "Verification email sent")
                                } else {
                                    errorText.text = "Error in verification"
                                    Log.e(
                                        "LoginActivity",
                                        "Error in verification",
                                        verificationTask.exception
                                    )
                                }
                            }
                    } else {
                        startActivity(Intent(this@Login, MainActivity::class.java))
                    }
                } else {
                    errorText.text = "Login failed: ${task.exception?.message}"
                    Log.e("LoginActivity", "Login failed: ${task.exception}")
                }
            }
    }

    fun ForgotPassBtn(view: View){
        val Intent = Intent(this@Login, ResetPasswordActivity::class.java)
        startActivity(Intent)
    }

    fun NotRegisteredBtn(view: View){
        val Intent = Intent(this@Login, SignUp::class.java)
        startActivity(Intent)
    }

    fun isEmailValid(Email: String): Boolean {
        return if (Email.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(Email).matches()
        } else {
            Email.isNotBlank()
        }
    }
        fun homeIcon(view: View) {
            val intent = Intent(this@Login, MainActivity::class.java)
            startActivity(intent)
    }
    fun profileIcon(view: View) {
        val intent = Intent(this@Login, Profile::class.java)
        startActivity(intent)
    }
    fun cartIcon(view: View){
        val intent = Intent(this@Login, Cart::class.java)
        startActivity(intent)
    }
}