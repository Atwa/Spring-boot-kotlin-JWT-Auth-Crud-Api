package com.atwa.psManager.device

import com.atwa.psManager.auth.UserRepository
import com.atwa.psManager.auth.UserService
import com.atwa.psManager.auth.payload.AddUserRequest
import com.atwa.psManager.device.payload.AddDeviceRequest
import com.atwa.psManager.shop.payload.AddShopRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import java.math.BigDecimal

class DeviceControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var deviceRepository: DeviceRepository

    @Autowired
    lateinit var deviceService: DeviceService

    @Autowired
    lateinit var shopService: DeviceService

    /*private fun addShop() {
        val addShopRequest = AddShopRequest(, BigDecimal(20), 1)
        shopService.addDevice(addShopRequest)
    }

    private fun addDevice() {
        val addDeviceRequest = AddDeviceRequest("Device1", BigDecimal(20), 1)
        deviceService.addDevice(addDeviceRequest)
    }*/

}