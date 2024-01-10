package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.Image
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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson

class Cart : AppCompatActivity() {
    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartItems: MutableList<Bicycle>
    private lateinit var auth: FirebaseAuth
    private lateinit var authListener: FirebaseAuth.AuthStateListener

    inner class CartAdapter(private val cartItemList: MutableList<Bicycle>) :
        RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_holder_cart, parent, false)
            return CartViewHolder(itemView)
        }

      private fun getCartItemsFromSharedPreferences(): MutableList<Bicycle> {
          val sharedPreferences = getSharedPreferences("Cart", Context.MODE_PRIVATE)
          val cartItemsJsonSet = sharedPreferences.getStringSet("cart_items", emptySet())

          val gson = Gson()
          val cartItems = mutableListOf<Bicycle>()

          cartItemsJsonSet?.forEach { jsonItem ->
              val item = gson.fromJson(jsonItem, Bicycle::class.java)
              cartItems.add(item)
          }

          return cartItems
      }
      @SuppressLint("NotifyDataSetChanged")
      fun removeItem(position: Int, context: Context) {
          cartItemList.removeAt(position)
          notifyItemRemoved(position)
          notifyItemRangeChanged(position, cartItemList.size)
          saveCartItemsToSharedPreferences(cartItemList)
          (context as Cart).updateCartItems(cartItemList)
      }

      private fun saveCartItemsToSharedPreferences(cartItems: List<Bicycle>) {
          val sharedPreferences = getSharedPreferences("Cart", Context.MODE_PRIVATE)
          val editor = sharedPreferences.edit()
          val gson = Gson()
          val cartItemsJsonSet = cartItems.map { gson.toJson(it) }.toSet()
          editor.putStringSet("cart_items", cartItemsJsonSet)
          editor.apply()
      }

        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        override fun onBindViewHolder(holder: CartViewHolder, position: Int) {

            val currentItem = cartItemList[position]
            val imageUri = Uri.parse(currentItem.imageResId)
            val PriceToString = currentItem.price.toString()

            // Set the data to your views in the ViewHolder
            holder.textBrand.text = currentItem.brand
            holder.textModel.text = currentItem.model
            holder.textPrice.text = "$PriceToString$"
            holder.quantityTextView.text = currentItem.quantity.toString()
            Glide.with(holder.itemView.context)
                .load(imageUri)
                .into(holder.BikeImage)



            holder.increaseButton.setOnClickListener {
                val updatedItem = cartItemList[position].copy(quantity = cartItemList[position].quantity + 1)
                cartItemList[position] = updatedItem
                saveCartItemsToSharedPreferences(cartItemList) // Save the updated list
                updateCartItems(cartItemList) // Notify the adapter about the dataset change
                updateTotalPrice()
                Toast.makeText(this@Cart, "Item's quantity has been increased", Toast.LENGTH_SHORT).show()
            }

            holder.decreaseButton.setOnClickListener {
                if(cartItemList[position].quantity == 1) {
                    Toast.makeText(this@Cart, "minimum quantity is 1", Toast.LENGTH_SHORT).show()
                } else {
                    if (cartItemList[position].quantity > 0) {
                        val updatedItem =
                            cartItemList[position].copy(quantity = cartItemList[position].quantity - 1)
                        cartItemList[position] = updatedItem
                        saveCartItemsToSharedPreferences(cartItemList) // Save the updated list
                        updateCartItems(cartItemList) // Notify the adapter about the dataset change
                        updateTotalPrice()
                        Toast.makeText(
                            this@Cart,
                            "Item's quantity has been decreased",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }




            holder.deleteButton.setOnClickListener {
                removeItem(position, holder.itemView.context)
                updateTotalPrice()
            }
        }

        override fun getItemCount() = cartItemList.size

        inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textBrand: TextView = itemView.findViewById(R.id.textBrandCartItem)
            val textModel: TextView = itemView.findViewById(R.id.textModelCartItem)
            val textPrice: TextView = itemView.findViewById(R.id.textPriceCartItem)
            val BikeImage: ImageView = itemView.findViewById(R.id.imageCartItem)
            val decreaseButton: ImageView = itemView.findViewById(R.id.imageDecreaseQuantity)
            val increaseButton: ImageView = itemView.findViewById(R.id.imageIncreaseQuantity)
            val deleteButton: ImageView = itemView.findViewById(R.id.imageDeleteCartItem)
            val quantityTextView: TextView = itemView.findViewById(R.id.textQuantityCartItem)
            // Other views in the cart item layout
        }
    }
    fun updateCartItems(updatedCartItems: MutableList<Bicycle>) {
        cartItems.clear()
        cartItems.addAll(updatedCartItems)
        cartAdapter.notifyDataSetChanged() // Notify the adapter about the dataset change
        updateTotalPrice()
        Log.d("CartActivity", "updateCartItems() called")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        // Initialize RecyclerView and its adapter
        cartRecyclerView = findViewById(R.id.recyclerViewCartAct)
        cartAdapter = CartAdapter(getCartItems().toMutableList())
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        cartRecyclerView.adapter = cartAdapter
        cartItems = getCartItems().toMutableList() // Initialize cartItems
        updateTotalPrice()
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide();
        val checkOutButton = findViewById<Button>(R.id.checkOutButton)

        checkOutButton.setOnClickListener {
            auth = FirebaseAuth.getInstance()
            authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                if (user == null) {
                    // User is not logged in, navigate to the Login or Sign Up activity
                    val intent = Intent(this@Cart, LoginOrSignUp::class.java)
                    startActivity(intent)
                } else {
                    // User is logged in, navigate to the Checkout activity
                    val intent = Intent(this@Cart, CheckOut::class.java)
                    intent.putExtra("TotalPrice", calculateTotalPrice())
                    intent.putExtra("CartItems", ArrayList(getCartItems()))
                    startActivity(intent)
                }
            }
            // Add the AuthStateListener to check the user's authentication status
            auth.addAuthStateListener(authListener)
        }

    }
    private fun calculateTotalPrice(): Int {
        var totalPrice = 0

        // Iterate through the cart items and calculate the total price
        for (item in cartItems) {
            totalPrice += item.price * item.quantity
        }
        Log.e("Cart", "calculate Total Price is called")
        return totalPrice
    }

    // Function to update the total price in the UI
    @SuppressLint("SetTextI18n")
    private fun updateTotalPrice() {
        val total = calculateTotalPrice()
        val totalPriceTextView = findViewById<TextView>(R.id.totalPriceTextView)
        totalPriceTextView.text = "Total Price: $total$" // Update the TextView with the total price
        Log.e("Cart", "update Total Is Called")
    }

        fun homeIcon(view: View) {
            val intent = Intent(this@Cart, MainActivity::class.java)
            startActivity(intent)
    }
    fun profileIcon(view: View) {
        val intent = Intent(this@Cart, Profile::class.java)
        startActivity(intent)
    }
//    fun cartIcon(view: View){
//        val intent = Intent(this@Cart, Cart::class.java)
//        startActivity(intent)
//    }


    // Retrieve cart items from SharedPreferences
    private fun getCartItems(): MutableList<Bicycle> {
        val sharedPreferences = getSharedPreferences("Cart", Context.MODE_PRIVATE)
        val cartItemsJsonSet = sharedPreferences.getStringSet("cart_items", emptySet())

        val gson = Gson()
        val cartItems = mutableListOf<Bicycle>()

        cartItemsJsonSet?.forEach { jsonItem ->
            val item = gson.fromJson(jsonItem, Bicycle::class.java)
            cartItems.add(item)
        }

        return cartItems
    }


}