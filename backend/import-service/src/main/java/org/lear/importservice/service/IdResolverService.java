package org.lear.importservice.service;

import lombok.RequiredArgsConstructor;
import org.lear.importservice.feign.CycleTimeServiceClient;
import org.lear.importservice.feign.MachineServiceClient;
import org.lear.importservice.feign.ProjectServiceClient;
import org.lear.importservice.feign.UserServiceClient;
import org.springframework.stereotype.Service;
import feign.FeignException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdResolverService {
    private final ProjectServiceClient projectClient;
    private final MachineServiceClient machineClient;
    private final CycleTimeServiceClient cycleTimeClient;
    private final UserServiceClient userClient;

    private Optional<Long> safelyGetId(String name, String entityType, java.util.function.Function<String, Long> clientCall) {
        if (name == null || name.trim().isEmpty()) {
             return Optional.empty();
        }
        try {
            Long id = clientCall.apply(name.trim());
            return Optional.ofNullable(id);
        } catch (FeignException.NotFound e) {
            // System.err.println("WARN: " + entityType + " '" + name + "' not found.");
            return Optional.empty();
        } catch (FeignException e) {
            System.err.println("ERROR: Feign client error fetching " + entityType + " ID for '" + name + "': " + e.status() + " - " + e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("ERROR: Unexpected error fetching " + entityType + " ID for '" + name + "': " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<Long> getProjectIdByName(String name) {
        return safelyGetId(name, "Project", projectClient::getProjectIdByName);
    }

    public Optional<Long> getVariantIdByName(String name) {
        return safelyGetId(name, "Variant", projectClient::getVariantIdByName);
    }

    public Optional<Long> getLineIdByName(String name) {
        return safelyGetId(name, "Production Line", machineClient::getLineIdByName);
    }

    public Optional<Long> getUserIdByEmail(String email) {
        return safelyGetId(email, "User", userClient::getUserIdByEmail);
    }

    public Optional<Long> getMachineIdByName(String name) {
        return safelyGetId(name, "Machine", machineClient::getMachineIdByName);
    }

    public Optional<Long> getStepIdByName(String name) {
        return safelyGetId(name, "Step", machineClient::getStepIdByName);
    }
}
