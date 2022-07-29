package com.atwa.remote_ps.device.payload

import java.math.BigDecimal

data class AddDeviceRequest(
    val name: String,
    val hourlyPrice: BigDecimal,
    var shopId: Long,
)