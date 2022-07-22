package com.atwa.psManager.shop

import com.atwa.psManager.auth.UserService
import com.atwa.psManager.auth.payload.AddUserRequest
import com.atwa.psManager.auth.payload.AuthRequest
import com.atwa.psManager.auth.payload.LoginResponse
import com.atwa.psManager.shop.payload.AddShopRequest
import com.atwa.psManager.shop.payload.EditShopRequest
import com.atwa.psManager.util.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
class ShopControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var shopRepository: ShopRepository

    @Autowired
    lateinit var shopService: ShopService

    @Autowired
    lateinit var userService: UserService

    @Test
    fun givenValidAdmin_whenAddShop_thenSuccess() {
        val admin = addAdmin()
        val request = getAddShopRequest(admin.id)
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/shop/add_Shop")
                .header(HttpHeaders.AUTHORIZATION, admin.token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonParser.toJson(objectMapper, request))
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response
        Assertions.assertThat(shopRepository.findById(1)).isNotNull
    }

    @Test
    fun givenUnprivilegedUser_whenAddShop_thenFailure() {
        addUser()
        val unprivilegedUser = userService.login(AuthRequest("testUser", "password")).body as LoginResponse
        val request = getAddShopRequest(unprivilegedUser.id)
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/shop/add_Shop")
                .header(HttpHeaders.AUTHORIZATION, unprivilegedUser.token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonParser.toJson(objectMapper, request))
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
            .andReturn().response
    }

    @Test
    fun givenShopExists_whenGetShopById_thenSuccess() {
        val adminUser = addAdmin()
        val shop = addShop(adminUser.id)
        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/shop/get_shop/{id}", shop.id)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, adminUser.token)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response
    }

    @Test
    fun givenShopNotFound_whenGetShopById_thenFailure() {
        val adminUser = addAdmin()
        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/shop/get_shop/{id}", 10)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, adminUser.token)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andReturn().response
        Assertions.assertThat(response.contentAsString).contains("Error: Shop not found.")
    }

    @Test
    fun givenShopExists_whenUpdateShop_thenSuccess() {
        val adminUser = addAdmin()
        val shop = addShop(adminUser.id)
        val request = EditShopRequest("Grinta", "Hay EL-Gamaa", "Mansoura")
        val response = mockMvc.perform(
            MockMvcRequestBuilders.put("/api/shop/update_shop/{id}", shop.id)
                .content(JsonParser.toJson(objectMapper, request))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, adminUser.token)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response
        Assertions.assertThat(response.contentAsString).contains("Grinta")
    }

    @Test
    fun givenShopNotFound_whenUpdateShop_thenFailure() {
        val adminUser = addAdmin()
        val request = EditShopRequest("Grinta", "Hay EL-Gamaa", "Mansoura")
        val response = mockMvc.perform(
            MockMvcRequestBuilders.put("/api/shop/update_shop/{id}", 10)
                .content(JsonParser.toJson(objectMapper, request))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, adminUser.token)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andReturn().response
        Assertions.assertThat(response.contentAsString).contains("Error: Shop not found.")
    }

    @Test
    fun givenShopExists_whenDeleteShop_thenSuccess() {
        val adminUser = addAdmin()
        val shop = addShop(adminUser.id)
        val request = EditShopRequest("Grinta", "Hay EL-Gamaa", "Mansoura")
        val response = mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/shop/delete_shop/{id}", shop.id)
                .content(JsonParser.toJson(objectMapper, request))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, adminUser.token)
        ).andExpect(MockMvcResultMatchers.status().isAccepted)
            .andReturn().response
    }

    @Test
    fun givenShopNotFound_whenDeleteShop_thenFailure() {
        val adminUser = addAdmin()
        val request = EditShopRequest("Grinta", "Hay EL-Gamaa", "Mansoura")
        val response = mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/shop/delete_shop/{id}", 10)
                .content(JsonParser.toJson(objectMapper, request))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, adminUser.token)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andReturn().response
        Assertions.assertThat(response.contentAsString).contains("Error: Shop not found.")
    }

    private fun getAddShopRequest(adminId: Long) =
        AddShopRequest(adminId, "Square", "ELHadaba AL-Wosta", "Cairo")

    private fun addUser() {
        val addUserRequest = AddUserRequest("testUser", "password", 1)
        userService.addUser(addUserRequest)
    }

    private fun addAdmin(): LoginResponse {
        val registerRequest = AuthRequest("testAdmin", "password")
        return (userService.register(registerRequest).body as LoginResponse)
    }

    private fun addShop(adminId: Long): Shop {
        val addShopRequest = AddShopRequest(adminId, "Square", "ELHadaba AL-Wosta", "Cairo")
        return (shopService.addShop(addShopRequest)).body as Shop
    }
}