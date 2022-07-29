package com.atwa.remote_ps.user

import com.atwa.remote_ps.user.payload.AddUserRequest
import com.atwa.remote_ps.user.payload.AuthRequest
import com.atwa.remote_ps.user.payload.ChangePasswordRequest
import com.atwa.remote_ps.user.payload.MessageResponse
import com.atwa.remote_ps.util.config.ApiConfig
import com.atwa.remote_ps.util.error.BadRequestException
import com.atwa.remote_ps.util.error.ValidationErrorProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import javax.validation.Valid


@RestController
@RequestMapping(UserController.PATH)
class UserController {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var validationErrorProcessor: ValidationErrorProcessor


    @PostMapping("/login")
    fun login(@Valid @RequestBody authRequest: AuthRequest): ResponseEntity<Any> {
        return try {
            userService.login(authRequest)
        } catch (e: Exception) {
            val status = when (e) {
                is UsernameNotFoundException -> HttpStatus.BAD_REQUEST
                is BadCredentialsException -> HttpStatus.UNAUTHORIZED
                is BadRequestException -> HttpStatus.FORBIDDEN
                else -> HttpStatus.INTERNAL_SERVER_ERROR
            }
            ResponseEntity<Any>(MessageResponse(e.message), status)
        }
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody authRequest: AuthRequest, result: BindingResult): ResponseEntity<Any> {
        if (result.hasFieldErrors()) {
            val errorMessage = validationErrorProcessor.process(result)
            return ResponseEntity.badRequest().body(MessageResponse(errorMessage))
        }
        return try {
            userService.register(authRequest)
        } catch (e: BadRequestException) {
            return ResponseEntity.badRequest().body(MessageResponse(e.message))
        }
    }

    @PostMapping("/add_user")
    @PreAuthorize("hasRole('ADMIN')")
    fun addUser(@Valid @RequestBody addUserRequest: AddUserRequest): Any =
        userService.addUser(addUserRequest)

    @PostMapping("/add_admin")
    @PreAuthorize("hasRole('ADMIN')")
    fun addAdmin(@Valid @RequestBody addUserRequest: AddUserRequest): Any =
        userService.addAdmin(addUserRequest)

    @PutMapping("/suspend_user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun suspendUser(@PathVariable id: Long): ResponseEntity<Any> = try {
        userService.suspendUser(id)
    } catch (e: BadRequestException) {
        ResponseEntity
            .badRequest()
            .body(MessageResponse(e.message))
    }

    @PostMapping("/change_password")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun changePassword(
        @Valid @RequestBody changePasswordRequest: ChangePasswordRequest,
    ): ResponseEntity<Any> = try {
        userService.changePassword(changePasswordRequest)
    } catch (e: BadRequestException) {
        ResponseEntity<Any>(MessageResponse(e.message), HttpStatus.UNAUTHORIZED)
    }

    companion object {
        const val PATH = ApiConfig.BASE_API_PATH + "auth"
    }
}