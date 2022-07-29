package com.atwa.remote_ps.device.payload

import java.math.BigDecimal

data class UpdateDeviceRequest(
    val name: String,
    val hourlyPrice: BigDecimal
)