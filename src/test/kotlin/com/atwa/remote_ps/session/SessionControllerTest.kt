package com.atwa.remote_ps.session

import com.atwa.remote_ps.user.UserService
import com.atwa.remote_ps.user.payload.AuthRequest
import com.atwa.remote_ps.user.payload.LoginResponse
import com.atwa.remote_ps.device.Device
import com.atwa.remote_ps.device.DeviceRepository
import com.atwa.remote_ps.device.DeviceService
import com.atwa.remote_ps.device.payload.AddDeviceRequest
import com.atwa.remote_ps.shop.Shop
import com.atwa.remote_ps.shop.ShopService
import com.atwa.remote_ps.shop.payload.AddShopRequest
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
class SessionControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var deviceRepository: DeviceRepository

    @Autowired
    lateinit var deviceService: DeviceService

    @Autowired
    lateinit var shopService: ShopService

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var sessionService: SessionService

    @Autowired
    lateinit var sessionRepository: SessionRepository

    @Test
    fun givenInActiveDevice_whenStartSession_thenSuccess() {
        val admin = addAdmin()
        val shop = addShop()
        val device = shop.id?.let { addDevice(it) }
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/session/start_session/{id}", device?.id)
                .header(HttpHeaders.AUTHORIZATION, admin.token)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response
        Assertions.assertThat(deviceRepository.findById(1)).isNotNull
    }

    @Test
    fun givenActiveDevice_whenStartSession_thenFailure() {
        val admin = addAdmin()
        val shop = addShop()
        val device = shop.id?.let {
            addDevice(it).also { device ->
                device.isActive = true
                deviceRepository.save(device)
            }
        }
        val response = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/session/start_session/{id}", device?.id)
                .header(HttpHeaders.AUTHORIZATION, admin.token)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andReturn().response
        Assertions.assertThat(response.contentAsString).contains("Error: Device has another session already running.")
    }

    @Test
    fun givenNonExistDevice_whenStartSession_thenFailure() {
        val admin = addAdmin()
        addShop()
        val response = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/session/start_session/{id}", 10)
                .header(HttpHeaders.AUTHORIZATION, admin.token)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andReturn().response
        Assertions.assertThat(response.contentAsString).contains("Error: Device not found.")
    }

    @Test
    fun givenActiveSession_whenEndSession_thenSuccess() {
        val admin = addAdmin()
        val session =
            addShop().id?.let { shopId -> addDevice(shopId) }?.id?.let { deviceId -> addSession(deviceId) }
        mockMvc.perform(
            MockMvcRequestBuilders.put("/api/session/end_session/{id}", session?.id)
                .header(HttpHeaders.AUTHORIZATION, admin.token)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response
        Assertions.assertThat(sessionRepository.findById(1)).isNotNull
    }


    @Test
    fun givenOverSession_whenEndSession_thenFailure() {
        val admin = addAdmin()
        val session =
            addShop().id?.let { shopId -> addDevice(shopId) }?.id?.let { deviceId -> addSession(deviceId) }
        session?.id?.let { endSession(it) }
        val response = mockMvc.perform(
            MockMvcRequestBuilders.put("/api/session/end_session/{id}", session?.id)
                .header(HttpHeaders.AUTHORIZATION, admin.token)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andReturn().response
        Assertions.assertThat(response.contentAsString).contains("Error: Session has already ended.")
    }

    @Test
    fun givenNonExistSession_whenEndSession_thenFailure() {
        val admin = addAdmin()
        val response = mockMvc.perform(
            MockMvcRequestBuilders.put("/api/session/end_session/{id}", 10)
                .header(HttpHeaders.AUTHORIZATION, admin.token)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andReturn().response
        Assertions.assertThat(response.contentAsString).contains("Error: Session not found.")
    }

    @Test
    fun givenSessionExists_whenGetSessionById_thenSuccess() {
        val admin = addAdmin()
        val session =
            addShop().id?.let { shopId -> addDevice(shopId) }?.id?.let { deviceId -> addSession(deviceId) }
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/session/get_session/{id}", session?.id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, admin.token)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response
    }

    @Test
    fun givenSessionNotFound_whenGetSessionById_thenFailure() {
        val adminUser = addAdmin()
        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/session/get_session/{id}", 10)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, adminUser.token)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andReturn().response
        Assertions.assertThat(response.contentAsString).contains("Error: Session not found.")
    }

    @Test
    fun givenSessionExists_whenDeleteSession_thenSuccess() {
        val admin = addAdmin()
        val session =
            addShop().id?.let { shopId -> addDevice(shopId) }?.id?.let { deviceId -> addSession(deviceId) }
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/session/delete_session/{id}", session?.id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, admin.token)
        ).andExpect(MockMvcResultMatchers.status().isAccepted)
            .andReturn().response
    }

    @Test
    fun givenSessionNotFound_whenDeleteSession_thenFailure() {
        val adminUser = addAdmin()
        val response = mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/session/delete_session/{id}", 10)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, adminUser.token)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andReturn().response
        Assertions.assertThat(response.contentAsString).contains("Error: Session not found.")
    }

    private fun addAdmin(): LoginResponse {
        val registerRequest = AuthRequest("testAdmin", "password")
        return (userService.register(registerRequest).body as LoginResponse)
    }

    private fun addShop(): Shop {
        val addShopRequest = AddShopRequest("Square", "ELHadaba AL-Wosta", "Cairo")
        return (shopService.addShop(addShopRequest)).body as Shop
    }

    private fun addDevice(shopId: Long): Device {
        val request = AddDeviceRequest("Square", BigDecimal(20), shopId)
        return (deviceService.addDevice(request)).body as Device
    }

    private fun addSession(deviceId: Long): Session {
        return (sessionService.startSession(deviceId)).body as Session
    }

    private fun endSession(sessionId: Long) {
        sessionService.endSession(sessionId)
    }

}