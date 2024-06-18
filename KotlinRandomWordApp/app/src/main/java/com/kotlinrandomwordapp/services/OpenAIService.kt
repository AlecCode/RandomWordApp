package com.kotlinrandomwordapp.services

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.kotlinrandomwordapp.constants.OPEN_AI_API_KEY
import com.kotlinrandomwordapp.constants.ROLE_DEF
import java.util.PriorityQueue
import kotlin.time.Duration.Companion.seconds

class OpenAIService {
    private val openAI: OpenAI = OpenAI(
        token = OPEN_AI_API_KEY,
        timeout = Timeout(socket = 60.seconds)
    )
//    private val gptBackStory: ChatMessage = ChatMessage(
//        ChatRole.System,
//        content = ROLE_DEF
//    )

    private var previousWords: PriorityQueue<String> = PriorityQueue<String>()

    suspend fun generateWord(word: String): String? {
        addToHistory(word)

        val gptBackStory: ChatMessage = ChatMessage(
            ChatRole.System,
            content = ROLE_DEF + previousWords.toString()
        )
        val chatRequest: ChatCompletionRequest = ChatCompletionRequest(
            ModelId("gpt-3.5-turbo"),
            listOf<ChatMessage>(
                gptBackStory,
                ChatMessage(
                    ChatRole.User,
                    word
                )
            ),
            presencePenalty = 2.0
        )

        val chatResponse: ChatCompletion = openAI.chatCompletion(chatRequest)
        val generatedWord: String = chatResponse.choices[0].message.content.toString()

        addToHistory(generatedWord)
        return generatedWord
    }

    private fun addToHistory(word: String): Unit {
        if (previousWords.size >= 20) {
            previousWords.poll()
        }
        previousWords.add(word)
    }
}