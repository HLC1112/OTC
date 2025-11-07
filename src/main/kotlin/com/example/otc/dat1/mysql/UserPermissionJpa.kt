package com.example.otc.dat1.mysql

import com.example.otc.dat1.entity.UserPermissionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserPermissionJpa : JpaRepository<UserPermissionEntity, String>