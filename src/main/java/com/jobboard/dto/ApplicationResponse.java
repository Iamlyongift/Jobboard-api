package com.jobboard.dto;

import com.jobboard.entity.Application;
import com.jobboard.entity.Job;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ApplicationResponse {
    private UUID id;
    private String jobTitle;
    private String jobLocation;
    private String status;
    private LocalDateTime appliedAt;


    public ApplicationResponse(Application application) {
        this.id = application.getId();
        this.jobTitle = application.getJob().getTitle();
        this.jobLocation = application.getJob().getLocation();
        this.status = application.getStatus().name();
        this.appliedAt = application.getAppliedAt();
    }
}

