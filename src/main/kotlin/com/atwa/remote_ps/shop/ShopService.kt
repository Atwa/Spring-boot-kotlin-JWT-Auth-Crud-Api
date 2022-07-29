package com.atwa.remote_ps.shop

import com.atwa.remote_ps.user.UserRepository
import com.atwa.remote_ps.shop.payload.AddShopRequest
import com.atwa.remote_ps.shop.payload.UpdateShopRequest
import com.atwa.remote_ps.util.error.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
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
        val userDetails: UserDetails = SecurityContextHolder.getContext().authentication.principal as UserDetails
        val user = userRepository.findByUsername(userDetails.username).get()
        val shop = Shop(
            name = addShopRequest.name,
            city = addShopRequest.city,
            area = addShopRequest.area,
        ).apply { shopRepository.save(this) }
        user.copy(shop = shop).apply { userRepository.save(this) }
        return ResponseEntity.ok().body(shop)
    }

    fun getShop(): ResponseEntity<Any> {
        val userDetails: UserDetails = SecurityContextHolder.getContext().authentication.principal as UserDetails
        val shopId = userRepository.findByUsername(userDetails.username).get().shop?.id
            ?: throw BadRequestException("Error: User doesn't have shop.")
        val shop = shopRepository.findById(shopId).orElseThrow { BadRequestException("Error: Shop not found.") }
        return ResponseEntity.ok().body(shop)
    }

    fun updateShop(newShop: UpdateShopRequest): ResponseEntity<Any> {
        val userDetails: UserDetails = SecurityContextHolder.getContext().authentication.principal as UserDetails
        val shopId = userRepository.findByUsername(userDetails.username).get().shop?.id
            ?: throw BadRequestException("Error: User doesn't have shop.")
        val shop = shopRepository.findById(shopId).orElseThrow { BadRequestException("Error: Shop not found.") }
        val updatedShop = shop.copy(
            name = newShop.name,
            city = newShop.city,
            area = newShop.area,
            updatedAt = LocalDateTime.now()
        )
        return ResponseEntity.ok().body(shopRepository.save(updatedShop))
    }

    fun deleteShop(): ResponseEntity<Any> {
        val userDetails: UserDetails = SecurityContextHolder.getContext().authentication.principal as UserDetails
        val shopId = userRepository.findByUsername(userDetails.username).get().shop?.id
            ?: throw BadRequestException("Error: User doesn't have shop.")
        val shop = shopRepository.findById(shopId).orElseThrow { BadRequestException("Error: Shop not found.") }
        shopRepository.delete(shop)
        return ResponseEntity<Any>(HttpStatus.ACCEPTED)
    }
}
