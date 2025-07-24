package org.lear.cycletimeservice.Services;

import lombok.RequiredArgsConstructor;
import org.lear.cycletimeservice.Repositories.CycleTimeRepository;
import org.lear.cycletimeservice.dtos.enums.RecordType;
import org.lear.cycletimeservice.feignClient.UserServiceClient;
import org.lear.cycletimeservice.dtos.CycleTimeRequest;
import org.lear.cycletimeservice.dtos.UserLogRequest;
import org.lear.cycletimeservice.entities.CycleTime;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor

public class CycleTimeService {

    private final CycleTimeRepository repository;
    private final UserServiceClient userServiceClient;

    private void validateRequest(CycleTimeRequest request) {
        RecordType recordType = request.getRecordType();
        if (recordType == null) {
            throw new IllegalArgumentException("Record type is required");
        }

        switch (recordType) {
            case OPERATION:
                if (request.getStepId() == null) {
                    throw new IllegalArgumentException("Operation ID is required for OPERATION record type");
                }
                break;

            case MACHINE:
                if (request.getMachineId() == null) {
                    throw new IllegalArgumentException("Machine ID is required for MACHINE record type");
                }
                break;
        }
    }



    public CycleTime create(CycleTimeRequest request) {
        validateRequest(request);
        CycleTime cycleTime = new CycleTime();
        BeanUtils.copyProperties(request, cycleTime);
        Duration duration = Duration.between(request.getStartTime(), request.getEndTime());
        cycleTime.setDuration(duration.toMillis());
        CycleTime saved = repository.save(cycleTime);
        log(request.getUserId(), "Created cycle time with ID " + saved.getId());
        return saved;
    }

    public CycleTime update(Long id, CycleTimeRequest request) {
        validateRequest(request);
        CycleTime cycleTime = repository.findById(id).orElseThrow();
        BeanUtils.copyProperties(request, cycleTime);
        Duration duration = Duration.between(request.getStartTime(), request.getEndTime());
        cycleTime.setDuration(duration.toMillis());
        CycleTime saved = repository.save(cycleTime);
        log(request.getUserId(), "Updated cycle time with ID " + saved.getId());
        return saved;
    }

    public void delete(Long id, Long userId) {
        repository.deleteById(id);
        log(userId, "Deleted cycle time with ID " + id);
    }

    private void log(Long userId, String action) {
        userServiceClient.logUserAction(new UserLogRequest(userId, action, LocalDateTime.now()));
    }

    public List<CycleTime> getAll() {
        return repository.findAll();
    }

    public CycleTime getById(Long id) {
        return repository.findById(id).orElseThrow();
    }
}
