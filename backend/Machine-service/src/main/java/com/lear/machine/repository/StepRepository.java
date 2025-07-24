package com.lear.machine.repository;

import com.lear.machine.model.entity.ProductionLineEntity;
import com.lear.machine.model.entity.StepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StepRepository extends JpaRepository<StepEntity, Integer> {
    List<StepEntity> findByMachineId(Integer machineId);
    Optional<StepEntity> findByName(String name);

} 
