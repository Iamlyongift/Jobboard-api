package com.jobboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Entity
@Table(name="users")
public class User{


    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID id;
    @Column(unique = true, nullable = true)
    private String email;
    @Column(nullable = true)
    private String password;
    @Column
    @Enumerated(EnumType.STRING)
    private  Role role;
    @Column
    @Enumerated(EnumType.STRING)
    private Provider provider;
    @Column(nullable = true)
    private String providerId;
    @Column(nullable = false)
    private boolean profileComplete = false;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

//ENUMS
    public enum Role{
        EMPLOYER, CANDIDATE
    }

    public enum Provider{
        LOCAL, GOOGLE, GITHUB
    }
}
