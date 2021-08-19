package com.AlecMai.RandomWordServer.word;

import javax.persistence.*;

@Entity
@Table
public class Word {
    @Id
    @SequenceGenerator(
            name = "word_sequence",
            sequenceName = "word_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "word_sequence"
    )
    private long id;
    private String word;

    public Word(long id, String word) {
        this.id = id;
        this.word = word;
    }

    public Word(String word) {
        this.word = word;
    }

    public Word() {}

    public String getWord() {
        return word;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", word='" + word + '\'' +
                '}';
    }
}
