package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.VariableSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VariableSettingRepository extends JpaRepository<VariableSetting, Long> {
    Optional<VariableSetting> findByKey(String key);

    boolean existsByKey(String key);
    void deleteBySource(String key);
}