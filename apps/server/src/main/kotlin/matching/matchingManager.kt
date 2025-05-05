package com.example.matching

import io.ktor.websocket.* // WebSocketセッションを扱うためのインポート
import kotlinx.coroutines.sync.Mutex // 同期処理を行うためのミューテックス
import kotlinx.coroutines.sync.withLock // ミューテックスを使ったロック処理
import kotlinx.coroutines.delay // 一定時間待機するための関数
import com.example.room.RoomManager

class MatchingManager {
    private val waitingPlayers = mutableListOf<WebSocketSession>() // 待機中のプレイヤーを格納するリスト
    private val mutex = Mutex() // 同期処理を行うためのミューテックス

    suspend fun addPlayer(session: WebSocketSession) {
        println("[DEBUG] addPlayer called. Session: $session")
        println("[INFO] addPlayer called. Current waiting players: ${waitingPlayers.size}")
        var isFirstPlayer = false

        mutex.withLock {
            if (waitingPlayers.contains(session)) {
                println("[DEBUG] Session already exists. Skipping addition.")
                return
            }
            // 同じセッションがリストに追加されないようにする
            if (!waitingPlayers.contains(session)) {
                // 新しいプレイヤーを待機リストに追加
                waitingPlayers.add(session) 
                println("[DEBUG] Session added. Current waiting players: ${waitingPlayers.map { it.hashCode() }}")
            } else {
                println("[DEBUG] Session already exists. Skipping addition.")
            }
            println("[INFO] Current waiting players: ${waitingPlayers.size}")
            waitingPlayers.forEach { player ->
                // 現在の待機プレイヤー数を送信
                player.send(waitingPlayers.size.toString())
            }
            if (waitingPlayers.size == 1) {
                // 最初のプレイヤーかどうかを判定
                isFirstPlayer = true 
            }
        }

        if (isFirstPlayer) {
            delay(10000)
            println("[DEBUG] Timer ended. Waiting players: ${waitingPlayers.map { it.hashCode() }}")
            println("[INFO] Timer ended. Waiting players: ${waitingPlayers.size}")

            mutex.withLock {
                // 待機リストが空でない場合
                if (waitingPlayers.isNotEmpty()) {
                    // 現在の待機リストをコピー
                    val playersToStart = waitingPlayers.toList()
                    // 待機リストをクリア
                    waitingPlayers.clear()
                    startGame(playersToStart)
                }
            }
        }
    }

    suspend fun removePlayer(session: WebSocketSession) {
        mutex.withLock {
            if (waitingPlayers.remove(session)) {
                println("[DEBUG] Session removed. Current waiting players: ${waitingPlayers.map { it.hashCode() }}")

                // 他のプレイヤーに通知
                waitingPlayers.forEach { player ->
                    player.send(waitingPlayers.size.toString())
                }

                // 待機リストが空の場合、タイマーをリセット
                if (waitingPlayers.isEmpty()) {
                    println("[INFO] All players have left. Resetting timer.")
                }
            } else {
                println("[DEBUG] Session not found in waitingPlayers.")
            }
        }
    }

    private suspend fun startGame(players: List<WebSocketSession>) {
        // 一意のルームIDを生成
        val roomId = System.currentTimeMillis()
        println("[INFO] Room $roomId created with players: ${players.map { it.hashCode() }}")

        players.forEach { player ->
            // 各プレイヤーにルームIDを通知
            player.send(roomId.toString())
        }

        val room = RoomManager.getRoom(roomId.toString())
        // プレイヤーをルームに追加
        players.forEach { room.addPlayer(it) } 
    }
}