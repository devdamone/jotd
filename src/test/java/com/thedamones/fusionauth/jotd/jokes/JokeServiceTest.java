package com.thedamones.fusionauth.jotd.jokes;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static com.thedamones.fusionauth.jotd.jokes.TestJokes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JokeServiceTest {

    private static final Pageable DEFAULT_PAGE_REQUEST = PageRequest.ofSize(20);

    @Mock
    private JokeRepository jokeRepository;

    @Spy
    private static GenericConversionService conversionService = new DefaultConversionService();

    @InjectMocks
    private JokeService jokeService;

    @BeforeAll
    static void setUp() {
        conversionService.addConverter(new JokeToJokeRecordConverter());
    }

    @Test
    void getJokes_shouldReturnFirstPage() {
        when(jokeRepository.findAll(DEFAULT_PAGE_REQUEST)).thenReturn(Page.empty(DEFAULT_PAGE_REQUEST));

        Page<JokeRecord> jokes = jokeService.getJokes(null, DEFAULT_PAGE_REQUEST);

        assertEquals(DEFAULT_PAGE_REQUEST.getPageNumber(), jokes.getNumber());
        assertEquals(DEFAULT_PAGE_REQUEST.getPageSize(), jokes.getSize());
        assertEquals(0, jokes.getTotalPages());
        assertEquals(0, jokes.getTotalElements());
    }

    @Test
    void getJokes_shouldReturnSpecifiedPage() {
        Pageable pageRequest = PageRequest.of(1, 20);
        when(jokeRepository.findAll(DEFAULT_PAGE_REQUEST)).thenReturn(Page.empty(pageRequest));

        Page<JokeRecord> jokes = jokeService.getJokes(null, DEFAULT_PAGE_REQUEST);

        assertEquals(pageRequest.getPageNumber(), jokes.getNumber());
        assertEquals(pageRequest.getPageSize(), jokes.getSize());
        assertEquals(0, jokes.getTotalPages());
        assertEquals(0, jokes.getTotalElements());
    }

    @Test
    void getJokes_withDateFilter() {
        LocalDate today = LocalDate.now();
        when(jokeRepository.findAllByDateGreaterThanEqual(today, DEFAULT_PAGE_REQUEST)).thenReturn(Page.empty(DEFAULT_PAGE_REQUEST));

        Page<JokeRecord> jokes = jokeService.getJokes(today, DEFAULT_PAGE_REQUEST);

        assertEquals(DEFAULT_PAGE_REQUEST.getPageNumber(), jokes.getNumber());
        assertEquals(DEFAULT_PAGE_REQUEST.getPageSize(), jokes.getSize());
        assertEquals(0, jokes.getTotalPages());
        assertEquals(0, jokes.getTotalElements());
    }

    @Test
    void addJoke_shouldSaveJoke() {
        Joke joke = createTestJoke();
        CreateJokeRecord createJokeRecord = createTestCreateJokeRecord();
        when(jokeRepository.saveAndFlush(any(Joke.class))).thenReturn(joke);

        JokeRecord result = jokeService.addJoke(createJokeRecord);

        assertTestJokeRecord(result);
    }

    @Test
    void addJoke_whenConversionFails() {
        Joke joke = createTestJoke();
        CreateJokeRecord createJokeRecord = createTestCreateJokeRecord();
        when(jokeRepository.saveAndFlush(any(Joke.class))).thenReturn(joke);
        when(conversionService.convert(joke, JokeRecord.class)).thenReturn(null);

        assertThrows(JokeServiceException.class, () -> jokeService.addJoke(createJokeRecord));
    }

    @Test
    void addJoke_whenDataIntegrityViolationException() {
        CreateJokeRecord createJokeRecord = createTestCreateJokeRecord();
        when(jokeRepository.saveAndFlush(any(Joke.class))).thenThrow(DataIntegrityViolationException.class);

        assertThrows(JokeDataIntegrityException.class, () -> jokeService.addJoke(createJokeRecord));
    }

    @Test
    void addJoke_whenAnyOtherException() {
        CreateJokeRecord createJokeRecord = createTestCreateJokeRecord();
        when(jokeRepository.saveAndFlush(any(Joke.class))).thenThrow(RuntimeException.class);

        assertThrows(JokeServiceException.class, () -> jokeService.addJoke(createJokeRecord));
    }

    @Test
    void getJoke_shouldReturnJokeRecord() {
        Joke joke = createTestJoke();
        JokeRecord jokeRecord = createTestJokeRecord();
        when(jokeRepository.findById(jokeRecord.id())).thenReturn(Optional.of(joke));

        JokeRecord result = jokeService.getJoke(jokeRecord.id());

        assertTestJokeRecord(result);
    }

    @Test
    void getJoke_whenNotFound() {
        when(jokeRepository.findById(TEST_ID)).thenReturn(Optional.empty());

        assertThrows(JokeNotFoundException.class, () -> jokeService.getJoke(TEST_ID));
    }

    @Test
    void getJokeOfTheDay_shouldReturnJokeRecord() {
        Joke joke = createTestJoke();
        JokeRecord jokeRecord = createTestJokeRecord();
        when(jokeRepository.findByDate(jokeRecord.date())).thenReturn(Optional.of(joke));

        JokeRecord result = jokeService.getJokeOfTheDay();

        assertTestJokeRecord(result);
    }

    @Test
    void getJokeOfTheDay_whenNotFound() {
        LocalDate today = LocalDate.now();
        when(jokeRepository.findByDate(today)).thenReturn(Optional.empty());

        assertThrows(NoJokeOfTheDayException.class, () -> jokeService.getJokeOfTheDay());
    }

    @Test
    void updateJoke_shouldUpdateJoke() {
        Joke joke = createTestJoke();
        JokeRecord jokeRecord = createTestJokeRecord();
        when(jokeRepository.findById(jokeRecord.id())).thenReturn(Optional.of(joke));
        when(jokeRepository.saveAndFlush(any(Joke.class))).thenReturn(joke);

        JokeRecord result = jokeService.updateJoke(jokeRecord.id(), jokeRecord);

        assertTestJokeRecord(result);
    }

    @Test
    void updateJoke_whenDataIntegrityViolationException() {
        Joke joke = createTestJoke();
        JokeRecord jokeRecord = createTestJokeRecord();
        when(jokeRepository.findById(jokeRecord.id())).thenReturn(Optional.of(joke));
        when(jokeRepository.saveAndFlush(any(Joke.class))).thenThrow(DataIntegrityViolationException.class);

        assertThrows(JokeDataIntegrityException.class, () -> jokeService.updateJoke(jokeRecord.id(), jokeRecord));
    }

    /**
     * JpaSystemException is thrown when some error occurs within JPA such as trying to modify the id of an entity.
     */
    @Test
    void updateJoke_whenJpaSystemException() {
        Joke joke = createTestJoke();
        JokeRecord jokeRecord = createTestJokeRecord();
        when(jokeRepository.findById(jokeRecord.id())).thenReturn(Optional.of(joke));
        when(jokeRepository.saveAndFlush(any(Joke.class))).thenThrow(JpaSystemException.class);

        assertThrows(JokeDataOperationException.class, () -> jokeService.updateJoke(jokeRecord.id(), jokeRecord));
    }

    @Test
    void updateJoke_whenNotFound() {
        JokeRecord jokeRecord = createTestJokeRecord();
        when(jokeRepository.findById(jokeRecord.id())).thenReturn(Optional.empty());

        assertThrows(JokeNotFoundException.class, () -> jokeService.updateJoke(jokeRecord.id(), jokeRecord));
    }

    @Test
    void removeJoke_shouldDeleteJoke() {
        when(jokeRepository.existsById(TEST_ID)).thenReturn(true);

        jokeService.removeJoke(TEST_ID);

        verify(jokeRepository, times(1)).deleteById(TEST_ID);
    }

    @Test
    void removeJoke_whenNotFound() {
        when(jokeRepository.existsById(TEST_ID)).thenReturn(false);

        assertThrows(JokeNotFoundException.class, () -> jokeService.removeJoke(TEST_ID));
    }

}