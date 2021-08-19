package com.AlecMai.RandomWordServer.word;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WordService {
    private final WordRepository wordRepository;

    @Autowired
    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    public List<Word> getWords() {
        return wordRepository.findAll();
    }

    public Word getWordByID(Long id) {
        return wordRepository.findById(id).orElseThrow(() -> new WordNotFoundException(id));
    }

    public List<Word> getWord(String word) {
        if (wordRepository.findByWord(word).size() == 0) {
            throw new WordNotFoundException(word);
        } else {
            return wordRepository.findByWord(word);
        }
    }

    public Word getRandomWordWithStart(char firstLetter) {
        return null;
    }
}
