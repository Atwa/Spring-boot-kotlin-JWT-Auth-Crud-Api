package com.atwa.remote_ps.device

import com.atwa.remote_ps.device.payload.AddDeviceRequest
import com.atwa.remote_ps.device.payload.UpdateDeviceRequest
import com.atwa.remote_ps.shop.ShopRepository
import com.atwa.remote_ps.util.error.BadRequestException
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

    fun addDevice(addDeviceRequest: AddDeviceRequest): ResponseEntity<Any> {
        val shop = shopRepository.findById(addDeviceRequest.shopId).orElseThrow { BadRequestException("Error: Shop not found.") }
        val device = Device(
            name = addDeviceRequest.name,
            hourlyPrice = addDeviceRequest.hourlyPrice,
            shop = shop
        )
        return ResponseEntity.ok().body(deviceRepository.save(device))
    }

    fun getDeviceById(deviceId: Long): ResponseEntity<Any> {
        val device = deviceRepository.findById(deviceId).orElseThrow { BadRequestException("Error: Device not found.") }
        return ResponseEntity.ok().body(device)
    }

    fun updateDeviceById(deviceId: Long, updateDeviceRequest: UpdateDeviceRequest): ResponseEntity<Any> {
        val device = deviceRepository.findById(deviceId).orElseThrow { BadRequestException("Error: Device not found.") }
        val updatedDevice: Device = device
            .copy(
                hourlyPrice = updateDeviceRequest.hourlyPrice,
                name = updateDeviceRequest.name,
                updatedAt = LocalDateTime.now(),
            )
        return ResponseEntity.ok().body(deviceRepository.save(updatedDevice))
    }

    fun deleteDevice(deviceId: Long): ResponseEntity<Any> {
        val device = deviceRepository.findById(deviceId).orElseThrow { BadRequestException("Error: Device not found.") }
        deviceRepository.delete(device)
        return ResponseEntity<Any>(HttpStatus.ACCEPTED)
    }
}