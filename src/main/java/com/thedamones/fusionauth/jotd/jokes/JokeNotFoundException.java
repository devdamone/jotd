package com.thedamones.fusionauth.jotd.jokes;

public class JokeNotFoundException extends RuntimeException {

    public JokeNotFoundException(String message) {
        super(message);
    }
}
