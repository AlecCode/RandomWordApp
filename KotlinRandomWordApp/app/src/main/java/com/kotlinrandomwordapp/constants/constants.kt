package com.kotlinrandomwordapp.constants

const val DICTIONARY_URL: String = "https://api.dictionaryapi.dev/api/v2/entries/en/"
const val ROLE_DEF: String = ("Respond with a single word that begins with the same letter as the" +
        " last letter of the word the user sent. Do not repeat words, the previous words were: ")
const val NOT_IN_DICTIONARY = "That's not in the dictionary"
const val NOT_VALID_CHAIN = "You need to start with an "
const val TOO_RECENT_WORD = "You just used that word"
const val INPUT_TOO_SOON = "ChatGPT hasn't picked a word yet"