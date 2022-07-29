package com.atwa.remote_ps.device

import com.atwa.remote_ps.shop.Shop
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "device")
data class Device(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(unique = true)
    val name: String,
    val hourlyPrice: BigDecimal,
    var isActive: Boolean? = false,
    val createdAt: LocalDateTime? = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    var shop: Shop?=null
)