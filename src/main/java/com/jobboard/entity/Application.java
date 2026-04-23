package com.jobboard.entity;

import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name="application")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private User candidate;
    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;
    @Column
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column
    private String cvFilepath;
    @Column
    private LocalDateTime  appliedAt;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.appliedAt = LocalDateTime.now();
    }


    public enum Status {
        PENDING, REVIEWED, REJECTED, ACCEPTED
    }
}

