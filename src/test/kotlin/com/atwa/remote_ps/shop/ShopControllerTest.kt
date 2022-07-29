package com.atwa.remote_ps.shop

import com.atwa.remote_ps.user.UserRepository
import com.atwa.remote_ps.user.UserService
import com.atwa.remote_ps.user.payload.AddUserRequest
import com.atwa.remote_ps.user.payload.AuthRequest
import com.atwa.remote_ps.user.payload.LoginResponse
import com.atwa.remote_ps.shop.payload.AddShopRequest
import com.atwa.remote_ps.shop.payload.UpdateShopRequest
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

    @Autowired
    lateinit var userRepository: UserRepository

    @Test
    fun givenValidAdmin_whenAddShop_thenSuccess() {
        val admin = addAdmin()
        val response = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/shop/add_Shop")
                .header(HttpHeaders.AUTHORIZATION, admin.token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonParser.toJson(objectMapper, addShopRequest))
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response
        Assertions.assertThat(shopRepository.findById(1)).isNotNull
    }

    @Test
    fun givenUnprivilegedUser_whenAddShop_thenFailure() {
        addUser()
        val unprivilegedUser = userService.login(AuthRequest("testUser", "password")).body as LoginResponse
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/shop/add_Shop")
                .header(HttpHeaders.AUTHORIZATION, unprivilegedUser.token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonParser.toJson(objectMapper, addShopRequest))
        ).andExpect(MockMvcResultMatchers.status().isForbidden)
            .andReturn().response
    }

    @Test
    fun givenShopExists_whenGetShopById_thenSuccess() {
        val adminUser = addAdmin()
        val shop = addShop(adminUser.id)
        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/shop/get_shop")
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
            MockMvcRequestBuilders.get("/api/shop/get_shop" )
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, adminUser.token)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andReturn().response
        Assertions.assertThat(response.contentAsString).contains("Error: User doesn't have shop.")
    }

    @Test
    fun givenShopExists_whenUpdateShop_thenSuccess() {
        val adminUser = addAdmin()
        val shop = addShop(adminUser.id)
        val request = UpdateShopRequest("Grinta", "Hay EL-Gamaa", "Mansoura")
        val response = mockMvc.perform(
            MockMvcRequestBuilders.put("/api/shop/update_shop")
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
        val request = UpdateShopRequest("Grinta", "Hay EL-Gamaa", "Mansoura")
        val response = mockMvc.perform(
            MockMvcRequestBuilders.put("/api/shop/update_shop")
                .content(JsonParser.toJson(objectMapper, request))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, adminUser.token)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andReturn().response
        Assertions.assertThat(response.contentAsString).contains("Error: User doesn't have shop.")
    }

    @Test
    fun givenShopExists_whenDeleteShop_thenSuccess() {
        val adminUser = addAdmin()
        val shop = addShop(adminUser.id)
        val request = UpdateShopRequest("Grinta", "Hay EL-Gamaa", "Mansoura")
        val response = mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/shop/delete_shop")
                .content(JsonParser.toJson(objectMapper, request))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, adminUser.token)
        ).andExpect(MockMvcResultMatchers.status().isAccepted)
            .andReturn().response
    }

    @Test
    fun givenShopNotFound_whenDeleteShop_thenFailure() {
        val adminUser = addAdmin()
        val request = UpdateShopRequest("Grinta", "Hay EL-Gamaa", "Mansoura")
        val response = mockMvc.perform(
            MockMvcRequestBuilders.delete("/api/shop/delete_shop")
                .content(JsonParser.toJson(objectMapper, request))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, adminUser.token)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andReturn().response
        Assertions.assertThat(response.contentAsString).contains("Error: User doesn't have shop.")
    }

    private val addShopRequest = AddShopRequest( "Square", "ELHadaba AL-Wosta", "Cairo")

    private fun addUser() {
        val addUserRequest = AddUserRequest("testUser", "password", 1)
        userService.addUser(addUserRequest)
    }

    private fun addAdmin(): LoginResponse {
        val registerRequest = AuthRequest("testAdmin", "password")
        return (userService.register(registerRequest).body as LoginResponse)
    }

    private fun addShop(adminId: Long): Shop {
        return (shopService.addShop(addShopRequest)).body as Shop
    }
}