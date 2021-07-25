package com.atwa.psManager.shop

import com.atwa.psManager.auth.UserRepository
import com.atwa.psManager.shop.payload.AddShopRequest
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

    fun addShop(addShopRequest: AddShopRequest): ResponseEntity<Shop> =
        userRepository.findById(addShopRequest.userId).map { user ->
            val shop = Shop(
                name = addShopRequest.name,
                city = addShopRequest.city,
                area = addShopRequest.area
            )
            user.shop = shop
            ResponseEntity.ok().body(shopRepository.save(shop))
        }.orElseThrow { RuntimeException("Error: Session not found.") }

    fun getShopById(shopId: Long): ResponseEntity<Shop> =
        shopRepository.findById(shopId).map { shop ->
            ResponseEntity.ok(shop)
        }.orElseThrow { RuntimeException("Error: Shop not found.") }

    fun updateShopById(shopId: Long, newShop: Shop): ResponseEntity<Shop> =
        shopRepository.findById(shopId).map { currentShop ->
            val updatedShop = currentShop.copy(
                name = newShop.name,
                city = newShop.city,
                area = newShop.area,
                createdAt = newShop.createdAt,
                updatedAt = LocalDateTime.now(),
                users = newShop.users
            )
            ResponseEntity.ok().body(shopRepository.save(updatedShop))
        }.orElseThrow { RuntimeException("Error: Shop not found.") }

    fun deleteShop(shopId: Long): ResponseEntity<Void> =
        shopRepository.findById(shopId).map { shop ->
            shopRepository.delete(shop)
            ResponseEntity<Void>(HttpStatus.ACCEPTED)
        }.orElseThrow { RuntimeException("Error: Shop not found.") }
}
