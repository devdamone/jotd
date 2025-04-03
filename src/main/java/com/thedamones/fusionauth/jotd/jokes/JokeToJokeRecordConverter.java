package com.thedamones.fusionauth.jotd.jokes;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class JokeToJokeRecordConverter implements Converter<Joke, JokeRecord> {

    @Override
    public JokeRecord convert(Joke source) {
        return new JokeRecord(
                source.getId(),
                source.getDate(),
                source.getJoke(),
                source.getDescription()
        );
    }
}
