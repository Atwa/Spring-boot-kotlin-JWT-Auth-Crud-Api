package com.atwa.psManager.device

import com.atwa.psManager.auth.payload.MessageResponse
import com.atwa.psManager.device.payload.AddDeviceRequest
import com.atwa.psManager.device.payload.UpdateDeviceRequest
import com.atwa.psManager.shop.ShopRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class DeviceService() {

    @Autowired
    lateinit var deviceRepository: DeviceRepository

    @Autowired
    lateinit var shopRepository: ShopRepository

    fun getDevices(): List<Device> =
        deviceRepository.findAll()

    fun addDevice(addDeviceRequest: AddDeviceRequest): ResponseEntity<MessageResponse> {
        Device(
            name = addDeviceRequest.name,
            hourlyPrice = addDeviceRequest.hourlyPrice,
            shop = shopRepository.findById(addDeviceRequest.shopId)
                .orElseThrow { RuntimeException("Error: Shop not found.") },
        ).apply{ deviceRepository.save(this) }
        return ResponseEntity.ok(MessageResponse("Device added successfully"))
    }

    fun getDeviceById(deviceId: Long): ResponseEntity<Device> =
        deviceRepository.findById(deviceId).map { device ->
            ResponseEntity.ok(device)
        }.orElseThrow { RuntimeException("Error: Device not found.") }

    fun updateDeviceById(deviceId: Long, updateDeviceRequest: UpdateDeviceRequest): ResponseEntity<Device> =
        deviceRepository.findById(deviceId).map { currentDevice ->
            val updatedDevice: Device =
                currentDevice
                    .copy(
                        hourlyPrice = updateDeviceRequest.hourlyPrice,
                        name = updateDeviceRequest.name,
                        updatedAt = LocalDateTime.now(),
                    )
            ResponseEntity.ok().body(deviceRepository.save(updatedDevice))
        }.orElseThrow { RuntimeException("Error: Device not found.") }

    fun deleteDevice(deviceId: Long): ResponseEntity<Void> =
        deviceRepository.findById(deviceId).map { device ->
            deviceRepository.delete(device)
            ResponseEntity<Void>(HttpStatus.ACCEPTED)
        }.orElseThrow { RuntimeException("Error: Device not found.") }
}