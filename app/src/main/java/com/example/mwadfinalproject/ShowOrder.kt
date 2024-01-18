package com.example.mwadfinalproject

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowOrder : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_order)
        val orderId = intent.getStringExtra("OrderId")
        val totalPaidText = findViewById<TextView>(R.id.TotalPaid)
        val OrderDateText = findViewById<TextView>(R.id.OrderDateShowOrderAct)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide();
        displayOrderItems(orderId.toString())
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        getOrderDetails(orderId.toString(), userId.toString(),
            onSuccess = { orderDetails ->
                // Use the order details as needed
                totalPaidText.text = "Total paid: ${orderDetails.totalAmount}$"
                OrderDateText.text = "Ordered On: ${orderDetails.orderDate}"
            },
            onError = { error ->
                // Handle the error
                Log.e("Show Order Activity", "Error: ${error.message}")
            }
        )
    }
    @SuppressLint("SetTextI18n")
    private fun displayOrderItems(orderId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val itemsContainer = findViewById<LinearLayout>(R.id.itemsContainerShowOrdersAct)
        val inflater = LayoutInflater.from(this@ShowOrder)

        // Reference to the items for the specific order
        val orderItemsRef = FirebaseDatabase.getInstance().getReference("orders").child(userId.orEmpty()).child(orderId).child("items")

        orderItemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(Bicycle::class.java)

                    // Assuming Bicycle class has 'brand' and 'quantity' properties
                    item?.let {
                        val itemView = inflater.inflate(R.layout.check_out_list, itemsContainer, false)
                        val itemName = itemView.findViewById<TextView>(R.id.itemName)
                        val itemQuantity = itemView.findViewById<TextView>(R.id.itemQuantity)

                        itemName.text = it.brand // Set the name of the item
                        itemQuantity.text = "X ${it.quantity}" // Set the quantity of the item

                        itemsContainer.addView(itemView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }
    data class OrderDetails(
        val orderDate: String,
        val totalAmount: Double
    )

    fun getOrderDetails(orderId: String, userId: String, onSuccess: (OrderDetails) -> Unit, onError: (DatabaseError) -> Unit) {
        // Reference to the specific order
        val orderRef = FirebaseDatabase.getInstance().getReference("orders").child(userId).child(orderId)

        // Add a listener to retrieve the order details
        orderRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check if the order exists
                if (snapshot.exists()) {
                    // Get the order date and total amount from the order
                    val orderDate = snapshot.child("orderDate").getValue(String::class.java)
                    val totalAmount = snapshot.child("totalAmount").getValue(Double::class.java)
                    val OrderStatus = snapshot.child("")

                    // Call the onSuccess callback with the order details
                    if (orderDate != null && totalAmount != null) {
                        val orderDetails = OrderDetails(orderDate, totalAmount)
                        onSuccess(orderDetails)
                    } else {
                        // Handle the case where orderDate or totalAmount is null
                        onError(DatabaseError.fromException(Exception("Order details are null")))
                    }
                } else {
                    // Handle the case where the order does not exist
                    onError(DatabaseError.fromException(Exception("Order does not exist")))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
                onError(error)
            }
        })
    }


}