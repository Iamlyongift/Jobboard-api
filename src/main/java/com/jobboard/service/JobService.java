package com.jobboard.service;

import com.jobboard.dto.JobRequest;
import com.jobboard.entity.Job;
import com.jobboard.entity.User;
import com.jobboard.exceptions.JobNotFoundException;
import com.jobboard.exceptions.UserNotFoundException;
import com.jobboard.repository.JobRepository;
import com.jobboard.repository.UserRepository;
import com.jobboard.specification.JobSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public Job createJob(JobRequest request, String email) {
        User employer = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setEmployer(employer);
        job.setStatus(Job.Status.OPEN);
        return jobRepository.save(job);
    }


    public Job updateJob(UUID id, JobRequest request, String email) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));

        if (!job.getEmployer().getEmail().equals(email)) {
            throw new IllegalArgumentException("Not your job!");
        }
        job.setTitle(request.getTitle());
        job.setEmail(request.getEmail());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        return jobRepository.save(job);
    }

    public void deleteJob(UUID id, String email) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));

        if (!job.getEmployer().getEmail().equals(email)) {
            throw new IllegalArgumentException("Not your job!");
        }
        jobRepository.deleteById(id);
    }

    public Job getJobById(UUID id){
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("job not found"));
        return job;
    }

    public List<Job> getAllJobs(String title, String location, Double minSalary){
        Specification<Job> spec = Specification.allOf(
                JobSpecification.isOpen(),
                JobSpecification.hasTitle(title),
                JobSpecification.hasLocation(location),
                JobSpecification.hasMinSalary(minSalary)
        );
        return jobRepository.findAll(spec);
    }
}

//