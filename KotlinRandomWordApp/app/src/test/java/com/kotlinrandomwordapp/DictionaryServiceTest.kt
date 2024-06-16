package com.kotlinrandomwordapp

import org.junit.Test

class DictionaryServiceTest {

    private val dictService: DictionaryService = DictionaryService()

    @Test
    fun testisInDictionaryValidWord() {
        assert(dictService.isInDictionary("Test"))
    }

    @Test
    fun testisInDictionaryInvalidWord() {
        assert(!dictService.isInDictionary("aeshdrshsrg"))
    }
}