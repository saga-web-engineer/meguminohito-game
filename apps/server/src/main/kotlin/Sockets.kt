package com.example

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import kotlin.time.Duration.Companion.seconds
import com.example.room.RoomManager

val clients: MutableList<WebSocketSession> = mutableListOf()

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/ws/{roomId}") {
            clients.add(this)
            val roomId = call.parameters["roomId"] ?: return@webSocket close(
                CloseReason(
                    CloseReason.Codes.VIOLATED_POLICY,
                    "No Room ID"
                )
            )
            val room = RoomManager.getRoom(roomId)
            room.addPlayer(this)

            try {
                for (frame in incoming) {

                    if (frame is Frame.Text) {
                        val message = frame.readText()
                        println(message)
                        println(message)
                        println(message)
                        println(message)
                        println(message)
                        if (message in listOf("グー", "チョキ", "パー")) {
                            room.setHand(this, message)
                            room.notifyOtherPlayers(this, "相手が $message を出しました！")
                            room.checkAndJudge()
                        } else {
                            clients.forEach {
                                it.send("無効な手です: $message")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println(e)
            } finally {
                room.removePlayer(this)
                clients.remove(this)
            }
        }
    }
}
