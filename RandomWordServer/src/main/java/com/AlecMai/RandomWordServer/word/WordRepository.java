package com.AlecMai.RandomWordServer.word;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordRepository
        extends JpaRepository<Word, Long> {

    @Query("SELECT w FROM Word w WHERE w.word = ?1")
    List<Word> findByWord(String searchWord);
}
