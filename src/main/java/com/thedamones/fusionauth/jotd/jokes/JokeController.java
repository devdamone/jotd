package com.thedamones.fusionauth.jotd.jokes;

import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jokes")
public class JokeController {

    private final JokeService jokeService;
    private final JokeModelAssembler jokeModelAssembler;
    private final PagedResourcesAssembler<JokeRecord> pagedResourcesAssembler;

    public JokeController(JokeService jokeService, JokeModelAssembler jokeModelAssembler, PagedResourcesAssembler<JokeRecord> pagedResourcesAssembler) {
        this.jokeService = jokeService;
        this.jokeModelAssembler = jokeModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @GetMapping
    public PagedModel<EntityModel<JokeRecord>> getJokes(@RequestParam(required = false) LocalDate date, @ParameterObject Pageable pageable) {
        return pagedResourcesAssembler.toModel(jokeService.getJokes(date, pageable), jokeModelAssembler);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EntityModel<JokeRecord>> createJoke(@Valid @RequestBody CreateJokeRecord request) {
        JokeRecord newJoke = jokeService.addJoke(request);
        EntityModel<JokeRecord> model = jokeModelAssembler.toModel(newJoke);
        URI selfLink = model.getLink(IanaLinkRelations.SELF)
                .map(Link::toUri)
                .orElse(null);
        return ResponseEntity.created(selfLink).body(model);
    }

    @GetMapping("/today")
    public EntityModel<JokeRecord> getJokeOfTheDay() {
        JokeRecord joke = jokeService.getJokeOfTheDay();
        return jokeModelAssembler.toModel(joke);
    }

    @GetMapping("/{id}")
    public EntityModel<JokeRecord> getJoke(@PathVariable UUID id) {
        JokeRecord joke = jokeService.getJoke(id);
        return jokeModelAssembler.toModel(joke);
    }

    @PutMapping("/{id}")
    public EntityModel<JokeRecord> updateJoke(@PathVariable UUID id, @Valid @RequestBody JokeRecord request) {
        JokeRecord joke = jokeService.updateJoke(id, request);
        return jokeModelAssembler.toModel(joke);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJoke(@PathVariable UUID id) {
        jokeService.removeJoke(id);
    }

    @ExceptionHandler({JokeNotFoundException.class, NoJokeOfTheDayException.class})
    public ProblemDetail handleJokeNotFoundException(Exception ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(JokeDataIntegrityException.class)
    public ProblemDetail handleDuplicateJokeDateException(JokeDataIntegrityException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(JokeDataOperationException.class)
    public ProblemDetail handleJokeIdNotModifiableException(JokeDataOperationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

}
