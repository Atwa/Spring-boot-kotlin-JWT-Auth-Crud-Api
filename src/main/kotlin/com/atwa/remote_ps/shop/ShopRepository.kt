package com.atwa.remote_ps.shop

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShopRepository : JpaRepository<Shop, Long>