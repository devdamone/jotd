package com.thedamones.fusionauth.jotd.jokes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface JokeRepository extends JpaRepository<Joke, UUID> {

    Optional<Joke> findByDate(LocalDate date);

    Page<Joke> findAllByDateGreaterThanEqual(LocalDate date, Pageable pageable);
}
