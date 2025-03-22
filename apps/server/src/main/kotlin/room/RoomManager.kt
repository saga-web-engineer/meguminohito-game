package com.example.room

import io.ktor.websocket.*
import java.util.concurrent.ConcurrentHashMap

object RoomManager {
  private val rooms = ConcurrentHashMap<String, Room>()

  fun getRoom(roomId: String): Room = rooms.computeIfAbsent(roomId) { Room() }
}

class Room {
  private val players = ConcurrentHashMap.newKeySet<WebSocketSession>()
  private val hands = ConcurrentHashMap<WebSocketSession, String>()

  fun addPlayer(session: WebSocketSession) {
    players.add(session)
  }

  fun removePlayer(session: WebSocketSession) {
    players.remove(session)
    hands.remove(session)
  }

  fun setHand(session: WebSocketSession, hand: String) {
    hands[session] = hand
  }

  suspend fun notifyOtherPlayers(sender: WebSocketSession, message: String) {
    players.filter { it != sender }.forEach { player ->
      try {
        player.send(Frame.Text(message))
      } catch (e: Exception) {
        println("Error sending message: ${e.message}")
      }
    }
  }

  suspend fun checkAndJudge() {
    if (hands.size == players.size) {
      val results = hands.entries.joinToString { "${it.key.hashCode()} -> ${it.value}" }
      players.forEach { it.send(Frame.Text("結果: $results")) }
      hands.clear()
    }
  }
}
