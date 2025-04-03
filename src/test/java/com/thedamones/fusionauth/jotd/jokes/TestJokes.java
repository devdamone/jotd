package com.thedamones.fusionauth.jotd.jokes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestJokes {

    public static final UUID TEST_ID = UUID.randomUUID();
    public static final LocalDate TEST_DATE = LocalDate.now();
    public static final String TEST_JOKE = "TEST_JOKE";
    public static final String TEST_DESCRIPTION = "TEST_DESCRIPTION";

    public static Joke createTestJoke() {
        Joke joke = new Joke();
        joke.setId(TEST_ID);
        joke.setDate(TEST_DATE);
        joke.setJoke(TEST_JOKE);
        joke.setDescription(TEST_DESCRIPTION);
        return joke;
    }

    public static void assertTestJokeRecord(JokeRecord result) {
        assertNotNull(result);
        assertEquals(TEST_ID, result.id());
        assertEquals(TEST_DATE, result.date());
        assertEquals(TEST_JOKE, result.joke());
        assertEquals(TEST_DESCRIPTION, result.description());
    }

    public static CreateJokeRecord createTestCreateJokeRecord() {
        return new CreateJokeRecord(TEST_DATE, TEST_JOKE, TEST_DESCRIPTION);
    }

    public static JokeRecord createTestJokeRecord() {
        return new JokeRecord(TEST_ID, TEST_DATE, TEST_JOKE, TEST_DESCRIPTION);
    }

    public static Page<Joke> createPageOfJokes(Pageable pageable, long total) {
        List<Joke> jokes = new ArrayList<>(pageable.getPageSize());
        for (int index = 0; index < pageable.getPageSize(); index++) {
            Joke joke = createTestJoke();
            joke.setId(UUID.randomUUID());
            joke.setDate(TEST_DATE.plusDays(index));
            jokes.add(joke);
        }
        return new PageImpl<>(jokes, pageable, total);
    }

    public static Page<JokeRecord> createPageOfJokeRecords(Pageable pageable, long total) {
        return createPageOfJokes(pageable, total)
                .map(joke -> new JokeRecord(joke.getId(), joke.getDate(), joke.getJoke(), joke.getDescription()));
    }
}
