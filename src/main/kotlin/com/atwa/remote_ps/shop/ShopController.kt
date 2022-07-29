package com.atwa.remote_ps.shop

import com.atwa.remote_ps.user.payload.MessageResponse
import com.atwa.remote_ps.util.config.ApiConfig
import com.atwa.remote_ps.shop.payload.AddShopRequest
import com.atwa.remote_ps.shop.payload.UpdateShopRequest
import com.atwa.remote_ps.util.error.BadRequestException
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

    @GetMapping("/get_shop")
    fun getShop(): ResponseEntity<Any> = try {
        shopService.getShop()
    } catch (e: BadRequestException) {
        ResponseEntity
            .badRequest()
            .body(MessageResponse(e.message))
    }

    @PutMapping("/update_shop")
    fun updateShop(
        @Valid @RequestBody newShop: UpdateShopRequest,
    ): ResponseEntity<Any> = try {
        shopService.updateShop(newShop)
    } catch (e: BadRequestException) {
        ResponseEntity
            .badRequest()
            .body(MessageResponse(e.message))
    }

    @DeleteMapping("/delete_shop")
    fun deleteShop(): ResponseEntity<Any> = try {
        shopService.deleteShop()
    } catch (e: BadRequestException) {
        ResponseEntity
            .badRequest()
            .body(MessageResponse(e.message))
    }

    companion object {
        const val PATH = ApiConfig.BASE_API_PATH + "shop"
    }
}