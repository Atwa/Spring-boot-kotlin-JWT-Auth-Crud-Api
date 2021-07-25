package com.atwa.psManager.device.payload

import java.math.BigDecimal

data class AddDeviceRequest(
    val name: String,
    val hourlyPrice: BigDecimal,
    var shopId: Long,
)