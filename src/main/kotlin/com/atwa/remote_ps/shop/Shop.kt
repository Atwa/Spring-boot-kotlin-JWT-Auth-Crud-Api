package com.atwa.remote_ps.shop

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "shop")
data class Shop(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(unique = true)
    val name: String,
    val area: String,
    val city: String,
    val createdAt: LocalDateTime? = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = LocalDateTime.now(),

)