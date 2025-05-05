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
        println("[DEBUG] addPlayer called. Session: $session") // セッション情報をログに記録
        println("[INFO] addPlayer called. Current waiting players: ${waitingPlayers.size}") // ログを英語に変更
        var isFirstPlayer = false

        mutex.withLock {
            if (!waitingPlayers.contains(session)) { // 同じセッションがリストに追加されないようにする
                waitingPlayers.add(session) // 新しいプレイヤーを待機リストに追加
                println("[DEBUG] Session added. Current waiting players: ${waitingPlayers.map { it.hashCode() }}") // ログを追加
            } else {
                println("[DEBUG] Session already exists. Skipping addition.") // 重複時のログ
            }
            println("[INFO] Current waiting players: ${waitingPlayers.size}") // ログを英語に変更
            waitingPlayers.forEach { player ->
                player.send("現在の待機プレイヤー数: ${waitingPlayers.size}") // 現在の待機プレイヤー数を送信
            }
            if (waitingPlayers.size == 1) {
                isFirstPlayer = true // 最初のプレイヤーかどうかを判定
            }
        }

        if (isFirstPlayer) {
            delay(10000) // 最大10秒間待機
            println("[DEBUG] Timer ended. Waiting players: ${waitingPlayers.map { it.hashCode() }}") // タイマー終了後の待機プレイヤー詳細をログに記録
            println("[INFO] Timer ended. Waiting players: ${waitingPlayers.size}") // ログを英語に変更

            mutex.withLock {
                if (waitingPlayers.isNotEmpty()) { // 待機リストが空でない場合
                    val playersToStart = waitingPlayers.toList() // 現在の待機リストをコピー
                    waitingPlayers.clear() // 待機リストをクリア
                    startGame(playersToStart) // ゲームを開始
                }
            }
        }
    }

    suspend fun removePlayer(session: WebSocketSession) {
        mutex.withLock {
            if (waitingPlayers.remove(session)) {
                println("[DEBUG] Session removed. Current waiting players: ${waitingPlayers.map { it.hashCode() }}") // ログを追加

                // 他のプレイヤーに通知
                waitingPlayers.forEach { player ->
                    player.send("プレイヤーが離脱しました。現在の待機プレイヤー数: ${waitingPlayers.size}")
                }

                // 待機リストが空の場合、タイマーをリセット
                if (waitingPlayers.isEmpty()) {
                    println("[INFO] All players have left. Resetting timer.")
                }
            } else {
                println("[DEBUG] Session not found in waitingPlayers.") // セッションが見つからない場合のログ
            }
        }
    }

    private suspend fun startGame(players: List<WebSocketSession>) {
        val roomId = System.currentTimeMillis() // 一意のルームIDを生成
        println("[INFO] Room $roomId created with players: ${players.map { it.hashCode() }}")

        players.forEach { player ->
            player.send(roomId.toString()) // 各プレイヤーにルームIDを通知
            player.send(players.size.toString()) // マッチングした人数
        }

        val room = RoomManager.getRoom(roomId.toString())
        players.forEach { room.addPlayer(it) } // プレイヤーをルームに追加
    }
}