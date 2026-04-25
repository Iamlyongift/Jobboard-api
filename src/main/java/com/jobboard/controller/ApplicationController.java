package com.jobboard.controller;


import com.jobboard.dto.ApplicationResponse;
import com.jobboard.entity.Application;
import com.jobboard.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    // ✅ APPLY
    @PostMapping("/{jobId}/apply")
    public ResponseEntity<ApplicationResponse> applyToJob(@PathVariable UUID jobId) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Application application = applicationService.applyToJob(jobId, email);
        return ResponseEntity.ok(new ApplicationResponse(application));
    }

    // ✅ GET MY APPLICATIONS
    @GetMapping("/mine")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        List<Application> applications = applicationService.getMyApplications(email);

        List<ApplicationResponse> response = applications.stream()
                .map(ApplicationResponse::new)
                .toList();

        return ResponseEntity.ok(response);
    }


    // ✅ DELETE APPLICATION
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable UUID id) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        applicationService.deleteApplication(id, email);
        return ResponseEntity.noContent().build();
    }
}

