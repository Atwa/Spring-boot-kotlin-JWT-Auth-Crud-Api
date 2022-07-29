package com.atwa.remote_ps.user

import com.atwa.remote_ps.user.model.Role
import com.atwa.remote_ps.user.model.User
import com.atwa.remote_ps.user.payload.AddUserRequest
import com.atwa.remote_ps.user.payload.AuthRequest
import com.atwa.remote_ps.user.payload.ChangePasswordRequest
import com.atwa.remote_ps.user.payload.LoginResponse
import com.atwa.remote_ps.shop.Shop
import com.atwa.remote_ps.shop.ShopService
import com.atwa.remote_ps.shop.payload.AddShopRequest
import com.atwa.remote_ps.util.JsonParser.fromJson
import com.atwa.remote_ps.util.JsonParser.toJson
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var shopService: ShopService

    @Autowired
    lateinit var passwordEncoder: BCryptPasswordEncoder

    val request = AuthRequest("Test_user", "Test12345_")

    @Test
    fun givenValidCredentials_whenRegister_thenSuccess() {
        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request)))
            .andExpect(status().isOk)
            .andReturn().response
        assertThat(userRepository.findByUsername("Test_user")).isNotNull
    }

    @Test
    fun givenTakenUsername_whenRegister_thenBadRequest() {
        userService.register(request)
        val response = mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request)))
            .andExpect(status().isBadRequest)
            .andReturn().response
        assertThat(response.contentAsString).contains("Username is already taken")
    }

    @Test
    fun givenUnacceptedCredentials_whenRegister_thenBadRequest() {
        val unacceptedRequest = AuthRequest("tt", "dsds")
        val response = mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, unacceptedRequest)))
            .andExpect(status().isBadRequest)
            .andReturn().response
        assertThat(response.contentAsString).contains("password size must be between 8 and 40")
        assertThat(response.contentAsString).contains("username size must be between 3 and 20")
    }

    @Test
    fun givenValidCredentials_whenLogin_thenSuccess() {
        userService.register(request)
        val response = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request)))
            .andExpect(status().isOk)
            .andReturn().response
        val loginResponse = fromJson(objectMapper, response.contentAsString, LoginResponse::class.java)
        assertThat(loginResponse.id).isNotNull
        assertThat(loginResponse.username).isNotNull
        assertThat(loginResponse.token).isNotNull
        assertThat(loginResponse.isAdmin).isTrue
    }

    @Test
    fun givenNonExistedUser_whenLogin_thenBadRequest() {
        val response = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, request)))
            .andExpect(status().isBadRequest)
            .andReturn().response
        assertThat(response.contentAsString).contains("Not found")
    }

    @Test
    fun givenWrongCredentials_whenLogin_thenUnAuthorized() {
        val invalidERequest = AuthRequest("Test_user", "InvalidPassword")
        userService.register(request)
        val response = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, invalidERequest)))
            .andExpect(status().isUnauthorized)
            .andReturn().response
        assertThat(response.contentAsString).contains("Invalid credentials")
    }

    @Test
    fun givenDisabledUser_whenLogin_thenForbidden() {
        val disabledRequest = AuthRequest("DisabledUser", "InvalidPassword")
        userRepository.save(
            User(
                username = disabledRequest.username,
                password = passwordEncoder.encode(disabledRequest.password),
                roles = hashSetOf(Role.ROLE_USER, Role.ROLE_ADMIN),
                enabled = false
            ))
        val response = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(objectMapper, disabledRequest)))
            .andExpect(status().isForbidden)
            .andReturn().response
        assertThat(response.contentAsString).contains("The user is not enabled")
    }

    @Test
    fun givenAdminRequest_whenAddUser_thenSuccess() {
        val adminUser = addAdmin()

        val addUserRequest = AddUserRequest("testUser", "password", 1)

        val response = mockMvc.perform(post("/api/auth/add_user")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, adminUser.token)
            .content(toJson(objectMapper, addUserRequest)))
            .andExpect(status().isOk)
            .andReturn().response

        assertThat(userRepository.findByUsername(addUserRequest.username).get().roles == hashSetOf(Role.ROLE_USER))
    }

    @Test
    fun givenAdminRequest_whenAddAdmin_thenSuccess() {
        val adminUser = addAdmin()
        val addUserRequest = AddUserRequest("testAdmin2", "password", 1)
        val response = mockMvc.perform(post("/api/auth/add_admin")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, adminUser.token)
            .content(toJson(objectMapper, addUserRequest)))
            .andExpect(status().isOk)
            .andReturn().response

        assertThat(userRepository.findByUsername(addUserRequest.username).get().roles == hashSetOf(Role.ROLE_USER,Role.ROLE_ADMIN))
    }

    @Test
    fun givenUnprivilegedUser_whenAddUser_thenForbidden() {
        addUser()
        val unprivilegedUser = userService.login(AuthRequest("testUser", "password")).body as LoginResponse

        val addSecondaryUser = AddUserRequest("testUser2", "password", 1)

        mockMvc.perform(post("/api/auth/add_user")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, unprivilegedUser.token)
            .content(toJson(objectMapper, addSecondaryUser)))
            .andExpect(status().isForbidden)
            .andReturn().response
    }

    @Test
    fun givenTakenUser_whenAddUser_thenBadRequest() {
        val adminUser = addAdmin()

        val addUserRequest = AddUserRequest("testUser", "password", 1)
        userService.addUser(addUserRequest)

        val response = mockMvc.perform(post("/api/auth/add_user")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, adminUser.token)
            .content(toJson(objectMapper, addUserRequest)))
            .andExpect(status().isBadRequest)
            .andReturn().response

        assertThat(response.contentAsString).contains("Username is already taken")
    }

    @Test
    fun givenAdminRequest_whenSuspendUser_thenSuccess() {
        val adminUser = addAdmin()
        val shop = addShop(adminUser.id)
        shop.id?.let { addUser(it) }
        val addedUserId = userRepository.findByUsername("testUser").get().id
        val response = mockMvc.perform(put("/api/auth/suspend_user/{id}", addedUserId)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, adminUser.token))
            .andExpect(status().isOk)
            .andReturn().response

        assertThat(!userRepository.findByUsername("testUser").get().enabled)
    }

    @Test
    fun givenUserNotFound_whenSuspendUser_thenBadRequest() {
        val adminUser = addAdmin()
        val response = mockMvc.perform(put("/api/auth/suspend_user/{id}", 999)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, adminUser.token))
            .andExpect(status().isBadRequest)
            .andReturn().response

        assertThat(response.contentAsString).contains("Error: User not found.")
    }

    @Test
    fun givenAdminInvalidCredentials_whenChangeAdminPassword_thenUnAuthorized() {
        val adminUser = addAdmin()
        val changePasswordRequest = ChangePasswordRequest( "wrongOldPassword", "newPassword")
        val response = mockMvc.perform(post("/api/auth/change_password")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, adminUser.token)
            .content(toJson(objectMapper, changePasswordRequest)))
            .andExpect(status().isUnauthorized)
            .andReturn().response

        assertThat(response.contentAsString).contains("Invalid credentials")
    }

    @Test
    fun givenUserChangeUserPassword_whenChangeUserPassword_thenSuccess() {
        addUser()
        val addedUser = loginAddedUser()
        val changePasswordRequest = ChangePasswordRequest( "password", "newPassword")
        val response = mockMvc.perform(post("/api/auth/change_password")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, addedUser.token)
            .content(toJson(objectMapper, changePasswordRequest)))
            .andExpect(status().isOk)
            .andReturn().response
    }

    @Test
    fun givenAdminChangeUserPassword_whenChangeUserPassword_thenSuccess() {
        val adminUser = addAdmin()
        addUser()
        val addedUser= userRepository.findByUsername("testUser").get()
        val changePasswordRequest = ChangePasswordRequest("password", "newPassword")
        val response = mockMvc.perform(post("/api/auth/change_password")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, adminUser.token)
            .content(toJson(objectMapper, changePasswordRequest)))
            .andExpect(status().isOk)
            .andReturn().response
    }

    @Test
    fun givenUserInvalidCredentials_whenChangeUserPassword_thenUnAuthorized() {
        addUser()
        val addedUser = loginAddedUser()
        val changePasswordRequest = ChangePasswordRequest( "wrongOldPassword", "newPassword")
        val response = mockMvc.perform(post("/api/auth/change_password")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, addedUser.token)
            .content(toJson(objectMapper, changePasswordRequest)))
            .andExpect(status().isUnauthorized)
            .andReturn().response

        assertThat(response.contentAsString).contains("Invalid credentials")
    }


    private fun addAdmin(): LoginResponse {
        val registerRequest = AuthRequest("testAdmin", "password")
        return (userService.register(registerRequest).body as LoginResponse)
    }

    private fun addUser(shopId : Long = 1) {
        val addUserRequest = AddUserRequest("testUser", "password", 1)
        userService.addUser(addUserRequest)
    }

    private fun loginAddedUser(): LoginResponse {
        val authRequest = AuthRequest("testUser", "password")
        return userService.login(authRequest).body as LoginResponse
    }

    private fun addShop(adminId: Long): Shop {
        val addShopRequest = AddShopRequest( "Square", "ELHadaba AL-Wosta", "Cairo")
        return (shopService.addShop(addShopRequest)).body as Shop
    }

}


