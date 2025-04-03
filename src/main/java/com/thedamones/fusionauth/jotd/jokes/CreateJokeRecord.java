package com.thedamones.fusionauth.jotd.jokes;

import com.opencsv.bean.CsvDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateJokeRecord(@NotNull @CsvDate("yyyy-MM-dd") LocalDate date, @NotBlank String joke, String description) {
}
