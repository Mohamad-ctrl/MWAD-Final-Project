package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
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
        val LoginBtn = findViewById<Button>(R.id.LoginUpBtnLoginAct)
        val loginFiled = findViewById<EditText>(R.id.EmailLoginAct)
        val passwordFiled = findViewById<EditText>(R.id.passwordLoginAct)
        LoginBtn.setOnClickListener{
                if ((loginFiled.text.toString() == "admin") && (passwordFiled.text.toString() == "admin")) {
                    val Intent = Intent(this, adminLogin::class.java)
                    startActivity(Intent)
                } else {
                    loginUser(loginFiled.text.toString(), passwordFiled.text.toString())
            }
        }

    }

    private fun loginUser(emailOrUsername: String, password: String) {
        val usersRef = database.getReference("users")
        val errorText = findViewById<TextView>(R.id.ErrorTextLogin)

        if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrUsername).matches()) {
            // Input is an email
            auth.signInWithEmailAndPassword(emailOrUsername, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null && !user.isEmailVerified) {
                            user.sendEmailVerification()
                                .addOnCompleteListener { verificationTask ->
                                    if (verificationTask.isSuccessful) {
                                        errorText.text =
                                            "Please check your email to verify your email"
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
                            Log.d("LoginActivity", "User logged in: ${user?.uid}")
                            startActivity(Intent(this@Login, MainActivity::class.java))
                            // Proceed to the next screen or perform required action
                        }
                    } else {
                        errorText.text = "Login failed: ${task.exception?.message}"
                        Log.e("LoginActivity", "Login failed: ${task.exception}")
                    }
                }
        } else {
            // Input is a username
            usersRef.orderByChild("username").equalTo(emailOrUsername)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val userSnapshot = dataSnapshot.children.first()
                            val userEmail = userSnapshot.child("email").getValue(String::class.java)
                            if (userEmail != null) {
                                auth.signInWithEmailAndPassword(userEmail, password)
                                    .addOnCompleteListener(this@Login) { task ->
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
                                                val userName = userSnapshot.child("username").getValue(String::class.java)
                                                if (userName != null) {
                                                    val profileUpdates =
                                                        UserProfileChangeRequest.Builder()
                                                            .setDisplayName(userName)
                                                            .build()
                                                    user?.updateProfile(profileUpdates)
                                                        ?.addOnCompleteListener { updateTask ->
                                                            if (updateTask.isSuccessful) {
                                                                Log.d(
                                                                    "LoginActivity",
                                                                    "Username updated: $userName"
                                                                )
                                                            }
                                                        }
                                                }
                                                startActivity(Intent(this@Login, MainActivity::class.java))
                                            }
                                        } else {
                                            errorText.text = "User Name or Password is Wrong"
                                            Log.e("LoginActivity", "Login failed: ${task.exception}")
                                        }
                                    }
                            } else {
                                errorText.text = "No email found for the username"
                            }
                        } else {
                            errorText.text = "Username not found"
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        errorText.text =
                            "Error checking username existence: ${databaseError.message}"
                        Log.e(
                            "LoginActivity",
                            "Error checking username existence: ${databaseError.message}"
                        )
                    }
                })
        }
    }


//    private fun loginUser(email: String, password: String) {
//        auth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // User login successful
//                    val user = auth.currentUser
//                    // Proceed to the next screen or perform required action
//                    Log.d("LoginActivity", "User logged in: ${user?.uid}")
//                } else {
//                    // Handle login failure
//                    Log.e("LoginActivity", "Login failed: ${task.exception}")
//                }
//            }
//    }

    // A placeholder username validation check
//    private fun isUserNameValid(username: String): Boolean {
//        return if (username.contains('@')) {
//            Patterns.EMAIL_ADDRESS.matcher(username).matches()
//        } else {
//            username.isNotBlank()
//        }
//    }
    // A placeholder password validation check
//    private fun isPasswordValid(password: String): Boolean {
//        return password.length > 5
//    }
}
