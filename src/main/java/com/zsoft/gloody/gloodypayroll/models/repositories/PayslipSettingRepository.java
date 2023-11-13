package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayslipSettingRepository extends JpaRepository<PayslipSetting, Long> {
    List<PayslipSetting> findByEmployeeId(Long employeeId);
}