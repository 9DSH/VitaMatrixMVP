package com.swings.vitamatrix.api

import com.swings.vitamatrix.models.Message
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class ChatRequest(
    val model: String,
    val messages: List<Map<String, String>>
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

//data class Message(
//    val role: String,
//    val content: String,
//)

interface OpenAiApi {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    fun sendMessage(
        @Body request: ChatRequest
    ): Call<ChatResponse>
}

