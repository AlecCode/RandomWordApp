package com.AlecMai.RandomWordServer.word;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Configuration
public class WordService {
    private ArrayList<String> words = new ArrayList<String>();

    @Bean
    CommandLineRunner commandLineRunner(WordRespository repository) {
        return args -> {
            List<S> s = repository.saveAll(words);
        };
    }

    private void loadWords() {
        try {
            File wordsFile = new File("english.txt");
            Scanner reader = new Scanner(wordsFile);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                words.add(data);
            }
        } catch (Exception e) {
            System.out.println("Error Occurred");
            e.printStackTrace();
        }
    }
}
