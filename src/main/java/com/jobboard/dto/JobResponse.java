package com.jobboard.dto;

import com.jobboard.entity.Job;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class JobResponse {
    private UUID id;
    private String title;
    private String description;
    private String location;
    private Double salary;
    private String status;
    private String employerEmail;
    private LocalDateTime createdAt;


    public JobResponse(Job job) {
        this.id = job.getId();
        this.title = job.getTitle();
        this.description = job.getDescription();
        this.location = job.getLocation();
        this.salary = job.getSalary();
        this.status = job.getStatus().name();
        this.employerEmail = job.getEmployer().getEmail();
        this.createdAt = job.getCreatedAt();
    }
}