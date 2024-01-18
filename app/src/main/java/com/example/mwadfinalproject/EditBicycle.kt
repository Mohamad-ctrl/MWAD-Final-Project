package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.util.*

class EditBicycle : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("bicycles")
    private lateinit var progressBar: ProgressBar
    private val IMAGE_PICK_CODE = 1000
    private lateinit var usersRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private lateinit var bicycleId: String
    private lateinit var oldImageURL: String


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_bicycle)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()
        bicycleId = intent.getStringExtra("bicycleId").toString()
        usersRef = myRef.child(bicycleId)
        storageRef = FirebaseStorage.getInstance().reference.child("images")

        progressBar = findViewById(R.id.loadingEditBikeAct)
        val UpdateBtn = findViewById<Button>(R.id.UpdateBtnEditBikeAct)
        val priceEdit = findViewById<EditText>(R.id.PriceEditBikeAct)
        val modelEdit = findViewById<EditText>(R.id.ModelEditBikeAct)
        val brandEdit = findViewById<EditText>(R.id.BrandEditBikeAct)
        val descriptionFieldEdit = findViewById<EditText>(R.id.descriptionEditBikeAct)
        val itemImageEdit = findViewById<ImageView>(R.id.ItemImageEditBikeAct)
        val curDataBtn = findViewById<Button>(R.id.ViewCurrentDataEditBikeAct)

        curDataBtn.setOnClickListener {
            val intent = Intent(this, DisplayData::class.java)
            startActivity(intent)
        }

        itemImageEdit.setOnClickListener {
            // Create an Intent to pick an image from the gallery
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                oldImageURL = snapshot.child("imageURL").getValue(String::class.java).toString()
                val bike = snapshot.getValue(Bicycle::class.java)
                val image = intent.getStringExtra("bicycleImage")
                val imageUri = Uri.parse(image)
                priceEdit.setText(bike?.price.toString())
                modelEdit.setText(bike?.model)
                brandEdit.setText(bike?.brand)
                descriptionFieldEdit.setText(bike?.description)
                Glide.with(this@EditBicycle)
                    .load(imageUri)
                    .into(itemImageEdit)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        UpdateBtn.setOnClickListener {
            val validationError = findViewById<TextView>(R.id.ValidationTextEditBikeAct)
            val price = priceEdit.text.toString()

            // Validate the price input
            if (isValidPrice(price)) {
                val priceInt: Int = price.toInt()

                val model = modelEdit.text.toString()
                val brand = brandEdit.text.toString()
                val descriptionField = descriptionFieldEdit.text.toString()

                // Update data in the database
                val bikeData = mapOf(
                    "brand" to brand,
                    "model" to model,
                    "price" to priceInt,
                    "description" to descriptionField
                )

                usersRef.updateChildren(bikeData)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Check if a new image is selected, and update it if necessary
                            val imageUri: Uri? = itemImageEdit.tag as? Uri
                            if (imageUri != null) {
                                uploadImage(imageUri)
                                deleteImageFromStorage(oldImageURL)
                            } else {
                                // No new image selected, show a toast or take appropriate action
                                Toast.makeText(this@EditBicycle, "Data has been updated", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@EditBicycle, "Failed to update data", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                validationError.text = "Please enter a valid price"
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            // Handle the result of image selection
            val imageUri: Uri? = data?.data
            findViewById<ImageView>(R.id.ItemImageEditBikeAct).setImageURI(imageUri)
            findViewById<ImageView>(R.id.ItemImageEditBikeAct).tag = imageUri // Store the image URI in the tag for future use
        }
    }

    private fun uploadImage(imageUri: Uri?) {
        if (imageUri != null) {
            val imageFileName = "image_${System.currentTimeMillis()}.jpg"
            val imageRef = storageRef.child(imageFileName)
            progressBar.visibility = ProgressBar.VISIBLE

            // Upload the new image file to Firebase Storage
            imageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                    // Image uploaded successfully, get the download URL
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        progressBar.visibility = ProgressBar.GONE
                        val newImageURL = uri.toString()

                        // Update the image URL in the database
                        usersRef.child("imageURL").setValue(newImageURL)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this@EditBicycle,
                                        "Data has been updated",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@EditBicycle,
                                        "Failed to update image",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = ProgressBar.GONE
                    // Handle failed image upload
                    Toast.makeText(this@EditBicycle, "Image upload failed: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun deleteImageFromStorage(imageURL: String) {
        // Get a reference to the old image file in Firebase Storage
        val oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageURL)

        // Delete the old image file
        oldImageRef.delete()
            .addOnSuccessListener {
                // File deleted successfully
                Toast.makeText(this@EditBicycle, "Old image has been deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Handle the failure to delete the old image
                Toast.makeText(this@EditBicycle, "Failed to delete old image: $e", Toast.LENGTH_SHORT).show()
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
        val intent = Intent(this@EditBicycle, MainActivity::class.java)
        startActivity(intent)
    }
    fun profileIcon(view: View) {
        val intent = Intent(this@EditBicycle, Profile::class.java)
        startActivity(intent)
    }
    fun cartIcon(view: View){
        val intent = Intent(this@EditBicycle, Cart::class.java)
        startActivity(intent)
    }
}