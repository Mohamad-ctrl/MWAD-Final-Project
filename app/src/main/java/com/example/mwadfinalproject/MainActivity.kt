package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.mwadfinalproject.Bicycle
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.bumptech.glide.Glide
import com.google.gson.Gson


class MainActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("bicycles")
    private lateinit var auth: FirebaseAuth
    private lateinit var authListener: FirebaseAuth.AuthStateListener
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()
        val welcomeText = findViewById<TextView>(R.id.welcomeTextMainAct)
        val signOutButton: Button = findViewById(R.id.signOutButtonMainAct)
        readDataFromDatabase()
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                // User is signed out, update UI accordingly
                signOutButton.visibility = View.GONE
                welcomeText.visibility = View.GONE
            } else {
                // User is signed in, update UI with the signed-in user's information
                user.reload().addOnCompleteListener {
                    val updatedUser = FirebaseAuth.getInstance().currentUser
                    val userName = user.displayName
                    welcomeText.text = "Welcome, \n${userName ?: "User"}"
                    signOutButton.visibility = View.VISIBLE
                }
            }
        }

        signOutButton.setOnClickListener {
            // Sign the user out
            auth.signOut()
        }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authListener)
    }

//    fun homeIcon(view: View) {
//        val intent = Intent(this@MainActivity, MainActivity::class.java)
//    }
    fun profileIcon(view: View) {
        val intent = Intent(this@MainActivity, Profile::class.java)
        startActivity(intent)
    }
    fun cartIcon(view: View){
        val intent = Intent(this@MainActivity, Cart::class.java)
        startActivity(intent)
    }
    private fun readDataFromDatabase() {
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val bicycles = mutableListOf<Bicycle>() // Create a list to hold bicycle items

                for (snapshot in dataSnapshot.children) {
                    val brand = snapshot.child("brand").getValue(String::class.java)
                    val model = snapshot.child("model").getValue(String::class.java)
                    val price = snapshot.child("price").getValue(Int::class.java)
                    val description = snapshot.child("description").getValue(String::class.java)
                    val imageURL = snapshot.child("imageURL").getValue(String::class.java)

                    // Get the unique ID of the bicycle from Firebase
                    val bicycleId = snapshot.key // Fetch the unique ID from Firebase

                    // Create Bicycle object with the fetched ID and add to the list
                    if (brand != null && model != null && price != null && description != null && imageURL != null && bicycleId != null) {
                        bicycles.add(Bicycle(bicycleId, brand, model, description, price, imageURL, false, 1))
                    }
                }

                // Set up your RecyclerView adapter here using the 'bicycles' list
                val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewMainAct)
                recyclerView.layoutManager = GridLayoutManager(this@MainActivity, 2) // Set 2 columns
                val adapter = BicycleAdapter(bicycles)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(
                    "adminLogin",
                    "Error reading data from database",
                    databaseError.toException()
                )
            }
        })
    }
    private fun addToCart(item: Bicycle) {
        val cartItems = getCartItems().toMutableSet()
        val existingItem = cartItems.find { it.id == item.id }

        if (existingItem != null) {
            existingItem.quantity++
        } else {
            cartItems.add(item.copy(addedToCart = true, quantity = 1))
        }

        saveCartItems(cartItems)
        Toast.makeText(this@MainActivity, "Item has been added to cart", Toast.LENGTH_SHORT).show()
    }

    private fun saveCartItems(cartItems: Set<Bicycle>) {
        val sharedPreferences = getSharedPreferences("Cart", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("cart_items", cartItems.map { Gson().toJson(it) }.toSet())
        editor.apply()
    }

    private fun getCartItems(): Set<Bicycle> {
        val sharedPreferences = getSharedPreferences("Cart", Context.MODE_PRIVATE)
        val cartItemsSet = sharedPreferences.getStringSet("cart_items", emptySet()) ?: emptySet()
        return cartItemsSet.map { Gson().fromJson(it, Bicycle::class.java) }.toSet()
    }
    inner class BicycleAdapter(private val bicycleList: List<Bicycle>) :
        RecyclerView.Adapter<BicycleAdapter.BicycleViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BicycleViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_holder, parent, false)
            return BicycleViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: BicycleViewHolder, position: Int) {
            val currentItem = bicycleList[position]
            val imageUri = Uri.parse(currentItem.imageResId)

            holder.brandTextView.text = currentItem.brand
            holder.priceTextView.text = currentItem.price.toString()

            Glide.with(holder.itemView.context)
                .load(imageUri)
                .into(holder.bikeImage)

            holder.readMoreButton.setOnClickListener {
                val description = currentItem.description
                val image = currentItem.imageResId
                val model = currentItem.model
                val brand = currentItem.brand
                val price = currentItem.price

                val intent = Intent(holder.itemView.context, ShowBicycle::class.java)
                intent.putExtra("description", description)
                intent.putExtra("image", image)
                intent.putExtra("model", model)
                intent.putExtra("brand", brand)
                intent.putExtra("price", price)

                holder.itemView.context.startActivity(intent)
            }

            holder.addToCartLayout.setOnClickListener {
                addToCart(currentItem)
            }
        }

        override fun getItemCount() = bicycleList.size

        inner class BicycleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val brandTextView: TextView = itemView.findViewById(R.id.textBrand)
            val priceTextView: TextView = itemView.findViewById(R.id.textPrice)
            val bikeImage: ImageView = itemView.findViewById(R.id.imageBicycle)
            val readMoreButton: Button = itemView.findViewById(R.id.buttonReadMore)
            val addToCartLayout = itemView.findViewById<LinearLayout>(R.id.AddToCart)
        }
    }
}