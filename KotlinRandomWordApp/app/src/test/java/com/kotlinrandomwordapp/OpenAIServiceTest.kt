package com.kotlinrandomwordapp

import com.kotlinrandomwordapp.services.OpenAIService
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.PriorityQueue

class OpenAIServiceTest {

    private val openAIService: OpenAIService = OpenAIService()

    @Test
    fun testisInDictionaryValidWord() {
        runBlocking {
            val wordResp: String? = openAIService.generateWord("Test", PriorityQueue<String>())
            if (wordResp != null) {
                assert(wordResp.first().equals('t', true))
            } else {
                assert(false)
            }
        }
    }

//    @Test
//    fun testisInDictionaryInvalidWord() {
//        assert()
//    }
}