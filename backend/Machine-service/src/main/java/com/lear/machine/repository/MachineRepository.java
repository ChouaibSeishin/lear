package com.lear.machine.repository;

import com.lear.machine.model.entity.MachineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MachineRepository extends JpaRepository<MachineEntity, Integer> {
    Optional<MachineEntity> findByName(String name);
} 
