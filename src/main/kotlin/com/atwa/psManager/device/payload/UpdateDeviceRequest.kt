package com.atwa.psManager.device.payload

import java.math.BigDecimal

data class UpdateDeviceRequest(
    val name: String,
    val hourlyPrice: BigDecimal
)