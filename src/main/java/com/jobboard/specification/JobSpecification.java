package com.jobboard.specification;

import com.jobboard.entity.Job;
import org.springframework.data.jpa.domain.Specification;

public class JobSpecification {

    public static Specification<Job> hasTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")),
                        "%" + title.trim().toLowerCase() + "%");
    }

    public static Specification<Job> hasLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("location")),
                        "%" + location.trim().toLowerCase() + "%");
    }

    public static Specification<Job> hasMinSalary(Double minSalary) {
        if (minSalary == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("salary"), minSalary);
    }



    public static Specification<Job> isOpen() {
        return (root, query, cb) ->
                cb.equal(root.get("status"), Job.Status.OPEN);
    }



}