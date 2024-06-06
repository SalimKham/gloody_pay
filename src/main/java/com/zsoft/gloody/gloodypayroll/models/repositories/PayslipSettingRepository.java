package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PayslipSettingRepository extends JpaRepository<PayslipSetting, Long> {
     @Nonnull
    Optional<PayslipSetting> findTopByTemplateOrderByIdAsc(boolean template);
     Page<PayslipSetting> findAllByCompanyAndTemplate(Long company, boolean template, Pageable pageable);
     @Modifying
     @Query("UPDATE PayslipSetting  p SET p.template = CASE WHEN p.id =:template_id THEN true ELSE false END")
     void markAsDefault(@Param("template_id") Long templateId);
    Page<PayslipSetting> findAllByNameLike(String query, Pageable pageable);
}