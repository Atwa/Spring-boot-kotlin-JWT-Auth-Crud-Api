package com.atwa.psManager.auth

import com.atwa.psManager.auth.model.Role
import com.atwa.psManager.auth.model.User
import com.atwa.psManager.auth.payload.*
import com.atwa.psManager.auth.payload.MessageResponse
import com.atwa.psManager.util.error.BadRequestException
import com.atwa.psManager.util.jwt.JwtUtils
import com.atwa.psManager.shop.ShopRepository
import com.fasterxml.jackson.databind.ObjectMapper
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
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken(authRequest.username,
                authRequest.password))
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
                roles
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
        User(
            username = addUserRequest.username,
            password = passwordEncoder.encode(addUserRequest.password),
            shop = shopRepository.findById(addUserRequest.shopId).orElse(null),
            roles = hashSetOf(Role.ROLE_USER)
        ).apply { userRepository.save(this) }
        return ResponseEntity.ok(MessageResponse("User added successfully"))
    }

    fun suspendUser(id: Long): ResponseEntity<Any> {
        val user = userRepository.findById(id)
            .orElseThrow { BadRequestException("Error: User not found.") }
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
        userRepository.save(updatedUser)
        return ResponseEntity.ok(MessageResponse("User suspended successfully"))
    }


    fun changeUserPassword(changePasswordRequest: ChangePasswordRequest): ResponseEntity<Any> {
        return userRepository.findById(changePasswordRequest.id).map { currentUser ->
            if (currentUser.isAdmin())
                ResponseEntity<Any>(MessageResponse("Forbidden request"), HttpStatus.FORBIDDEN)
            else changePassword(currentUser, changePasswordRequest)
        }.orElseThrow { BadRequestException("Error: User not found.") }
    }

    fun changeAdminPassword(changePasswordRequest: ChangePasswordRequest): ResponseEntity<Any> {
        return userRepository.findById(changePasswordRequest.id).map { currentUser ->
            if (!currentUser.isAdmin())
                ResponseEntity<Any>(MessageResponse("Forbidden request"), HttpStatus.FORBIDDEN)
            else changePassword(currentUser, changePasswordRequest)
        }.orElseThrow { BadRequestException("Error: User not found.") }
    }

    fun changePassword(currentUser: User, changePasswordRequest: ChangePasswordRequest): ResponseEntity<Any> {
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
        userRepository.save(updatedUser)
        return ResponseEntity<Any>(MessageResponse("password changed successfully"), HttpStatus.OK)
    }




}