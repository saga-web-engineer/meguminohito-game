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
import com.example.matching.MatchingManager

val clients: MutableList<WebSocketSession> = mutableListOf()
val matchingManager = MatchingManager()

fun Application.configureSockets() {
  install(WebSockets) {
    pingPeriod = 15.seconds
    timeout = 15.seconds
    maxFrameSize = Long.MAX_VALUE
    masking = false
  }
  routing {
    webSocket("/ws/matching") {
      matchingManager.addPlayer(this)
      try {
        for (frame in incoming) {
          if (frame is Frame.Text) {
            val message = frame.readText()
            println("[INFO] メッセージを受信しました: $message")
          }
        }
      } catch (e: Exception) {
        println("[ERROR] WebSocketセッションでの例外発生: ${e.message}")
      } finally {
        matchingManager.removePlayer(this) // セッション終了時にプレイヤーを削除
      }
    }

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
            println(message) // メッセージを1回だけ出力
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
        println("[ERROR] 例外が発生しました: ${e.message}")
      } finally {
        room.removePlayer(this) // プレイヤーをルームから削除
        RoomManager.removeRoomIfEmpty(roomId) // ルームが空なら削除
        clients.remove(this)
      }
    }
  }
}
