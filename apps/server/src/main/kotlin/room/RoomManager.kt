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

  fun setHand(session: WebSocketSession, hand: String) {
    println("[DEBUG] プレイヤー ${session.hashCode()} の手を設定しています: $hand")
    hands[session] = hand
    println("[INFO] 手が設定されました。現在の手の数: ${hands.size}/${players.size}")
  }

  fun isEmpty(): Boolean {
    val empty = players.isEmpty()
    println("[DEBUG] ルームの空チェック結果: $empty")
    return empty
  }

  suspend fun notifyOtherPlayers(sender: WebSocketSession, message: String) {
    println("[DEBUG] 送信者 ${sender.hashCode()} から他のプレイヤーに通知しています")
    val otherPlayers = players.filter { it != sender }
    println("[INFO] ${otherPlayers.size} 人の他のプレイヤーにメッセージを送信しています")

    otherPlayers.forEach { player ->
      try {
        player.send(Frame.Text(message))
      } catch (e: Exception) {
        println("[ERROR] プレイヤー ${player.hashCode()} へのメッセージ送信中にエラーが発生しました: ${e.message}")
      }
    }
  }

  suspend fun checkAndJudge() {
    println("[DEBUG] 全プレイヤーが手を提出したか確認しています: ${hands.size}/${players.size}")
    if (hands.size == players.size) {
      println("[INFO] 全プレイヤーが手を提出しました。結果を計算しています...")
      val results = hands.entries.joinToString { "${it.key.hashCode()} -> ${it.value}" }
      println("[DEBUG] 結果: $results")

      players.forEach { player ->
        try {
          player.send(Frame.Text("結果: $results"))
        } catch (e: Exception) {
          println("[ERROR] プレイヤー ${player.hashCode()} への結果送信中にエラーが発生しました: ${e.message}")
        }
      }
      hands.clear()
      println("[INFO] 結果が全プレイヤーに送信され、手がクリアされました")
    } else {
      println("[INFO] より多くのプレイヤーが手を提出するのを待っています: ${hands.size}/${players.size}")
    }
  }
}
