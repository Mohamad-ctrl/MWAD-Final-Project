package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class SignUp : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        usersRef = database.getReference("users")

        // Call the function to register a new user
        val signupButton = findViewById<Button>(R.id.SignUpBtnSignUpAct)
        val birthDateButton = findViewById<Button>(R.id.ShowCalBtnSignUpAct)
        val calendar = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            // Set the selected date to the EditText field or use it as needed
            val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
            findViewById<EditText>(R.id.AgeSignUpAct).setText(selectedDate)
        }

        birthDateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this@SignUp,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis() // Set max date as current date
            datePickerDialog.show()
        }

        signupButton.setOnClickListener {
            // Fetch EditText values when the button is clicked
            val birthDate = findViewById<EditText>(R.id.AgeSignUpAct).text.toString() // Retrieve date of birth
            val email = findViewById<EditText>(R.id.EmailSignUpAct).text.toString()
            val userName = findViewById<EditText>(R.id.usernameSignUpAct).text.toString().trim()
            val password = findViewById<EditText>(R.id.passwordSignUpAct).text.toString()
            val password2 = findViewById<EditText>(R.id.ConfoirmPasswordSignUpAct).text.toString()
            val errorText = findViewById<TextView>(R.id.ErrorTextSignUpAct)
            val genderGroup = findViewById<RadioGroup>(R.id.radioGroupSignUpAct)
            val genderId = genderGroup.checkedRadioButtonId
            val selectedGender = when (genderId) {
                R.id.MaleRadioSignUpAct -> "Male"
                R.id.FemaleRadioSignUpAct -> "Female"
                else -> "" // Handle if no gender is selected
            }
            if (isPasswordValid(password, password2))
                registerNewUser(email, password, userName, birthDate, selectedGender)
            else
                errorText.text = "Password do not match"

        }
    }

    private fun registerNewUser(email: String, password: String, userName: String, birthDate: String, selectedGender: String) {
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
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(userName) // Set the display name
                                    .build()
                                user?.updateProfile(profileUpdates)
                                    ?.addOnCompleteListener{ updateTask ->
                                        if (updateTask.isSuccessful) {
                                            Log.d("SignUpActivity", "User display name updated")
                                        } else {
                                            Log.d("SignUpActivity", "Error in updating the user's display name")
                                        }
                                    }
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
                                    saveUserDataToDatabase(userId, userName, birthDate, selectedGender)
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

    private fun saveUserDataToDatabase(userId: String, userName: String, birthDate: String, gender: String) {
        val userData = HashMap<String, Any>()
        userData["username"] = userName
        userData["birthDate"] = birthDate
        userData["gender"] = gender // Save gender

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
    private fun isPasswordValid(password: String, password2: String): Boolean {
        return password == password2
    }
