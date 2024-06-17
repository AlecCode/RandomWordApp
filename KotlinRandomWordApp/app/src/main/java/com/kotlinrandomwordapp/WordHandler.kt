package com.kotlinrandomwordapp

import android.os.CountDownTimer
import com.example.kotlinrandomwordapp.databinding.FragmentFirstBinding
import com.kotlinrandomwordapp.services.DictionaryService
import com.kotlinrandomwordapp.services.OpenAIService

class WordHandler(binding: FragmentFirstBinding) {
    private val dictionaryService: DictionaryService = DictionaryService()
    private val openAIService: OpenAIService = OpenAIService()

    private var score: Int = 0
    private var currentWord: String = ""
    private var isValid = false
    private var timerStart : Boolean = false
    private val timer = object: CountDownTimer(10000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            binding.mainTimerText.text = (millisUntilFinished / 1000).toString()
        }

        override fun onFinish() {
            binding.mainTimerText.text = "Time Up!"
            binding.mainScoreText.text = "0"
            score = 0
            timerStart = false
        }
    }

    fun handleUserEntry(userInput: String, mainText: String): Unit {
        val inDictionary: Boolean = dictionaryService.isInDictionary(userInput)

        if ((mainText.isEmpty()
                    || mainText.last().equals(userInput.first(), ignoreCase=true))
            && inDictionary) {
            isValid = true
            score++
        } else {
            isValid = false
        }
    }

    fun getNonGPTVar(): Pair<String, Boolean> {
        return Pair(score.toString(), isValid)
    }

    suspend fun getNewWord(userInput: String): String {
        return openAIService.generateWord(userInput).toString()
    }

    fun restartTimer(): Unit {
        timer.start()
    }
}