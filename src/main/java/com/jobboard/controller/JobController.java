package com.jobboard.controller;

import com.jobboard.dto.JobRequest;
import com.jobboard.dto.JobResponse;
import com.jobboard.entity.Job;
import com.jobboard.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class JobController {
    private final JobService jobService;

    @GetMapping("/jobs")
    public ResponseEntity<List<JobResponse>> getAllJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double minSalary) {
        return ResponseEntity.ok(
                jobService.getAllJobs(title, location, minSalary)
                        .stream()
                        .map(JobResponse::new)
                        .toList()
        );
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable UUID id){
        Job message = jobService.getJobById(id);
        return ResponseEntity.ok(new JobResponse(message));
    }

    @PostMapping("/jobs")
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest request) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        Job message = jobService.createJob(request, email);
        return ResponseEntity.ok(new JobResponse(message));
    }

    @PutMapping("/jobs/{id}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable UUID id,
            @Valid @RequestBody JobRequest request){
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        Job message = jobService.updateJob(id, request, email);
         return ResponseEntity.ok(new JobResponse(message));
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<String> deleteJob(@PathVariable UUID id) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        jobService.deleteJob(id, email);
        return ResponseEntity.noContent().build();
    }
}
