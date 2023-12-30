package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignUp : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        usersRef = database.getReference("users")

        // Call the function to register a new user
        val signupButton = findViewById<Button>(R.id.SignUpBtn)
        signupButton.setOnClickListener {
            // Fetch EditText values when the button is clicked
            val age = findViewById<EditText>(R.id.Age).text.toString()
            val ageInt: Int = age.toIntOrNull() ?: 0
            val email = findViewById<EditText>(R.id.Email).text.toString()
            val userName = findViewById<EditText>(R.id.username).text.toString().trim()
            val password = findViewById<EditText>(R.id.password).text.toString()

            // Call the function to register a new user with fetched values
            registerNewUser(email, password, userName, ageInt)
        }
    }

    private fun registerNewUser(email: String, password: String, userName: String, age: Int) {
        val errorText = findViewById<TextView>(R.id.ErrorTextSignUpAct)
        // Check if the username already exists
        usersRef.orderByChild("username").equalTo(userName).addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Username already exists, handle accordingly (show error, prompt for a new username, etc.)
                    Log.d("SignUpActivity", "Username already exists")
                } else {
                    // Username doesn't exist, proceed with user registration
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this@SignUp) { task ->
                            if (task.isSuccessful) {
                                // User registration successful
                                val user = auth.currentUser
                                user?.sendEmailVerification()
                                    ?.addOnCompleteListener { verificationTask ->
                                        if (verificationTask.isSuccessful) {
                                            errorText.text = "Please check your email to verify your email"
                                        } else {
                                            errorText.text = "Error in verifying the email"
                                        }
                                    }
                                Toast.makeText(this@SignUp, "Sign is completed !", Toast.LENGTH_SHORT).show()
                                // Save additional user data to the database
                                user?.uid?.let { userId ->
                                    saveUserDataToDatabase(userId, userName, age)
                                }
                            } else {
                                // Handle user registration failure
                                Log.e("SignUpActivity", "User registration failed: ${task.exception}")
                            }
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error while checking for username existence
                Log.e("SignUpActivity", "Error checking username existence: ${databaseError.message}")
            }
        })
    }

    private fun saveUserDataToDatabase(userId: String, userName: String, age: Int) {
        val userData = HashMap<String, Any>()
        userData["username"] = userName
        userData["age"] = age

        usersRef.child(userId).setValue(userData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User data saved successfully
                    Log.d("SignUpActivity", "User data saved to database")
                } else {
                    // Handle failure to save user data
                    Log.e("SignUpActivity", "Failed to save user data: ${task.exception}")
                }
            }
    }
}
    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
