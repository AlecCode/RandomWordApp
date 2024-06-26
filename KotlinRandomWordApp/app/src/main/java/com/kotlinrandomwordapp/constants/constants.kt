package com.kotlinrandomwordapp.constants

const val PREFERENCES_NAME: String = "word_train_preferences"

const val DICTIONARY_URL: String = "https://api.dictionaryapi.dev/api/v2/entries/en/"
const val ROLE_DEF: String = ("You must follow these rules:\n1. You must respond with a single" +
        " word that begins with the same letter as the last letter of the word the user sent.\n" +
        "2. You may not respond with the following words: ")
const val TIME_UP: String = "Time!"
const val SCORE: String = "Previous Best: "

// Hint Strings
const val EMPTY_ENTRY: String = "You need to enter something"
const val NOT_IN_DICTIONARY: String = "That's not in the dictionary"
const val NOT_VALID_CHAIN: String = "You need to start with an "
const val TOO_RECENT_WORD: String = "That word was just used"
const val INPUT_TOO_SOON: String = "ChatGPT hasn't picked a word yet"
const val LONGER_INPUT: String = "Longer word +1"

const val GAME_TIME: Long = 5000
const val TICK: Long = 5