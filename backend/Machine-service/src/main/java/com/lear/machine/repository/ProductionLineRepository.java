package com.lear.machine.repository;

import com.lear.machine.model.entity.ProductionLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductionLineRepository extends JpaRepository<ProductionLineEntity, Integer> {
    Optional<ProductionLineEntity> findByName(String name);
}
