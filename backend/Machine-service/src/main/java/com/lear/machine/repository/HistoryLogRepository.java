package com.lear.machine.repository;

import com.lear.machine.model.entity.HistoryLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryLogRepository extends JpaRepository<HistoryLogEntity, Integer> {
    List<HistoryLogEntity> findByMachineId(Integer machineId);
} 