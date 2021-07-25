package com.atwa.psManager.session

import com.atwa.psManager.device.DeviceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SessionService {

    @Autowired
    lateinit var sessionRepository: SessionRepository

    @Autowired
    lateinit var deviceRepository: DeviceRepository


    fun getSessions(): List<Session> =
        sessionRepository.findAll()

    fun addSession(deviceId: Long): ResponseEntity<Session> =
        deviceRepository.findById(deviceId).map { device ->
            device.isRunning = true
            val session = Session(device = device, shop = device.shop)
            ResponseEntity.ok().body(sessionRepository.save(session))
        }.orElseThrow { RuntimeException("Error: Device not found.") }

    fun getSessionById(sessionId: Long): ResponseEntity<Session> =
        sessionRepository.findById(sessionId).map { session ->
            ResponseEntity.ok(session)
        }.orElseThrow { RuntimeException("Error: Session not found.") }

    fun endSession(sessionId: Long): ResponseEntity<Session> =
        sessionRepository.findById(sessionId).map { currentSession ->
            currentSession.device.isRunning = false
            val updatedSession = currentSession.copy(
                isOver = true,
                endedAt = LocalDateTime.now()).also { it.updateTotalDue() }
            ResponseEntity.ok().body(sessionRepository.save(updatedSession))
        }.orElseThrow { RuntimeException("Error: Session not found.") }

    fun deleteSession(sessionId: Long): ResponseEntity<Void> =
        sessionRepository.findById(sessionId).map { session ->
            sessionRepository.delete(session)
            ResponseEntity<Void>(HttpStatus.ACCEPTED)
        }.orElseThrow { RuntimeException("Error: Session not found.") }


}