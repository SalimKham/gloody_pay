package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import com.zsoft.gloody.gloodypayroll.models.enums.AbsenceType;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipSettingRepository extends JpaRepository<PayslipSetting, Long> {
     @Nonnull
    Optional<PayslipSetting> findTopByOrderByIdAsc();
}