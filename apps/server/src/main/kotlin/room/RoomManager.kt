package com.example.room

import io.ktor.websocket.* // WebSocketセッションを扱うためのインポート
import java.util.concurrent.ConcurrentHashMap // スレッドセーフなハッシュマップ

object RoomManager {
  // ルームIDとルームオブジェクトのマッピング
  private val rooms = ConcurrentHashMap<String, Room>()

  fun getRoom(roomId: String): Room = rooms.computeIfAbsent(roomId) {
    println("[INFO] IDが $roomId の新しいルームを作成しています")
    Room()
  }

  fun removeRoomIfEmpty(roomId: String) {
    println("[DEBUG] ルーム $roomId が空かどうかを確認しています")
    val room = rooms[roomId]
    if (room != null && room.isEmpty()) {
      rooms.remove(roomId)
      println("[INFO] ルーム $roomId は空のため削除されました")
    } else {
      println("[DEBUG] ルーム $roomId にはまだプレイヤーがいるか、存在していません")
    }
  }
}

class Room {
  // プレイヤーのWebSocketセッションを格納するセット
  private val players = ConcurrentHashMap.newKeySet<WebSocketSession>()
  // プレイヤーの手（選択）を格納するマップ
  private val hands = ConcurrentHashMap<WebSocketSession, String>()
  // 問題番号を3つランダムに選出する（仮：1~100）
  private val sharedRandomNumbers = List(3) { (1..100).random() }

  suspend fun addPlayer(session: WebSocketSession) {
    println("[DEBUG] プレイヤーをルームに追加しています。セッション: ${session.hashCode()}")
    players.add(session)
    println("[INFO] プレイヤーが追加されました。現在のプレイヤー数: ${players.size}")
    // Roomの問題番号をPlayerにおくる
    session.send("""{"type": "question", "content": "${sharedRandomNumbers.joinToString(", ")}"}""")
  }

  fun removePlayer(session: WebSocketSession) {
    println("[DEBUG] プレイヤーをルームから削除しています。セッション: ${session.hashCode()}")
    players.remove(session)
    hands.remove(session)
    println("[INFO] プレイヤーが削除されました。現在のプレイヤー数: ${players.size}")
  }

  fun isEmpty(): Boolean {
    val empty = players.isEmpty()
    println("[DEBUG] ルームの空チェック結果: $empty")
    return empty
  }
}
