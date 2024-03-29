package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.google.firebase.auth.FirebaseAuth

class   ResetPasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        auth = FirebaseAuth.getInstance()
        val validationText = findViewById<TextView>(R.id.ValidationTextResetPassAct)
        val resetButton = findViewById<Button>(R.id.SendEmailBtnResestPassAct)
        val emailEditText = findViewById<EditText>(R.id.EmailFieldResetPassAct)
        val profileIcon = findViewById<ImageView>(R.id.profileIconResetPassAct).drawable
        profileIcon.setColorFilter(resources.getColor(R.color.GoldenColor), PorterDuff.Mode.SRC_IN)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide();

        resetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                // Handle empty email
                validationText.text = "Please enter an email"
            } else {
                if (isEmailValid(email)) {
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Password reset email sent successfully
                                Toast.makeText(
                                    this,
                                    "Password reset email sent to $email",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish() // Finish the activity after sending the email
                            } else {
                                // Handle password reset failure
                                Toast.makeText(
                                    this,
                                    "Failed to send password reset email",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }  else
                    validationText.text = "Please enter a valid email"
            }
        }
    }
        fun homeIcon(view: View) {
            val intent = Intent(this@ResetPasswordActivity, MainActivity::class.java)
            startActivity(intent)
    }
    fun profileIcon(view: View) {
        val intent = Intent(this@ResetPasswordActivity, Profile::class.java)
        startActivity(intent)
    }
    fun cartIcon(view: View){
        val intent = Intent(this@ResetPasswordActivity, Cart::class.java)
        startActivity(intent)
    }
    fun isEmailValid(Email: String): Boolean {
        return if (Email.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(Email).matches()
        } else {
            Email.isNotBlank()
        }
    }
}