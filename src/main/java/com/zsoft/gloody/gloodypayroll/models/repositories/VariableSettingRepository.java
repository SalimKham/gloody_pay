package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.VariableSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface VariableSettingRepository extends JpaRepository<VariableSetting, Long> {
    boolean existsByKeyAndCompanyAndIdNot(String key, Long company, Long id);
    boolean existsBySourceAndCompanyAndIdNot(String source, Long company, Long id);
    void deleteBySource(String key);
    void deleteAllBySourceIsNotNullAndCompany(Long company);
    Page<VariableSetting> findAllByLabelLikeOrKeyLike(String label, String key, Pageable pageable);
}