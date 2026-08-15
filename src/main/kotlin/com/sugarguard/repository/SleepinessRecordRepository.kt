package com.sugarguard.repository

import com.sugarguard.entity.SleepinessRecord
import org.springframework.data.jpa.repository.JpaRepository

interface SleepinessRecordRepository : JpaRepository<SleepinessRecord, Long>