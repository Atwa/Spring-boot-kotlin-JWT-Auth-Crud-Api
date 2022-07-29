package com.atwa.remote_ps.session

import com.atwa.remote_ps.user.payload.MessageResponse
import com.atwa.remote_ps.util.config.ApiConfig
import com.atwa.remote_ps.util.error.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*


@RestController
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@RequestMapping(SessionController.PATH)
class SessionController {

    @Autowired
    private lateinit var sessionService: SessionService

    @GetMapping
    fun getSessions(): List<Session> =
        sessionService.getSessions()

    @PostMapping("/start_session/{id}")
    fun startSession(@PathVariable(value = "id") deviceId: Long): ResponseEntity<Any> = try {
        sessionService.startSession(deviceId)
    } catch (e: BadRequestException) {
        ResponseEntity
            .badRequest()
            .body(MessageResponse(e.message))
    }

    @PutMapping("/end_session/{id}")
    fun endSession(@PathVariable(value = "id") sessionId: Long): ResponseEntity<Any> = try {
        sessionService.endSession(sessionId)
    } catch (e: BadRequestException) {
        ResponseEntity
            .badRequest()
            .body(MessageResponse(e.message))
    }

    @GetMapping("/get_session/{id}")
    fun getSessionById(@PathVariable(value="id") sessionId: Long): ResponseEntity<Any> = try {
        sessionService.getSessionById(sessionId)
    } catch (e: BadRequestException) {
        ResponseEntity
            .badRequest()
            .body(MessageResponse(e.message))
    }

    @DeleteMapping("/delete_session/{id}")
    fun deleteSession(@PathVariable(value = "id") sessionId: Long): ResponseEntity<Any> = try {
        sessionService.deleteSession(sessionId)
    } catch (e: BadRequestException) {
        ResponseEntity
            .badRequest()
            .body(MessageResponse(e.message))
    }
    companion object {
        const val PATH = ApiConfig.BASE_API_PATH + "session"
    }
}