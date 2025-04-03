package com.thedamones.fusionauth.jotd.jokes;

import com.thedamones.fusionauth.jotd.config.IsAdmin;
import com.thedamones.fusionauth.jotd.config.IsUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class JokeService {

    private static final Logger logger = LoggerFactory.getLogger(JokeService.class);

    private final JokeRepository jokeRepository;
    private final ConversionService conversionService;

    @Autowired
    public JokeService(JokeRepository jokeRepository, ConversionService conversionService) {
        this.jokeRepository = jokeRepository;
        this.conversionService = conversionService;
    }

    @IsUser
    public Page<JokeRecord> getJokes(LocalDate date, Pageable pageable) {
        Page<Joke> jokes;
        if (date == null) {
            jokes = jokeRepository.findAll(pageable);
        }
        else {
            jokes = jokeRepository.findAllByDateGreaterThanEqual(date, pageable);
        }
        return jokes.map(toJokeRecord());
    }

    @IsAdmin
    @Transactional
    public JokeRecord addJoke(CreateJokeRecord request) {
        return createJoke(request)
                .map(this::saveJoke)
                .map(toJokeRecord())
                .orElseThrow(jokeServiceException("Exception while adding joke"));
    }

    @IsUser
    public JokeRecord getJoke(UUID id) {
        return jokeRepository.findById(id)
                .map(toJokeRecord())
                .orElseThrow(jokeNotFoundException(id));
    }

    public JokeRecord getJokeOfTheDay() {
        LocalDate today = LocalDate.now();
        return jokeRepository.findByDate(today)
                .map(toJokeRecord())
                .orElseThrow(noJokeOfTheDayException(today));
    }

    @IsAdmin
    @Transactional
    public JokeRecord updateJoke(UUID id, JokeRecord request) {
        return jokeRepository.findById(id)
                .map(withJokeRecord(request))
                .map(this::saveJoke)
                .map(toJokeRecord())
                .orElseThrow(jokeNotFoundException(request.id()));
    }

    @IsAdmin
    @Transactional
    public void removeJoke(UUID id) {
        if (!jokeRepository.existsById(id)) {
            throw new JokeNotFoundException("Joke with ID " + id + " not found");
        }
        jokeRepository.deleteById(id);
    }

    private Optional<Joke> createJoke(CreateJokeRecord request) {
        Joke joke = new Joke();
        joke.setDate(request.date());
        joke.setJoke(request.joke());
        joke.setDescription(request.description());
        return Optional.of(joke);
    }

    private Function<Joke, Joke> withJokeRecord(JokeRecord request) {
        return joke -> {
            joke.setId(request.id());
            joke.setDate(request.date());
            joke.setJoke(request.joke());
            joke.setDescription(request.description());
            return joke;
        };
    }

    private Joke saveJoke(Joke joke) {
        try {
            return jokeRepository.saveAndFlush(joke);
        } catch (Exception e) {
            if (e instanceof JpaSystemException) {
                throw new JokeDataOperationException(e);
            }
            else if (e instanceof DataIntegrityViolationException) {
                throw new JokeDataIntegrityException(e);
            } else {
                throw new JokeServiceException("Exception while saving joke", e);
            }
        }
    }

    private Function<Joke, JokeRecord> toJokeRecord() {
        return joke -> conversionService.convert(joke, JokeRecord.class);
    }

    private Supplier<JokeNotFoundException> jokeNotFoundException(UUID id) {
        return () -> new JokeNotFoundException("Joke with ID " + id + " not found");
    }

    private Supplier<NoJokeOfTheDayException> noJokeOfTheDayException(LocalDate date) {
        return () -> new NoJokeOfTheDayException("No Joke for date '" + date + "' exists");
    }

    private Supplier<JokeServiceException> jokeServiceException(String message) {
        return () -> new JokeServiceException(message);
    }

}
