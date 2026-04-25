package com.jobboard.service;


import com.jobboard.entity.Application;
import com.jobboard.entity.Job;
import com.jobboard.entity.User;
import com.jobboard.exceptions.JobNotFoundException;
import com.jobboard.repository.ApplicationRepository;
import com.jobboard.repository.JobRepository;
import com.jobboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final UserRepository userRepository;
    private final ApplicationRepository  applicationRepository;
    private final JobRepository          jobRepository;

    public Application applyToJob(UUID id, String candidateEmail){
        User candidate = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new IllegalArgumentException("candidate not found"));
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));

        if (job.getStatus() != Job.Status.OPEN) {
            throw new IllegalStateException("Job is no longer accepting applications");
        }
        // ✅ Check if already applied
        if (applicationRepository.existsByCandidateAndJob(candidate, job)) {
            throw new IllegalStateException("Already applied to this job");
        }

        Application application = new Application();
        application.setCandidate(candidate);
        application.setJob(job);
        application.setStatus(Application.Status.PENDING);
        applicationRepository.save(application);
        return application;
    }

    public List<Application> getMyApplications(String candidateEmail){
        User candidate = userRepository.findByEmail(candidateEmail)
        .orElseThrow(() -> new IllegalArgumentException("candidate not found"));
        return applicationRepository.findByCandidate(candidate);
    }

    public void deleteApplication(UUID id, String candidateEmail){
      Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new JobNotFoundException("Application not found"));

      if (!application.getCandidate().getEmail().equals(candidateEmail)) {
          throw new IllegalArgumentException("Not your application");
      }
      if (application.getStatus() != Application.Status.PENDING) {
          throw new IllegalStateException("Cannot withdraw a reviewed application");
      }
      applicationRepository.delete(application);
    }

}

