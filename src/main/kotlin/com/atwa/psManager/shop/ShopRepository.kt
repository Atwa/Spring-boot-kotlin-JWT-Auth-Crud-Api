package com.atwa.psManager.shop

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

@Repository
interface ShopRepository : JpaRepository<Shop, Long>