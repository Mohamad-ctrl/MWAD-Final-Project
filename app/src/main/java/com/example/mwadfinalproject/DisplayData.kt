package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class DisplayData : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("bicycles")
    private lateinit var auth: FirebaseAuth
    private lateinit var authListener: FirebaseAuth.AuthStateListener

    fun homeIcon(view: View) {
        val intent = Intent(this@DisplayData, MainActivity::class.java)
        startActivity(intent)
    }
    fun profileIcon(view: View) {
        val intent = Intent(this@DisplayData, Profile::class.java)
        startActivity(intent)
    }
    fun cartIcon(view: View){
        val intent = Intent(this@DisplayData, Cart::class.java)
        startActivity(intent)
    }
    private fun readDataFromDatabase(brandName: String) {
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val bicycles = mutableListOf<Bicycle>()

                for (snapshot in dataSnapshot.children) {
                    val brand = snapshot.child("brand").getValue(String::class.java)

                    if (brandName.isEmpty() || brand.equals(brandName, ignoreCase = true)) {
                        val model = snapshot.child("model").getValue(String::class.java)
                        val price = snapshot.child("price").getValue(Int::class.java)
                        val description = snapshot.child("description").getValue(String::class.java)
                        val imageURL = snapshot.child("imageURL").getValue(String::class.java)
                        val bicycleId = snapshot.key

                        if (model != null && price != null && description != null && imageURL != null && bicycleId != null) {
                            bicycles.add(Bicycle(bicycleId, brand.toString(), model, description, price, imageURL, false, 1))
                        }
                    }
                }

                val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewDisplayDataAct)
                recyclerView.layoutManager = GridLayoutManager(this@DisplayData, 2)
                val adapter = BicycleAdapter(bicycles)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("adminLogin", "Error reading data from database", databaseError.toException())
            }
        })
    }
    private fun deleteImageFromStorage(imageURL: String) {
        // Get a reference to the old image file in Firebase Storage
        val oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageURL)

        // Delete the old image file
        oldImageRef.delete()
            .addOnSuccessListener {
                // File deleted successfully
                Toast.makeText(this@DisplayData, "Bicycle data has been deleted from database", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Handle the failure to delete the old image
                Toast.makeText(this@DisplayData, "Failed to delete old image: $e", Toast.LENGTH_SHORT).show()
            }
    }

    inner class BicycleAdapter(private val bicycleList: List<Bicycle>) :
        RecyclerView.Adapter<BicycleAdapter.BicycleViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BicycleViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_holder_admin, parent, false)
            return BicycleViewHolder(itemView)
        }
        private fun deleteBicycle(bicycleId: String) {
            // Delete the bicycle from the database
            myRef.child(bicycleId).removeValue()
                .addOnSuccessListener {
                    Log.d("adminLogin", "Bicycle data has been deleted from database")
                }
                .addOnFailureListener {
                    Log.e("adminLogin", "Error deleting bicycle data from database", it)
                }
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: BicycleViewHolder, position: Int) {
            val currentItem = bicycleList[position]
            val imageUri = Uri.parse(currentItem.ImageURL)
            val bicycleId = currentItem.id


            holder.brandTextView.text = currentItem.brand
            holder.priceTextView.text = "${currentItem.price}$"

            Glide.with(holder.itemView.context)
                .load(imageUri)
                .into(holder.bikeImage)

            holder.EditButton.setOnClickListener {

                val intent = Intent(holder.itemView.context, EditBicycle::class.java)
                intent.putExtra("bicycleId", bicycleId)
                intent.putExtra("bicycleImage", currentItem.ImageURL)

                holder.itemView.context.startActivity(intent)
            }

            holder.DeleteItem.setOnClickListener {
                val imageURL = currentItem.ImageURL
                deleteBicycle(bicycleId)
                deleteImageFromStorage(imageURL)
                holder.container.visibility = View.GONE
            }
        }

        override fun getItemCount() = bicycleList.size

        inner class BicycleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val brandTextView: TextView = itemView.findViewById(R.id.textBrand)
            val priceTextView: TextView = itemView.findViewById(R.id.textPrice)
            val bikeImage: ImageView = itemView.findViewById(R.id.imageBicycle)
            val EditButton: Button = itemView.findViewById(R.id.EditButton)
            val DeleteItem = itemView.findViewById<LinearLayout>(R.id.DeleteItem)
            val container = itemView.findViewById<LinearLayout>(R.id.holderContin)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_data)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide();
        val insertbtn = findViewById<Button>(R.id.InsertNewItemsBtnDisplayDataAct)
        val brandEditText = findViewById<EditText>(R.id.brandEditTextDisplayDataAct)

        insertbtn.setOnClickListener {
            startActivity(Intent(this, adminLogin::class.java))
        }

        brandEditText.setOnClickListener {
            val brandName = brandEditText.text.toString()
            readDataFromDatabase(brandName)
        }
        readDataFromDatabase("")
    }
}