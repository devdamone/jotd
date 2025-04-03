package com.thedamones.fusionauth.jotd.jokes;

public class NoJokeOfTheDayException extends RuntimeException{
    public NoJokeOfTheDayException(String message) {
        super(message);
    }
}
