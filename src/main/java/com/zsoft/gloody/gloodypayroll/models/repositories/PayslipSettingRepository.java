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
    @Query("SELECT ps FROM PayslipSetting ps WHERE  NOT ps.draft AND (ps.byDefault OR  (MONTH(ps.paymentDate) = MONTH(:current_month ) AND YEAR(ps.paymentDate) = YEAR (:current_month )) OR ps.id IN :ids) ")
    List<PayslipSetting> findAllByMonthAndIdIn( @Param("current_month") LocalDate currentMonth, @Param("ids") List<Long> ids);

    @Query("SELECT ps.employee FROM PayslipSetting ps WHERE  NOT ps.draft AND not ps.template AND ps.employee IS NOT NULL AND (ps.paymentDate is NULL OR (MONTH(ps.paymentDate) = MONTH(:current_month ) AND YEAR(ps.paymentDate) = YEAR (:current_month ))) ")
    List<Long> findAllEmployeesWithTemplates( @Param("current_month") LocalDate currentMonth);

    @Modifying
    @Query("DELETE FROM PayslipSetting ps where ps.employee in :ids and ps.draft")
    void deleteByEmployeeInAndDraft(@Param("ids") List<Long> ids);

    @EntityGraph(value = "PayslipSetting.elements")
    List<PayslipSetting> findAll( );


    @Override
    @EntityGraph(value = "PayslipSetting.elements")
    @Query("SELECT ps FROM PayslipSetting ps  WHERE ps.id = :id AND NOT ps.draft")
    Optional<PayslipSetting> findById(@Param("id") Long id);

    @Nonnull
    @EntityGraph(value = "PayslipSetting.elements")
    Optional<PayslipSetting> findTopByByDefaultOrderByIdAsc(boolean template);
     Optional<PayslipSetting> findFirstByDraftAndContract(Boolean draft, Long contractId);
     @Modifying
     @Query("UPDATE PayslipSetting  p SET p.byDefault = CASE WHEN p.id =:template_id THEN true ELSE false END")
     void markAsDefault(@Param("template_id") Long templateId);
    Page<PayslipSetting> findAllByNameLikeAndTemplateIsTrue(String query, Pageable pageable);
    @EntityGraph(value = "PayslipSetting.elements")
    @Query("SELECT ps FROM PayslipSetting ps where ps.employee = :employee_id  AND not ps.draft AND MONTH(ps.paymentDate) = MONTH(:current_month ) AND YEAR(ps.paymentDate) = YEAR (:current_month ) order by ps.id desc ")
    List<PayslipSetting> findByEmployeeAndCurrentMonth(@Param("employee_id") Long employeeId, @Param("current_month") LocalDate currentMonth);

    @Query("SELECT ps.contract FROM PayslipSetting ps WHERE ps.draft AND MONTH(ps.updatedAt) = MONTH(CURRENT_DATE) AND YEAR (ps.updatedAt) = YEAR(CURRENT_DATE ) ")
    List<Long> findDraftContractIds();
}