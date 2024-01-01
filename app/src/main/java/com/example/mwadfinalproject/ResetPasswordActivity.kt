package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        auth = FirebaseAuth.getInstance()

        val resetButton = findViewById<Button>(R.id.SendEmailBtnResestPassAct)
        val emailEditText = findViewById<EditText>(R.id.EmailFieldResetPassAct)
        val profileIcon = findViewById<ImageView>(R.id.profileIconResetPassAct).drawable
        profileIcon.setColorFilter(resources.getColor(R.color.GoldenColor), PorterDuff.Mode.SRC_IN)

        resetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                // Handle empty email
                Toast.makeText(this, "Enter your email", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Password reset email sent successfully
                            Toast.makeText(this, "Password reset email sent to $email", Toast.LENGTH_SHORT).show()
                            finish() // Finish the activity after sending the email
                        } else {
                            // Handle password reset failure
                            Toast.makeText(this, "Failed to send password reset email", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}