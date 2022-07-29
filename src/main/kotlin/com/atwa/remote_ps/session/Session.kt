package com.atwa.remote_ps.session

import com.atwa.remote_ps.device.Device
import com.atwa.remote_ps.shop.Shop
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.persistence.*


@Entity
@Table(name = "session")
data class Session(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var totalDue: Int? = 0,
    val isOver: Boolean? = false,
    val createdAt: LocalDateTime? = LocalDateTime.now(),
    val endedAt: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    var shop: Shop? = null,

    @OneToOne
    @JoinColumn(name = "device_id", nullable = false)
    val device: Device,

    ) {
    fun updateTotalDue() {
        val minutePrice = device.hourlyPrice / 60.toBigDecimal()
        val minutesNo = ChronoUnit.MINUTES.between(createdAt, endedAt).toBigDecimal()
        totalDue = (minutesNo * minutePrice).toInt()
    }

}