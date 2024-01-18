package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ManageAccount : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var usersRef: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var email: String


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_account)
        auth = FirebaseAuth.getInstance()
        usersRef = FirebaseDatabase.getInstance().getReference("users").child(auth.currentUser?.uid ?: "")

        val editTextUsername = findViewById<TextView>(R.id.EmailTextManageAccAct)
        val editTextEmail = findViewById<EditText>(R.id.editTextUserName)
        val editTextBirthDate = findViewById<EditText>(R.id.editTextBirthDate)
        val radioGroupGender = findViewById<RadioGroup>(R.id.radioGroupGender)
        val radioButtonMale = findViewById<RadioButton>(R.id.radioButtonMale)
        val radioButtonFemale = findViewById<RadioButton>(R.id.radioButtonFemale)
        val buttonUpdateProfile = findViewById<Button>(R.id.buttonUpdateProfile)
        val calendar = Calendar.getInstance()
        val birthDateButton = findViewById<ImageView>(R.id.ShowCalBtnEditProfileAct)
        val ValidationError = findViewById<TextView>(R.id.ValidationTextEditProfileAct)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide();
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            // Set the selected date to the EditText field or use it as needed
            val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
            findViewById<EditText>(R.id.editTextBirthDate).setText(selectedDate)
        }

        birthDateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this@ManageAccount,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis() // Set max date as current date
            datePickerDialog.show()
        }

        // Fetch current user data and populate the fields
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                editTextUsername.text = "Email: ${user?.email}"
                editTextEmail.setText(user?.username)
                editTextBirthDate.setText(user?.birthDate)
                email = user?.email.toString()
                // Set the gender based on fetched data
                if (user?.gender == "Male") {
                    radioButtonMale.isChecked = true
                } else if (user?.gender == "Female") {
                    radioButtonFemale.isChecked = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        buttonUpdateProfile.setOnClickListener {
            val userName = editTextEmail.text.toString()
            val birthDate = editTextBirthDate.text.toString()
            val selectedGender = when (radioGroupGender.checkedRadioButtonId) {
                R.id.radioButtonMale -> "Male"
                R.id.radioButtonFemale -> "Female"
                else -> "Null"
            }

            // Update user data in Firebase Database
            // Validating user's input
            usersRef.orderByChild("username").equalTo(userName).addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        ValidationError.text = "Username already exists please choose a another one"
                    } else {
                        if (isDateOfBirthValid(birthDate)) {
                            if (selectedGender == "Null") {
                                ValidationError.text = "Please Select a gender"
                            } else {
                                val user = auth.currentUser
                                val userData = User(userName, email, birthDate, selectedGender)
                                usersRef.setValue(userData)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val profileUpdates = UserProfileChangeRequest.Builder()
                                                .setDisplayName(userName) // Set the display name
                                                .build()
                                            user?.updateProfile(profileUpdates)
                                                ?.addOnCompleteListener{ updateTask ->
                                                    if (updateTask.isSuccessful) {
                                                        Log.d("ManageAccount", "User display name updated")
                                                    } else {
                                                        Log.d("ManageAccount", "Error in updating the user's display name")
                                                    }
                                                }
                                            Toast.makeText(this@ManageAccount, "Profile updated", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(this@ManageAccount, "Failed to update profile", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        } else
                            ValidationError.text = "Please enter a valid date of birth"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error while checking for username existence
                    Log.e("SignUpActivity", "Error checking username existence: ${databaseError.message}")
                }
            })
        }
    }
    data class User(
        val username: String = "",
        val email: String = "",
        val birthDate: String = "",
        val gender: String = ""
    )
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 6
    }

    fun isDateOfBirthValid(dateOfBirth: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateFormat.isLenient = false // Set leniency to false for strict date parsing

        try {
            // Attempt to parse the date string
            dateFormat.parse(dateOfBirth)
            return true // Date format is valid
        } catch (e: ParseException) {
            return false // Date format is invalid
        }
    }
    fun homeIcon(view: View) {
        val intent = Intent(this@ManageAccount, MainActivity::class.java)
        startActivity(intent)
    }
        fun profileIcon(view: View) {
        val intent = Intent(this@ManageAccount, Profile::class.java)
        startActivity(intent)
    }
    fun cartIcon(view: View){
        val intent = Intent(this@ManageAccount, Cart::class.java)
        startActivity(intent)
    }
}
