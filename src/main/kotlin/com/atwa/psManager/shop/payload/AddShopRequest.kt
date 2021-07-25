package com.atwa.psManager.shop.payload

import java.math.BigDecimal

data class AddShopRequest(
    val userId: Long,
    val name: BigDecimal,
    val city: String,
    val area: String,
)