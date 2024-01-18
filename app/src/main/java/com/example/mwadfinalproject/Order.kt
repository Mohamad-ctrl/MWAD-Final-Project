package com.example.mwadfinalproject

data class Order(
    val orderId: String? = null,
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val orderDate: String? = null,
    val status: String? = "Confirmed"
    // Add other properties as needed
) {
    // Add a no-argument constructor
    constructor() : this("", emptyList(), 0.0, "", "Confirmed")
}

data class OrderItem(
    val bicycleId: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val quantity: Int = 0,
    val price: Double = 0.0
) {
    // Add a no-argument constructor
    constructor() : this("", "", "", 0, 0.0)
}
