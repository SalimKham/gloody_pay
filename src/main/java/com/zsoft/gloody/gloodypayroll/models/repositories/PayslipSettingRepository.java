package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PayslipSettingRepository extends JpaRepository<PayslipSetting, Long> {
     @Nonnull
    Optional<PayslipSetting> findTopByOrderByIdAsc();
     Page<PayslipSetting> findAllByCompanyAndTemplate(Long company, boolean template, Pageable pageable);
}