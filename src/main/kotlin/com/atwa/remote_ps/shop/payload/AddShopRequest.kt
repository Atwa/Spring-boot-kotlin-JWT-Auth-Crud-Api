package com.atwa.remote_ps.shop.payload

data class AddShopRequest(
    val name: String,
    val area: String,
    val city: String
)