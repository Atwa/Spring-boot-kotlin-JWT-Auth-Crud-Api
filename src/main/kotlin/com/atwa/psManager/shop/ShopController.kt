package com.atwa.psManager.shop

import com.atwa.psManager.auth.payload.MessageResponse
import com.atwa.psManager.util.config.ApiConfig
import com.atwa.psManager.shop.payload.AddShopRequest
import com.atwa.psManager.shop.payload.EditShopRequest
import com.atwa.psManager.util.error.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping(ShopController.PATH)
class ShopController {

    @Autowired
    private lateinit var shopService: ShopService

    @PostMapping("/add_Shop")
    fun addShop(@Valid @RequestBody addShopRequest: AddShopRequest): ResponseEntity<Any> = try {
        shopService.addShop(addShopRequest)
    } catch (e: BadRequestException) {
        ResponseEntity
            .badRequest()
            .body(MessageResponse(e.message))
    }

    @GetMapping("/get_shop/{id}")
    fun getShopById(@PathVariable(value = "id") shopId: Long): ResponseEntity<Any> = try {
        shopService.getShopById(shopId)
    } catch (e: BadRequestException) {
        ResponseEntity
            .badRequest()
            .body(MessageResponse(e.message))
    }

    @PutMapping("/update_shop/{id}")
    fun updateShopById(
        @PathVariable(value = "id") shopId: Long,
        @Valid @RequestBody newShop: EditShopRequest,
    ): ResponseEntity<Any> = try {
        shopService.updateShopById(shopId, newShop)
    } catch (e: BadRequestException) {
        ResponseEntity
            .badRequest()
            .body(MessageResponse(e.message))
    }

    @DeleteMapping("/delete_shop/{id}")
    fun deleteShop(@PathVariable(value = "id") shopId: Long): ResponseEntity<Any> = try {
        shopService.deleteShop(shopId)
    } catch (e: BadRequestException) {
        ResponseEntity
            .badRequest()
            .body(MessageResponse(e.message))
    }

    companion object {
        const val PATH = ApiConfig.BASE_API_PATH + "shop"
    }
}