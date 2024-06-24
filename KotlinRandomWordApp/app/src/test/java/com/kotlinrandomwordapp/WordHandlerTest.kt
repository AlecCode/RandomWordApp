package com.kotlinrandomwordapp

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test

class WordHandlerTest {

    private val wordHandler: WordHandler = WordHandler()

    @After
    fun cleanUp() {
        wordHandler.reset()
    }

    @Test
    fun testHandleUserEntryValidInputEmptyMainText(): Unit {
        wordHandler.handleUserEntry("Test", "")

        assert(wordHandler.getScore() == 1)
        assert(wordHandler.getIsValidChain())
        assert(wordHandler.getInDictionary())
        assert(wordHandler.getIsNew())
    }

    @Test
    fun testHandleUserEntryValidInput(): Unit {
        wordHandler.handleUserEntry("Train", "Test")

        assert(wordHandler.getScore() == 1)
        assert(wordHandler.getIsValidChain())
        assert(wordHandler.getInDictionary())
        assert(wordHandler.getIsNew())
    }

    @Test
    fun testHandleUserEntryManyInputs(): Unit {
        wordHandler.handleUserEntry("Train", "Test")
        wordHandler.handleUserEntry("Train", "Test")
        wordHandler.handleUserEntry("Train", "Test")
        wordHandler.handleUserEntry("Train", "Test")
        wordHandler.handleUserEntry("Train", "Test")

        assert(wordHandler.getScore() == 5)
        assert(wordHandler.getIsValidChain())
        assert(wordHandler.getInDictionary())
        assert(wordHandler.getIsNew())
    }

    @Test
    fun testHandleUserEntryNotValidChain(): Unit {
        wordHandler.handleUserEntry("Test", "None")

        assert(wordHandler.getScore() == 0)
        assert(!wordHandler.getIsValidChain())
        assert(wordHandler.getInDictionary())
        assert(wordHandler.getIsNew())
    }

    @Test
    fun testHandleUserEntryNotInDictionary(): Unit {
        wordHandler.handleUserEntry("Invalid Entry", "")

        assert(wordHandler.getScore() == 0)
        assert(wordHandler.getIsValidChain())
        assert(!wordHandler.getInDictionary())
        assert(wordHandler.getIsNew())
    }

    @Test
    fun testHandleUserEntryInHistory(): Unit {
        runBlocking {
            wordHandler.getNewWord("Test")
        }
        wordHandler.handleUserEntry("Test", "")

        assert(wordHandler.getScore() == 0)
        assert(wordHandler.getIsValidChain())
        assert(wordHandler.getInDictionary())
        assert(!wordHandler.getIsNew())
    }

    @Test
    fun testGetNewWord(): Unit {
        val result: String = runBlocking {
            wordHandler.getNewWord("Test")
        }

        assert(result.isNotEmpty())
        assert(!result.contains(" "))
        assert(result.first().lowercase() == "t")
        assert(wordHandler.getWordHistory().size == 2)
    }

    @Test
    fun testGetNewWordManyTimes(): Unit {
        runBlocking {
            for (i in 1..11) {
                val result: String = wordHandler.getNewWord("Test")

                assert(result.isNotEmpty())
                assert(!result.contains(" "))
                assert(result.first().lowercase() == "t")
            }
        }

        assert(wordHandler.getWordHistory().size == 20)
    }
}