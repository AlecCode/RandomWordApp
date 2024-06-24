package com.kotlinrandomwordapp

import com.kotlinrandomwordapp.services.DictionaryService
import com.kotlinrandomwordapp.services.OpenAIService
import java.util.ArrayDeque

class WordHandler() {
    private val dictionaryService: DictionaryService = DictionaryService()
    private val openAIService: OpenAIService = OpenAIService()

    private var score: Int = 0
    private var isValidChain: Boolean = false
    private var inDictionary: Boolean = false
    private var isNew: Boolean = false
    private var wordHistory: ArrayDeque<String> = ArrayDeque<String>()

    fun handleUserEntry(userInput: String, mainText: String): Unit {
        val user: String = userInput.lowercase()
        val main: String = mainText.lowercase()
        isValidChain = if (main.isNotEmpty()) main.last() == user.first() else true
        inDictionary = dictionaryService.isInDictionary(user)
        isNew = !wordHistory.contains(user)

        if (isValidChain && inDictionary && isNew) {
            score++
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

    fun getScore(): Int {
        return score
    }

    fun getCombinedFlags(): Boolean {
        return inDictionary && isValidChain && isNew
    }

    fun getIsValidChain(): Boolean {
        return isValidChain
    }

    fun getInDictionary(): Boolean {
        return inDictionary
    }

    fun getIsNew(): Boolean {
        return isNew
    }

    fun getWordHistory(): ArrayDeque<String> {
        return wordHistory
    }

    fun reset(): Unit {
        score = 0
        wordHistory.clear()
    }
}