package com.atwa.psManager.shop

import com.atwa.psManager.auth.UserRepository
import com.atwa.psManager.shop.payload.AddShopRequest
import com.atwa.psManager.shop.payload.EditShopRequest
import com.atwa.psManager.util.error.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ShopService {

    @Autowired
    lateinit var shopRepository: ShopRepository

    @Autowired
    lateinit var userRepository: UserRepository

    fun getShops(): List<Shop> =
        shopRepository.findAll()

    fun addShop(addShopRequest: AddShopRequest): ResponseEntity<Any> {
        val user = userRepository.findById(addShopRequest.userId)
            .orElseThrow { BadRequestException("Error: User not found.") }
        val shop = Shop(
            name = addShopRequest.name,
            city = addShopRequest.city,
            area = addShopRequest.area
        )
        user.shop = shop
        return ResponseEntity.ok().body(shopRepository.save(shop))
    }

    fun getShopById(shopId: Long): ResponseEntity<Any> {
        val shop = shopRepository.findById(shopId).orElseThrow { BadRequestException("Error: Shop not found.") }
        return ResponseEntity.ok(shop)
    }

    fun updateShopById(shopId: Long, newShop: EditShopRequest): ResponseEntity<Any> {
        val shop = shopRepository.findById(shopId).orElseThrow { BadRequestException("Error: Shop not found.") }
        val updatedShop = shop.copy(
            name = newShop.name,
            city = newShop.city,
            area = newShop.area,
            updatedAt = LocalDateTime.now()
        )
        return ResponseEntity.ok().body(shopRepository.save(updatedShop))
    }

    fun deleteShop(shopId: Long): ResponseEntity<Any> {
        val shop = shopRepository.findById(shopId).orElseThrow { BadRequestException("Error: Shop not found.") }
        shopRepository.delete(shop)
        return ResponseEntity<Any>(HttpStatus.ACCEPTED)
    }
}
