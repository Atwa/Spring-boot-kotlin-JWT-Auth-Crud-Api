package com.atwa.remote_ps.session

import com.atwa.remote_ps.device.DeviceRepository
import com.atwa.remote_ps.util.error.BadRequestException
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

    fun startSession(deviceId: Long): ResponseEntity<Any> {
        val device = deviceRepository.findById(deviceId).orElseThrow { BadRequestException("Error: Device not found.") }
        if (device.isActive == true) throw BadRequestException("Error: Device has another session already running.")
        val updatedDevice = device.copy(isActive = true).apply { deviceRepository.save(this) }
        val session = Session(device = updatedDevice, shop = updatedDevice.shop)
        return ResponseEntity.ok().body(sessionRepository.save(session))
    }

    fun endSession(sessionId: Long): ResponseEntity<Any> {
        val session = sessionRepository.findById(sessionId).orElseThrow { BadRequestException("Error: Session not found.") }
        if (session.isOver == true) throw BadRequestException("Error: Session has already ended.")
        val updatedDevice = session.device.copy(isActive = false).apply { deviceRepository.save(this) }
        val updatedSession = session.copy(
            device = updatedDevice,
            isOver = true,
            endedAt = LocalDateTime.now()
        ).also { it.updateTotalDue() }
        return ResponseEntity.ok().body(sessionRepository.save(updatedSession))
    }

    fun getSessionById(sessionId: Long): ResponseEntity<Any> {
        val session =
            sessionRepository.findById(sessionId).orElseThrow { BadRequestException("Error: Session not found.") }
       return ResponseEntity.ok().body(session)
    }

    fun deleteSession(sessionId: Long): ResponseEntity<Any> {
        val session =
            sessionRepository.findById(sessionId).orElseThrow { BadRequestException("Error: Session not found.") }
        sessionRepository.delete(session)
        return ResponseEntity<Any>(HttpStatus.ACCEPTED)
    }


}