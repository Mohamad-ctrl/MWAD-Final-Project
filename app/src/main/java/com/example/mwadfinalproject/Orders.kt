package com.example.mwadfinalproject

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Orders : AppCompatActivity() {
    private lateinit var ordersRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide();
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        ordersRef = FirebaseDatabase.getInstance().getReference("orders").child(userId.orEmpty())
        fetchOrders()
    }
    private fun fetchOrders() {
        val ordersList = mutableListOf<Order>()

        ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (orderSnapshot in snapshot.children) {
                    val order = orderSnapshot.getValue(Order::class.java)
                    order?.let { ordersList.add(it) }
                }

                // Display the fetched orders
                displayOrders(ordersList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Orders Activity", "Error fetching the orders")
            }
        })
    }
    @SuppressLint("SetTextI18n")
    private fun displayOrders(orders: List<Order>) {
        val itemsContainer = findViewById<LinearLayout>(R.id.itemsContainer)
        val inflater = LayoutInflater.from(this@Orders)

        for (order in orders) {
            val orderView = inflater.inflate(R.layout.item_holder_orders, itemsContainer, false)

            val orderDateTextView = orderView.findViewById<TextView>(R.id.textBrandCartItem)
            val orderStatusTextView = orderView.findViewById<TextView>(R.id.textModelCartItem)
            val ShowOrderBtn = orderView.findViewById<LinearLayout>(R.id.ShowOrderBtn)

            orderDateTextView.text = "Ordered on ${order.orderDate}"
            orderStatusTextView.text = "Order Status: ${order.status}"
            ShowOrderBtn.setOnClickListener{
                val intent = Intent(this@Orders, ShowOrder::class.java)
                intent.putExtra("OrderId", order.orderId)
                startActivity(intent)
            }

            itemsContainer.addView(orderView)
        }
    }
    fun homeIcon(view: View) {
        val intent = Intent(this@Orders, MainActivity::class.java)
        startActivity(intent)
    }
    fun profileIcon(view: View) {
        val intent = Intent(this@Orders, Profile::class.java)
        startActivity(intent)
    }
    fun cartIcon(view: View){
        val intent = Intent(this@Orders, Cart::class.java)
        startActivity(intent)
    }
}