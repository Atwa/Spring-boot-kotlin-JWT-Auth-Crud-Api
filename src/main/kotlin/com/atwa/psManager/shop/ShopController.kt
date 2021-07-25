package com.atwa.psManager.shop

import com.atwa.psManager.util.config.ApiConfig
import com.atwa.psManager.shop.payload.AddShopRequest
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

    @PostMapping
    fun addShop(@Valid @RequestBody addShopRequest: AddShopRequest): ResponseEntity<Shop> =
        shopService.addShop(addShopRequest)

    @GetMapping("/{id}")
    fun getShopById(@PathVariable(value = "id") shopId: Long): ResponseEntity<Shop> =
        shopService.getShopById(shopId)

    @PutMapping("/{id}")
    fun updateShopById(
        @PathVariable(value = "id") shopId: Long,
        @Valid @RequestBody newShop: Shop,
    ): ResponseEntity<Shop> =
        shopService.updateShopById(shopId, newShop)

    @DeleteMapping("/{id}")
    fun deleteShop(@PathVariable(value = "id") shopId: Long): ResponseEntity<Void> =
        shopService.deleteShop(shopId)

    companion object {
        const val PATH = ApiConfig.BASE_API_PATH + "shop"
    }
}