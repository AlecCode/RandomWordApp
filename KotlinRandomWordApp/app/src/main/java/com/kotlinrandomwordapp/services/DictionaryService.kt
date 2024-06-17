package com.kotlinrandomwordapp.services

import com.kotlinrandomwordapp.constants.DICTIONARY_URL
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Call
import okhttp3.Response

class DictionaryService {
    private val client: OkHttpClient = OkHttpClient()

    fun isInDictionary(word: String): Boolean {
        return getWordFromDictionary(word).code / 100 == 2
    }

    // TODO: Need a definitions class if I want to mess with definitions
//    fun getWordDefinitions(word: String): String {
//        return getWordFromDictionary(word).body?.string() ?: throw Error("API returned malformed response")
//    }

    private fun getWordFromDictionary(word: String): Response {
        val request: Request = Request.Builder()
            .url(DICTIONARY_URL + word)
            .build()
        val call: Call = client.newCall(request)
        val response: Response = call.execute()

        return response
    }
}