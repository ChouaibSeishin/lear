package org.lear.cycletimeservice.Repositories;

import org.lear.cycletimeservice.entities.CycleTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CycleTimeRepository extends JpaRepository<CycleTime, Long> {}

