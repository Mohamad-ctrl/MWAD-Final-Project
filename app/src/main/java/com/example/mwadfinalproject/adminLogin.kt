package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class adminLogin : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("bicycles")
    private val IMAGE_PICK_CODE = 1000 // Arbitrary request code
    private lateinit var progressBar: ProgressBar
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)
        progressBar = findViewById(R.id.loadingAdminLoginAct)
        val InsertBtn = findViewById<Button>(R.id.insertBtn)
        val Price = findViewById<EditText>(R.id.Price)
        val Model = findViewById<EditText>(R.id.Model)
        val Brand = findViewById<EditText>(R.id.Brand)
        val descriptionField = findViewById<EditText>(R.id.description)
        val ItemImage = findViewById<ImageView>(R.id.ItemImage)
        val curDataBtn = findViewById<Button>(R.id.ViewCurrentDataAdminLoginAct)
        val validationError = findViewById<TextView>(R.id.ValidationTextAdminLoginAct)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide();

        curDataBtn.setOnClickListener {
            val intent = Intent(this, DisplayData::class.java)
            startActivity(intent)
        }

        ItemImage.setOnClickListener {
            // Create an Intent to pick an image from the gallery
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        InsertBtn.setOnClickListener {
            val pricestr = Price.text.toString()
            val priceInt: Int = pricestr.toIntOrNull() ?: 0
            val description = descriptionField.text.toString()
            val brand = Brand.text.toString()
            val model = Model.text.toString()

            // Validation: Check if the image is selected
            val imageUri = ItemImage.tag as? Uri
            if (imageUri != null) {
                if (isValidPrice(pricestr)) {
                    uploadImageToFirebaseStorage(imageUri, brand, model, priceInt, description)
                } else
                    validationError.text = "Please enter a valid price"
            } else
                validationError.text = "Please select a photo for the bicycle"
        }
    }

    private fun uploadImageToFirebaseStorage(
        imageUri: Uri,
        brand: String,
        model: String,
        price: Int,
        description: String
    ) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val imagesRef = storageRef.child("images")

        // Generate a unique filename for the image
        val imageFileName = "image_${System.currentTimeMillis()}.jpg"
        val imageRef = imagesRef.child(imageFileName)
        progressBar.visibility = View.VISIBLE

        // Upload the image file to Firebase Storage
        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Image uploaded successfully, get the download URL
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    progressBar.visibility = View.GONE
                    val imageURL = uri.toString()

                    // Call the function to write data to the database, including the image URL
                    writeDataToDatabase(brand, model, price, description, imageURL)
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                // Handle failed image upload
                Log.e("adminLogin", "Image upload failed: $e")
            }
    }

    private fun writeDataToDatabase(
        brand: String,
        model: String,
        price: Int,
        description: String,
        imageURL: String
    ) {
        val bicycleId = myRef.push().key

        val bicycle = HashMap<String, Any>()
        bicycle["brand"] = brand
        bicycle["model"] = model
        bicycle["price"] = price
        bicycle["description"] = description
        bicycle["imageURL"] = imageURL

        bicycleId?.let {
            myRef.child(it).setValue(bicycle)
                .addOnSuccessListener {
                    Toast.makeText(this@adminLogin, "Item has been inserted to the database", Toast.LENGTH_SHORT).show()
                    Log.d("adminLogin", "Bicycle data saved to database")
//                    readDataFromDatabase()
                }
                .addOnFailureListener {
                    Log.e("adminLogin", "Error writing bicycle data to database", it)
                }
        }
    }

//    private fun readDataFromDatabase() {
//        // Read data from the database
//        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (snapshot in dataSnapshot.children) {
//                    // Retrieve bicycle data
//                    val brand = snapshot.child("brand").getValue(String::class.java)
//                    val model = snapshot.child("model").getValue(String::class.java)
//                    val price = snapshot.child("price").getValue(Double::class.java)
//                    val description = snapshot.child("description").getValue(String::class.java)
//
//                    // Log retrieved data
//                    Log.d(
//                        "adminLogin",
//                        "Brand: $brand, Model: $model, Price: $price, Description: $description"
//                    )
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.e(
//                    "adminLogin",
//                    "Error reading data from database",
//                    databaseError.toException()
//                )
//            }
//        })
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val ItemImage = findViewById<ImageView>(R.id.ItemImage)

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            findViewById<ImageView>(R.id.ItemImage).setImageURI(imageUri)
            ItemImage.tag = imageUri // Store the image URI in the tag for future use
        }
    }
    fun isValidPrice(priceInput: String): Boolean {
        // Check if the input is not empty
        if (priceInput.isEmpty()) {
            return false
        }

        // Try parsing the input as a double
        try {
            val price = priceInput.toInt()

            // Check if the price is a positive number
            if (price >= 0) {
                return true
            }
        } catch (e: NumberFormatException) {
            // Handle the case where the input is not a valid number
            return false
        }

        return false
    }

    fun homeIcon(view: View) {
        val intent = Intent(this@adminLogin, MainActivity::class.java)
        startActivity(intent)
    }
    fun profileIcon(view: View) {
        val intent = Intent(this@adminLogin, Profile::class.java)
        startActivity(intent)
    }
    fun cartIcon(view: View){
        val intent = Intent(this@adminLogin, Cart::class.java)
        startActivity(intent)
    }
}