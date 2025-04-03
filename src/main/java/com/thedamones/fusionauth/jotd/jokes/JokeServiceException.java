package com.thedamones.fusionauth.jotd.jokes;

public class JokeServiceException extends RuntimeException {

    public JokeServiceException(String message) {
        super(message);
    }

    public JokeServiceException(String message, Exception e) {
        super(message, e);
    }

}
