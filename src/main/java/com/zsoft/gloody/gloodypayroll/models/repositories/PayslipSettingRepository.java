package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSetting;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipSettingRepository extends JpaRepository<PayslipSetting, Long> {

    @EntityGraph(value = "PayslipSetting.elements")
    @Query("SELECT ps FROM PayslipSetting ps where not ps.closed  and (ps.byDefault OR ( ps.template = false AND ps.paymentDate is null) OR  (MONTH(ps.paymentDate) = MONTH(:current_month ) AND YEAR(ps.paymentDate) = YEAR (:current_month ))) ")
    List<PayslipSetting> findAllByMonth( @Param("current_month") LocalDate currentMonth);

    @Override
    @EntityGraph(value = "PayslipSetting.elements")
    Optional<PayslipSetting> findById(Long aLong);

    @Nonnull
    @EntityGraph(value = "PayslipSetting.elements")
    Optional<PayslipSetting> findTopByByDefaultOrderByIdAsc(boolean template);
     Page<PayslipSetting> findAllByCompanyAndTemplate(Long company, boolean template, Pageable pageable);
     @Modifying
     @Query("UPDATE PayslipSetting  p SET p.byDefault = CASE WHEN p.id =:template_id THEN true ELSE false END")
     void markAsDefault(@Param("template_id") Long templateId);
    Page<PayslipSetting> findAllByNameLikeAndTemplateIsTrue(String query, Pageable pageable);
    @EntityGraph(value = "PayslipSetting.elements")
    @Query("SELECT ps FROM PayslipSetting ps where ps.employee = :employee_id AND MONTH(ps.paymentDate) = MONTH(:current_month ) AND YEAR(ps.paymentDate) = YEAR (:current_month ) ")
    Optional<PayslipSetting> findByEmployeeAndCurrentMonth(@Param("employee_id") Long employeeId, @Param("current_month") LocalDate currentMonth);
}