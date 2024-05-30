package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.SectionCategory;
import jakarta.annotation.Nonnull;
import jakarta.persistence.OrderBy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipConfigRepository extends JpaRepository<SectionCategory, Long> {
     @Nonnull
     @OrderBy("code asc")
     List<SectionCategory> findByCompany(Long company);
     Optional<SectionCategory> findByCodeAndCompany(String code, Long company);
}