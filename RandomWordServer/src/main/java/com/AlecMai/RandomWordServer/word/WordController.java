package com.AlecMai.RandomWordServer.word;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WordController {
    private final WordService wordService;

    @Autowired
    public WordController(WordService wordService) {
        this.wordService = wordService;
    }

    @GetMapping("api/v1/words")
    public List<Word> getWords() {
        return wordService.getWords();
    }

    @GetMapping("api/v1/get/{word}")
    public List<Word> getWord(@PathVariable String word) {
        return wordService.getWord(word);
    }
}
