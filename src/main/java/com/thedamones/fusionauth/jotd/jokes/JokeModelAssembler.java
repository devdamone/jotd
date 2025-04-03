package com.thedamones.fusionauth.jotd.jokes;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.function.Function;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class JokeModelAssembler implements RepresentationModelAssembler<JokeRecord, EntityModel<JokeRecord>> {

    private final JokeRepository jokeRepository;

    public JokeModelAssembler(JokeRepository jokeRepository) {
        this.jokeRepository = jokeRepository;
    }

    @Override
    public EntityModel<JokeRecord> toModel(JokeRecord joke) {
        EntityModel<JokeRecord> jokeModel = EntityModel.of(joke,
                linkTo(methodOn(JokeController.class).getJoke(joke.id())).withSelfRel());

        // add link for next day if one exists
        jokeRepository.findByDate(joke.date().plusDays(1))
                .map(toLink("nextDay"))
                .ifPresent(jokeModel::add);

        return jokeModel;
    }

    private Function<Joke, Link> toLink(String rel) {
        return joke -> linkTo(methodOn(JokeController.class).getJoke(joke.getId())).withRel(rel);
    }
}
