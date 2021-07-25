package com.atwa.psManager.device

import com.atwa.psManager.auth.payload.MessageResponse
import com.atwa.psManager.device.payload.AddDeviceRequest
import com.atwa.psManager.device.payload.UpdateDeviceRequest
import com.atwa.psManager.util.config.ApiConfig
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

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun getDevices(): List<Device> =
        deviceService.getDevices()

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun addDevice(@Valid @RequestBody addDeviceRequest: AddDeviceRequest): ResponseEntity<MessageResponse> =
        deviceService.addDevice(addDeviceRequest)

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    fun getDeviceById(@PathVariable(value="id") deviceId: Long): ResponseEntity<Device> =
        deviceService.getDeviceById(deviceId)

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateDeviceById(
        @PathVariable(value = "id") deviceId: Long,
        @Valid @RequestBody updateDeviceRequest: UpdateDeviceRequest
    ): ResponseEntity<Device> =
        deviceService.updateDeviceById(deviceId, updateDeviceRequest)

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteDevice(@PathVariable(value = "id") deviceId: Long): ResponseEntity<Void> =
        deviceService.deleteDevice(deviceId)

    companion object {
        const val PATH = ApiConfig.BASE_API_PATH + "device"
    }
}