package com.atwa.remote_ps.device

import com.atwa.remote_ps.user.UserService
import com.atwa.remote_ps.user.payload.AddUserRequest
import com.atwa.remote_ps.user.payload.AuthRequest
import com.atwa.remote_ps.user.payload.LoginResponse
import com.atwa.remote_ps.device.payload.AddDeviceRequest
import com.atwa.remote_ps.device.payload.UpdateDeviceRequest
import com.atwa.remote_ps.shop.Shop
import com.atwa.remote_ps.shop.ShopService
import com.atwa.remote_ps.shop.payload.AddShopRequest
import com.atwa.remote_ps.util.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
class DeviceControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var deviceRepository: DeviceRepository

    @Autowired
    lateinit var deviceService: DeviceService

    @Autowired
    lateinit var shopService: ShopService

    @Autowired
    lateinit var userService: UserService

    @Test
    fun givenValidAdmin_whenAddDevice_thenSuccess() {
        val admin = addAdmin()
        val shop = addShop()
        val request = shop.id?.let { getAddDeviceRequest(it) }
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/device/add_device")
                .header(HttpHeaders.AUTHORIZATION, admin.token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonParser.toJson(objectMapper, request))
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response
        Assertions.assertThat(deviceRepository.findById(1)).isNotNull
    }

    @Test
    fun givenUnprivilegedUser_whenAddDevice_thenFailure() {
        addUser()
        val unprivilegedUser = userService.login(AuthRequest("testUser", "password")).body as LoginResponse
        addAdmin()
        val shop = addShop()
        val request = shop.id?.let { getAddDeviceRequest(it) }
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/device/add_device")
                .header(HttpHeaders.AUTHORIZATION, unprivilegedUser.token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonParser.toJson(objectMapper, request))
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
            .andReturn().response
    }

    @Test
    fun givenDeviceExists_whenGetDeviceById_thenSuccess() {
        val adminUser = addAdmin()
        val shop = addShop()
        val device = shop.id?.let { addDevice(it)}
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/device/get_device/{id}", device?.id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, adminUser.token)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response
    }

    @Test
    fun givenDeviceNotFound_whenGetDeviceById_thenFailure() {
        val adminUser = addAdmin()
        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/device/get_device/{id}", 10)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, adminUser.token)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andReturn().response
        Assertions.assertThat(response.contentAsString).contains("Error: Device not found.")
    }

    @Test
    fun givenDeviceExists_whenUpdateDevice_thenSuccess() {
        val adminUser = addAdmin()
        val shop = addShop()
        val device = shop.id?.let { addDevice(it)}
        val request = UpdateDeviceRequest("Square", BigDecimal(30))
        val response = mockMvc.perform(
            MockMvcRequestBuilders.put("/api/device/update_device/{id}", device?.id)
                .content(JsonParser.toJson(objectMapper, request))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, adminUser.token)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response
        Assertions.assertThat(response.contentAsString).contains("Square")
    }

    @Test
    fun givenDeviceNotFound_whenUpdateDevice_thenFailure() {
        val adminUser = addAdmin()
        val request = UpdateDeviceRequest("Square",  BigDecimal(30))
        val response = mockMvc.perform(
            MockMvcRequestBuilders.put("/api/device/update_device/{id}", 10)
                .content(JsonParser.toJson(objectMapper, request))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, adminUser.token)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andReturn().response
        Assertions.assertThat(response.contentAsString).contains("Error: Device not found.")
    }

    @Test
    fun givenDeviceExists_whenDeleteDevice_thenSuccess() {
        val adminUser = addAdmin()
        val shop = addShop()
        val device = shop.id?.let { addDevice(it)}
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/device/delete_device/{id}", device?.id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, adminUser.token)
        ).andExpect(MockMvcResultMatchers.status().isAccepted)
            .andReturn().response
    }

    @Test
    fun givenDeviceNotFound_whenDeleteDevice_thenFailure() {
        val adminUser = addAdmin()
        val response = mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/device/delete_device/{id}", 10)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, adminUser.token)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andReturn().response
        Assertions.assertThat(response.contentAsString).contains("Error: Device not found.")
    }

    private fun getAddDeviceRequest(shopId: Long) =
        AddDeviceRequest("Square", BigDecimal(20), shopId)

    private fun addUser() {
        val addUserRequest = AddUserRequest("testUser", "password", 1)
        userService.addUser(addUserRequest)
    }

    private fun addAdmin(): LoginResponse {
        val registerRequest = AuthRequest("testAdmin", "password")
        return (userService.register(registerRequest).body as LoginResponse)
    }

    private fun addShop(): Shop {
        val addShopRequest = AddShopRequest( "Square", "ELHadaba AL-Wosta", "Cairo")
        return (shopService.addShop(addShopRequest)).body as Shop
    }

    private fun addDevice(shopId: Long): Device {
        val request = AddDeviceRequest("Square", BigDecimal(20), shopId)
        return (deviceService.addDevice(request)).body as Device
    }

}