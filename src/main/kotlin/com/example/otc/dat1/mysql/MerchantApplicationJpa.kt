package com.example.otc.dat1.mysql

import com.example.otc.dat1.entity.MerchantApplicationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MerchantApplicationJpa : JpaRepository<MerchantApplicationEntity, String>