package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.content.Context
import java.text.ParseException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CheckOut : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val ordersRef = currentUser?.uid?.let { database.getReference("users").child(it).child("subOrders") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out)
        val ValidtionText = findViewById<TextView>(R.id.ValidationTextCheckOutAct)
        val cardNumber = findViewById<EditText>(R.id.CardNumberFieldCheckOutAct)
        val CardYear = findViewById<EditText>(R.id.YearFiledCheckOutAct)
        val CardMonth = findViewById<EditText>(R.id.MonthFiledCheckOutAct)
        val CardCCV = findViewById<EditText>(R.id.ccvFiledCheckOutAct).text
        val NameOnCard = findViewById<EditText>(R.id.NameOnCardFieldCheckOutAct).text.toString()
        val TotalPriceText = findViewById<TextView>(R.id.TotalPriceCheckOutAct)
        val PlaceOrderBtn = findViewById<Button>(R.id.PlaceOrderBtn)
        TotalPriceText.text = "Total Price: ${calculateTotalPrice()}$"
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()
        displayCartItems()
        PlaceOrderBtn.setOnClickListener {
            var CardDate: String = "${CardMonth.text}/${CardYear.text}"
            if (isValidCreditCardNumber(cardNumber.text.toString())) {
                if (isValidCreditCardExpiration(CardDate)) {
                    if (isValidCCV(CardCCV.toString())) {
                        placeOrder()
                    } else
                        ValidtionText.text = "please enter a valid CCV number"
                } else
                    ValidtionText.text = "Please enter a valid card expiration date"
            } else
                ValidtionText.text = "Please enter a valid card number"
            Log.e("CheckOut Activity", "Date: $CardDate")
            Log.e("CheckOut Activity", "CVV: $CardCCV")
        }
    }
    private fun placeOrder() {
        // Get user information (You might want to retrieve this from Firebase Auth)
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Check if the user is signed in
        if (userId != null) {
            val cartItems = getCartItems()

            // Check if the cart is not empty
            if (cartItems.isNotEmpty()) {
                val orderItems = cartItems.map {
                    OrderItem(
                        bicycleId = it.id,
                        brand = it.brand,
                        model = it.model,
                        quantity = it.quantity,
                        price = it.price.toDouble()
                    )
                }

                val totalAmount = cartItems.sumByDouble { it.price.toDouble() * it.quantity }

                // Create an Order object
                val order = Order(
                    orderId = generateOrderId(),
                    items = orderItems,
                    totalAmount = totalAmount,
                    orderDate = getCurrentDateTime(),

                    // Add other properties as needed
                )

                // Save the order to Firebase Realtime Database
                saveOrderToDatabase(userId, order)

                // Clear the cart after placing the order
                clearCart()

                // Provide feedback to the user
                Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Your cart is empty. Add items before placing an order.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Handle the case where the user is not signed in
            Toast.makeText(this, "User not signed in. Please sign in to place an order.", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to generate a unique order ID
    private fun generateOrderId(): String {
        return UUID.randomUUID().toString()
    }

    // Function to get the current date and time
    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    // Function to save the order to Firebase Realtime Database
    private fun saveOrderToDatabase(userId: String, order: Order) {
        val database = FirebaseDatabase.getInstance()
        val ordersRef = database.getReference("orders")

        // Save the order under the user's ID
        ordersRef.child(userId).child(order.orderId ?: "").setValue(order)
    }

    // Function to clear the cart after placing an order
    private fun clearCart() {
        val sharedPreferences = getSharedPreferences("Cart", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
    private fun getCartItems(): Set<Bicycle> {
        val sharedPreferences = getSharedPreferences("Cart", Context.MODE_PRIVATE)
        val cartItemsSet = sharedPreferences.getStringSet("cart_items", emptySet()) ?: emptySet()
        return cartItemsSet.map { Gson().fromJson(it, Bicycle::class.java) }.toSet()
    }
    fun homeIcon(view: View) {
        val intent = Intent(this@CheckOut, MainActivity::class.java)
        startActivity(intent)
    }
    fun profileIcon(view: View) {
        val intent = Intent(this@CheckOut, Profile::class.java)
        startActivity(intent)
    }
    fun cartIcon(view: View){
        val intent = Intent(this@CheckOut, Cart::class.java)
        startActivity(intent)
    }
    @SuppressLint("SetTextI18n")
    private fun displayCartItems() {
        val cartItems = getCartItems()

        val itemsContainer = findViewById<LinearLayout>(R.id.itemsContainer)
        val inflater = LayoutInflater.from(this@CheckOut)

        for (item in cartItems) {
            val itemView = inflater.inflate(R.layout.check_out_list, itemsContainer, false)
            val itemName = itemView.findViewById<TextView>(R.id.itemName)
            val itemQuantity = itemView.findViewById<TextView>(R.id.itemQuantity)

            itemName.text = item.brand // Set the name of the item
            itemQuantity.text = "X ${item.quantity}" // Set the quantity of the item

            itemsContainer.addView(itemView)
        }
    }

    fun isValidCreditCardExpiration(expirationDate: String): Boolean {
        try {
            // Define the expected date format
            val dateFormat = SimpleDateFormat("MM/yy", Locale.US)
            dateFormat.isLenient = false  // Disable lenient parsing

            // Parse the expiration date string into a Date object
            val expiration = dateFormat.parse(expirationDate)

            // Get the current date
            val currentDate = Date()

            // Check if the expiration date is in the future and not before the current date
            return expiration != null && expiration.after(currentDate)
        } catch (e: ParseException) {
            // Handle parsing exceptions, such as invalid date format
            return false
        }
    }

    fun isValidCreditCardNumber(cardNumber: String): Boolean {
        // Clean the input by removing non-digit characters
        val cleanedCardNumber = cardNumber.replace("\\D".toRegex(), "")

        // Check if the card number contains only digits and has a valid length
        if (!cleanedCardNumber.matches(Regex("\\d+")) || cleanedCardNumber.length !in 13..19) {
            return false
        }

        // Apply the Luhn algorithm to validate the card number
        var sum = 0
        var alternate = false
        for (i in cleanedCardNumber.length - 1 downTo 0) {
            var digit = cleanedCardNumber[i].toString().toInt()
            if (alternate) {
                digit *= 2
                if (digit > 9) {
                    digit -= 9
                }
            }
            sum += digit
            alternate = !alternate
        }

        return sum % 10 == 0
    }
    fun isValidCCV(ccv: String): Boolean {
        // Check if the CCV contains only digits and has a valid length
        return ccv.matches(Regex("\\d+")) && ccv.length in 3..4
    }
    private fun calculateTotalPrice(): Int {
        val cartItems = getCartItems()
        var totalPrice = 0

        // Iterate through the cart items and calculate the total price
        for (item in cartItems) {
            totalPrice += item.price * item.quantity
        }
        return totalPrice
    }


}