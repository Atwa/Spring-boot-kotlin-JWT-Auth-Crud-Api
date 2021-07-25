package com.atwa.psManager.device

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

@Repository
interface DeviceRepository : JpaRepository<Device, Long>