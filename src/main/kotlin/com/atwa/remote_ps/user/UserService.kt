package com.atwa.remote_ps.user

import com.atwa.remote_ps.user.model.Role
import com.atwa.remote_ps.user.model.User
import com.atwa.remote_ps.user.payload.*
import com.atwa.remote_ps.user.payload.MessageResponse
import com.atwa.remote_ps.util.error.BadRequestException
import com.atwa.remote_ps.util.jwt.JwtUtils
import com.atwa.remote_ps.shop.ShopRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.stream.Collectors


@Service
class UserService : UserDetailsService {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var shopRepository: ShopRepository

    @Autowired
    lateinit var passwordEncoder: BCryptPasswordEncoder

    @Autowired
    lateinit var authenticationManager: AuthenticationManager

    @Autowired
    lateinit var jwtUtils: JwtUtils

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String?): UserDetails {
        val user =
            userRepository.findByUsername(username).orElseThrow { UsernameNotFoundException("Not found: $username") }
        return UserDetailImpl(user)
    }

    fun login(authRequest: AuthRequest): ResponseEntity<Any> {
        val userDetail = loadUserByUsername(authRequest.username)

        if (!passwordEncoder.matches(authRequest.password, userDetail.password)) {
            throw BadCredentialsException("Invalid credentials")
        }

        if (!userDetail.isEnabled) {
            throw BadRequestException("The user is not enabled")
        }


        val authentication =
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    authRequest.username,
                    authRequest.password
                )
            )
        SecurityContextHolder.getContext().authentication = authentication

        val jwtToken = jwtUtils.generateJwtToken(authentication)

        val userDetails = authentication.principal as UserDetailImpl
        val user = userDetails.user
        val roles = userDetails.authorities.stream()
            .map { item -> item.authority }
            .collect(Collectors.toList())


        user.token = jwtToken
        userRepository.save(user)

        return ResponseEntity.ok().body(
            LoginResponse(
                jwtToken,
                userDetails.getId() ?: 0,
                userDetails.username,
                userDetails.getShop()?.id,
                user.isAdmin()
            )
        )
    }


    fun register(authRequest: AuthRequest): ResponseEntity<Any> {
        if (userRepository.existsByUsername(authRequest.username)) {
            throw BadRequestException("Username is already taken")
        }
        User(
            username = authRequest.username,
            password = passwordEncoder.encode(authRequest.password),
            roles = hashSetOf(Role.ROLE_USER, Role.ROLE_ADMIN)
        ).apply { userRepository.save(this) }
        return login(authRequest)
    }

    fun addUser(addUserRequest: AddUserRequest): ResponseEntity<Any> {
        if (userRepository.existsByUsername(addUserRequest.username)) {
            return ResponseEntity
                .badRequest()
                .body(MessageResponse("Username is already taken"))
        }
        val user = User(
            username = addUserRequest.username,
            password = passwordEncoder.encode(addUserRequest.password),
            shop = shopRepository.findById(addUserRequest.shopId).orElse(null),
            roles = hashSetOf(Role.ROLE_USER)
        )
        return ResponseEntity.ok().body(userRepository.save(user))
    }

    fun addAdmin(addUserRequest: AddUserRequest): ResponseEntity<Any> {
        if (userRepository.existsByUsername(addUserRequest.username)) {
            return ResponseEntity
                .badRequest()
                .body(MessageResponse("Username is already taken"))
        }
        val user = User(
            username = addUserRequest.username,
            password = passwordEncoder.encode(addUserRequest.password),
            shop = shopRepository.findById(addUserRequest.shopId).orElse(null),
            roles = hashSetOf(Role.ROLE_USER, Role.ROLE_ADMIN)
        )
        return ResponseEntity.ok().body(userRepository.save(user))
    }

    fun suspendUser(id: Long): ResponseEntity<Any> {
        val user = userRepository.findById(id).orElseThrow { BadRequestException("Error: User not found.") }
        val admin = SecurityContextHolder.getContext().authentication.principal as UserDetails
        val adminShopId = userRepository.findByUsername(admin.username).get().shop?.id
        val userShopId = user.shop?.id
        if (user.isAdmin()) throw BadRequestException("Error: Admin can't suspend admins.")
        if (adminShopId == null || adminShopId != userShopId) throw BadRequestException("Error: Admin can't suspend users of different shops.")
        val updatedUser = user.copy(
            username = user.username,
            token = user.token,
            enabled = false,
            id = user.id,
            createdAt = user.createdAt,
            updatedAt = LocalDateTime.now(),
            roles = user.roles,
            password = user.password,
        )
        return ResponseEntity.ok().body(userRepository.save(updatedUser))
    }


    fun changePassword(changePasswordRequest: ChangePasswordRequest): ResponseEntity<Any> {
        val userDetails: UserDetails = SecurityContextHolder.getContext().authentication.principal as UserDetails
        val user = userRepository.findByUsername(userDetails.username)
            .orElseThrow { BadRequestException("Error: User not found.") }

        return changeUserPassword(user, changePasswordRequest)
    }

    fun changeUserPassword(currentUser: User, changePasswordRequest: ChangePasswordRequest): ResponseEntity<Any> {
        if (!passwordEncoder.matches(changePasswordRequest.oldPassword, currentUser.password))
            return ResponseEntity<Any>(MessageResponse("Invalid credentials"), HttpStatus.UNAUTHORIZED)

        val updatedUser: User =
            currentUser
                .copy(
                    username = currentUser.username,
                    token = currentUser.token,
                    enabled = currentUser.enabled,
                    id = currentUser.id,
                    createdAt = currentUser.createdAt,
                    updatedAt = LocalDateTime.now(),
                    roles = currentUser.roles,
                    password = passwordEncoder.encode(changePasswordRequest.newPassword)
                )

        return ResponseEntity.ok().body(userRepository.save(updatedUser))
    }


}