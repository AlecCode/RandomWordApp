package com.AlecMai.RandomWordServer.word;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Configuration
public class WordConfig {
    private List<Word> words = new ArrayList<Word>();

    @Bean
    CommandLineRunner commandLineRunner(WordRepository repository) {
        loadWords();

        return args -> {
            repository.saveAll(words);
        };
    }

    private void loadWords() {
        try {
            File wordsFile = new File("english.txt");
            Scanner reader = new Scanner(wordsFile);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                Word newWord = new Word(data);
                words.add(newWord);
            }
        } catch (Exception e) {
            System.out.println("Error Occurred");
            e.printStackTrace();
        }
    }
}
