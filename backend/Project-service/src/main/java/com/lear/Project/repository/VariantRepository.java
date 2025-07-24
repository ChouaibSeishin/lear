package com.lear.Project.repository;

import com.lear.Project.entity.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariantRepository extends JpaRepository<Variant, Long> {
    List<Variant> findByProjectId(Long projectId);
    Optional<Variant> findByName(String name);
} 
