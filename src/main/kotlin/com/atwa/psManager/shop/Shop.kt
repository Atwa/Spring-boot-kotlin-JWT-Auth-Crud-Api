package com.atwa.psManager.shop

import com.atwa.psManager.auth.model.User
import com.atwa.psManager.device.Device
import com.atwa.psManager.session.Session
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "shop")
data class Shop(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(unique = true)
    val name: BigDecimal,
    val city: String,
    val area: String,
    val createdAt: LocalDateTime? = LocalDateTime.now(),
    val updatedAt: LocalDateTime? = LocalDateTime.now(),

    @OneToMany(mappedBy = "shop", cascade = [CascadeType.ALL], fetch = FetchType.LAZY) val users: List<User> = ArrayList(),
    @OneToMany(mappedBy = "shop", cascade = [CascadeType.ALL], fetch = FetchType.LAZY) val devices: List<Device> = ArrayList(),
    @OneToMany(mappedBy = "shop", cascade = [CascadeType.ALL], fetch = FetchType.LAZY) val sessions: List<Session> = ArrayList()
)