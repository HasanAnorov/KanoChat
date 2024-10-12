package com.ierusalem.androchat.core.updater


import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

typealias Message = Int

private fun runWithTimeTrack(action: () -> Unit) {
    val startTime = System.currentTimeMillis()

    action.invoke()

    val endTime = System.currentTimeMillis()
    println("\n Took time: ${endTime - startTime}")
}

fun main() {
    val messages = (1..10).toList()

    runWithTimeTrack {
        println("\n====== No concurrency ======")
        runBlocking { uploadMessages1(messages)  }
    }

    runWithTimeTrack {
        println("\n====== Concurrency with chunks ======")
        runBlocking { uploadMessages2(messages, chunkSize = 5)  }
    }

    runWithTimeTrack {
        println("\n====== Concurrency with flows and with chunks ======")
        runBlocking { uploadMessagesWithStream(messages, chunkSize = 5) }
    }
}


private suspend fun uploadMessages1(messages: List<Message>) {
    messages.forEach { uploadMessage(it) }
}

private suspend fun uploadMessages2(messages: List<Message>, chunkSize: Int) = withContext(Dispatchers.IO) {
    val chunks = messages.chunked(chunkSize)

    chunks.forEach {
        it.map { async { uploadMessage(it) } }.awaitAll()
    }
}

private suspend fun uploadMessagesWithStream(messages: List<Message>, chunkSize: Int) = withContext(Dispatchers.IO) {
    val uploadingMessagesCountStream = MutableStateFlow(0)

    flow {
        var index = 0
        while (index < messages.size) {
            val remainingCount = chunkSize - uploadingMessagesCountStream.first { it < chunkSize }

            val subMessages = messages.drop(index).take(remainingCount)
            index += subMessages.size

            subMessages.forEach { emit(it) }
        }

        uploadingMessagesCountStream.first { it <= 0 }
    }.collect {
        uploadingMessagesCountStream.update { it + 1 }
        launch {
            uploadMessage(it)
            uploadingMessagesCountStream.update { it - 1 }
        }
    }
}

private suspend fun uploadMessage(message: Message) {
    print("$message ")

    val delay = (message % 3 + 1) * 1000L // delay might be 1s, 2s or 3s
    delay(delay)
}
