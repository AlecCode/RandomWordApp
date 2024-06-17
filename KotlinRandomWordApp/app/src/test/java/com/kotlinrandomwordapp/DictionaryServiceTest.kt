package com.kotlinrandomwordapp

import com.kotlinrandomwordapp.services.DictionaryService
import org.junit.Test

class DictionaryServiceTest {

    private val dictService: DictionaryService = DictionaryService()

    @Test
    fun testIsInDictionaryValidWord() {
        assert(dictService.isInDictionary("Test"))
    }

    @Test
    fun testIsInDictionaryInvalidWord() {
        assert(!dictService.isInDictionary("aeshdrshsrg"))
    }
}