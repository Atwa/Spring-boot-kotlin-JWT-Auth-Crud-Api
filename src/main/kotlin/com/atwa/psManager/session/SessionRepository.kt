package com.atwa.psManager.session

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

@Repository
interface SessionRepository : JpaRepository<Session, Long>