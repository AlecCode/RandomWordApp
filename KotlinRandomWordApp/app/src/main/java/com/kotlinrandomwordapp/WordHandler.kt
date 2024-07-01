package com.kotlinrandomwordapp

import com.kotlinrandomwordapp.services.DictionaryService
import com.kotlinrandomwordapp.services.OpenAIService
import java.util.ArrayDeque

class WordHandler() {
    private val dictionaryService: DictionaryService = DictionaryService()
    private val openAIService: OpenAIService = OpenAIService()

    var score: Int = 0
        private set
    var isValidChain: Boolean = false
        private set
    var inDictionary: Boolean = false
        private set
    var isNew: Boolean = false
        private set
    var longer: Boolean = false
        private set
    var wordHistory: ArrayDeque<String> = ArrayDeque<String>()
        private set

    fun handleUserEntry(userInput: String, givenText: String): Unit {
        val entry: String = userInput.lowercase()
        val given: String = givenText.lowercase()
        isValidChain = if (given.isNotEmpty()) given.last() == entry.first() else true
        inDictionary = dictionaryService.isInDictionary(entry)
        isNew = !wordHistory.contains(entry)

        if (isValidChain && inDictionary && isNew) {
            score++

            if (userInput.length > givenText.length && given.isNotEmpty()) {
                score++
                longer = true
            } else {
                longer = false
            }
        }
    }

    suspend fun getNewWord(userInput: String): String {
        val newWord: String = openAIService.generateWord(userInput, wordHistory).toString()

        addToHistory(newWord.lowercase())
        addToHistory(userInput.lowercase())

        return newWord
    }

    private fun addToHistory(word: String): Unit {
        if (wordHistory.size >= 20) {
            wordHistory.poll()
        }

        wordHistory.add(word)
    }

    fun getCombinedFlags(): Boolean {
        return inDictionary && isValidChain && isNew
    }

    fun reset(): Unit {
        score = 0
        wordHistory.clear()
    }
}