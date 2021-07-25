package com.atwa.psManager.session

import com.atwa.psManager.util.config.ApiConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid


@RestController
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@RequestMapping(SessionController.PATH)
class SessionController {

    @Autowired
    private lateinit var sessionService: SessionService

    @GetMapping
    fun getSessions(): List<Session> =
        sessionService.getSessions()

    @PostMapping
    fun startSession(@Valid @RequestBody deviceId:Long): ResponseEntity<Session> =
        sessionService.addSession(deviceId)

    @GetMapping("/{id}")
    fun getSessionById(@PathVariable(value="id") sessionId: Long): ResponseEntity<Session> =
        sessionService.getSessionById(sessionId)

    @PutMapping("/{id}")
    fun endSession(
        @PathVariable(value = "id") sessionId: Long,
        @Valid @RequestBody newSession: Session): ResponseEntity<Session> =
        sessionService.endSession(sessionId)

    @DeleteMapping("/{id}")
    fun deleteSession(@PathVariable(value = "id") sessionId: Long): ResponseEntity<Void> =
        sessionService.deleteSession(sessionId)

    companion object {
        const val PATH = ApiConfig.BASE_API_PATH + "session"
    }
}