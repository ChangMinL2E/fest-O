package com.example.festo.customer_ui.home

import java.io.Serializable

class RecommendMenu (
    val productId: String,
    val image: String,
    val name : String,
    val price : Int,
): Serializable {}