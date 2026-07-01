package com.repository;

import com.model.Bio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BioRepository extends JpaRepository<Bio, Long> {
    Optional<Bio> findByUserId(Long userId);
}
