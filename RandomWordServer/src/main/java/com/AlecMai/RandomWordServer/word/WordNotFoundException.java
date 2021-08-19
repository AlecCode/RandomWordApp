package com.AlecMai.RandomWordServer.word;

public class WordNotFoundException extends RuntimeException {
    WordNotFoundException (Long id) {
        super("Could not find word with id: " + id);
    }

    WordNotFoundException (String word) {
        super("Could not find " + word);
    }

    WordNotFoundException () {
        super("An error occurred");
    }
}
