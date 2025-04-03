package com.thedamones.fusionauth.jotd.jokes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDate;
import java.util.UUID;

@Relation(collectionRelation = "jokes")
public record JokeRecord(UUID id, @NotNull LocalDate date, @NotBlank String joke, String description) {
}
