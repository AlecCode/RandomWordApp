package com.kotlinrandomwordapp

import com.kotlinrandomwordapp.services.DictionaryService
import com.kotlinrandomwordapp.services.OpenAIService
import java.util.PriorityQueue

class WordHandler() {
    private val dictionaryService: DictionaryService = DictionaryService()
    private val openAIService: OpenAIService = OpenAIService()

    private var score: Int = 0
    private var isValidChain: Boolean = false
    private var inDictionary: Boolean = false
    private var notInHistory: Boolean = false
    private var timerStart : Boolean = false
    private var wordHistory: PriorityQueue<String> = PriorityQueue<String>()

    fun handleUserEntry(userInput: String, mainText: String): Unit {
        isValidChain = if (mainText.isNotEmpty()) mainText.last().equals(userInput.first(), ignoreCase=true) else true
        inDictionary = dictionaryService.isInDictionary(userInput)
        notInHistory = !wordHistory.contains(userInput.lowercase())

        if (isValidChain && inDictionary && notInHistory) {
            score++
        }
    }

    suspend fun getNewWord(userInput: String): String {
        addToHistory(userInput.lowercase())
        val newWord: String = openAIService.generateWord(userInput, wordHistory).toString()
        addToHistory(newWord.lowercase())
        return newWord
    }

    private fun addToHistory(word: String): Unit {
        if (wordHistory.size >= 30) {
            wordHistory.poll()
        }

        wordHistory.add(word)
    }

    fun getNonGPTVar(): Pair<String, Boolean> {
        return Pair(score.toString(), inDictionary && isValidChain && notInHistory)
    }

    fun getIsValidChain(): Boolean {
        return isValidChain
    }

    fun getInDictionary(): Boolean {
        return inDictionary
    }

    fun getNotInHistory(): Boolean {
        return notInHistory
    }

    fun resetScore(): Unit {
        score = 0
        timerStart = false
    }
}