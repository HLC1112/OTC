package com.example.otc.dat1.mysql

import com.example.otc.dat1.entity.OutboxEvtEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OutboxEvtJpa : JpaRepository<OutboxEvtEntity, String> {
    fun findTop50BySentAtIsNullOrderByCreatedAtAsc(): List<OutboxEvtEntity>
}