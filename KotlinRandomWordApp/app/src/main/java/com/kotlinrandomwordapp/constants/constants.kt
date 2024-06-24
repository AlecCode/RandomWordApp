package com.kotlinrandomwordapp.constants

const val DICTIONARY_URL: String = "https://api.dictionaryapi.dev/api/v2/entries/en/"
const val ROLE_DEF: String = ("You must follow these rules:\n1. You must respond with a single" +
        " word that begins with the same letter as the last letter of the word the user sent.\n" +
        "2. You may not respond with the following words: ")
const val NOT_IN_DICTIONARY = "That's not in the dictionary"
const val NOT_VALID_CHAIN = "You need to start with an "
const val TOO_RECENT_WORD = "You just used that word"
const val INPUT_TOO_SOON = "ChatGPT hasn't picked a word yet"