package com.atwa.remote_ps.device

import com.atwa.remote_ps.user.payload.MessageResponse
import com.atwa.remote_ps.device.payload.AddDeviceRequest
import com.atwa.remote_ps.device.payload.UpdateDeviceRequest
import com.atwa.remote_ps.util.config.ApiConfig
import com.atwa.remote_ps.util.error.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping(DeviceController.PATH)
class DeviceController {

    @Autowired
    private lateinit var deviceService: DeviceService

    @GetMapping("/get_devices")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun getDevices(): List<Device> =
        deviceService.getDevices()

    @PostMapping("/add_device")
    @PreAuthorize("hasRole('ADMIN')")
    fun addDevice(@Valid @RequestBody addDeviceRequest: AddDeviceRequest): ResponseEntity<Any> = try {
        deviceService.addDevice(addDeviceRequest)
    } catch (e: BadRequestException) {
        ResponseEntity
            .badRequest()
            .body(MessageResponse(e.message))
    }

    @GetMapping("/get_device/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun getDeviceById(@PathVariable(value = "id") deviceId: Long): ResponseEntity<Any> = try {
        deviceService.getDeviceById(deviceId)
    } catch (e: BadRequestException) {
        ResponseEntity
            .badRequest()
            .body(MessageResponse(e.message))
    }

    @PutMapping("/update_device/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateDeviceById(
        @PathVariable(value = "id") deviceId: Long,
        @Valid @RequestBody updateDeviceRequest: UpdateDeviceRequest
    ): ResponseEntity<Any> = try {
        deviceService.updateDeviceById(deviceId, updateDeviceRequest)
    } catch (e: BadRequestException) {
        ResponseEntity
            .badRequest()
            .body(MessageResponse(e.message))
    }

    @DeleteMapping("/delete_device/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteDevice(@PathVariable(value = "id") deviceId: Long): ResponseEntity<Any> = try {
        deviceService.deleteDevice(deviceId)
    } catch (e: BadRequestException) {
        ResponseEntity
            .badRequest()
            .body(MessageResponse(e.message))
    }

    companion object {
        const val PATH = ApiConfig.BASE_API_PATH + "device"
    }
}