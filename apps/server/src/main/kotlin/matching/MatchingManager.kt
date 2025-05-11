package com.example.matching

import io.ktor.websocket.* // WebSocketセッションを扱うためのインポート
import kotlinx.coroutines.sync.Mutex // 同期処理を行うためのミューテックス
import kotlinx.coroutines.sync.withLock // ミューテックスを使ったロック処理
import kotlinx.coroutines.delay // 一定時間待機するための関数
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MatchingManager {
  // 待機中のプレイヤーを格納するリスト
  private val waitingPlayers = mutableListOf<WebSocketSession>()
  // 同期処理を行うためのミューテックス
  private val mutex = Mutex()
  private var timerJob: Job? = null
  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  suspend fun addPlayer(session: WebSocketSession) {
    println("[DEBUG] addPlayer() セッション: $session")
    println("[INFO] addPlayer() 現在の待機プレイヤー数: ${waitingPlayers.size}")
    var isFirstPlayer = false
    var shouldStartImmediately = false

    mutex.withLock {
      if (waitingPlayers.contains(session) || waitingPlayers.size >= 5) return

      // 新しいプレイヤーを待機リストに追加
      waitingPlayers.add(session)
      println("[DEBUG] セッションが追加。現在の待機プレイヤー: ${waitingPlayers.map { it.hashCode() }}")

      println("[INFO] 現在の待機プレイヤー数: ${waitingPlayers.size}")
      waitingPlayers.forEach { player ->
        // 現在の待機プレイヤー数を送信
        player.send("""{"type": "player", "content": "${waitingPlayers.size.toString()}"}""")
      }

      if (waitingPlayers.size == 1) {
        // 最初のプレイヤーかどうかを判定
        isFirstPlayer = true
      }

      // 5人揃ったら即座にゲーム開始
      if (waitingPlayers.size == 5) {
        shouldStartImmediately = true
      }
    }

    if (shouldStartImmediately) {
      // タイマーが動いていればキャンセル
      timerJob?.cancel()
      timerJob = null
      mutex.withLock {
        val playersToStart = waitingPlayers.take(5)
        waitingPlayers.removeAll(playersToStart)
        startGame(playersToStart)
      }
      return
    }

    if (isFirstPlayer) {
      // 既存のタイマーがあればキャンセル
      timerJob?.cancel()
      timerJob = coroutineScope.launch {
        delay(10000)
        println("[DEBUG] タイマーが終了。待機プレイヤー: ${waitingPlayers.map { it.hashCode() }}")
        println("[INFO] タイマーが終了。待機プレイヤー数: ${waitingPlayers.size}")

        mutex.withLock {
          if (waitingPlayers.isNotEmpty()) {
            val playersToStart = waitingPlayers.take(5)
            waitingPlayers.removeAll(playersToStart)
            startGame(playersToStart)
          }
        }
      }
    }
  }

  suspend fun removePlayer(session: WebSocketSession) {
    mutex.withLock {
      if (waitingPlayers.remove(session)) {
        println("[DEBUG] セッションが削除。現在の待機プレイヤー: ${waitingPlayers.map { it.hashCode() }}")

        // 他のプレイヤーに通知
        waitingPlayers.forEach { player ->
          player.send("""{"type": "player", "content": "${waitingPlayers.size.toString()}"}""")
        }
      }
    }
  }

  private suspend fun startGame(players: List<WebSocketSession>) {
    // 一意のルームIDを生成
    val roomId = System.currentTimeMillis()
    println("[INFO] ルーム $roomId が作成されました。プレイヤー: ${players.map { it.hashCode() }}")

    players.forEach { player ->
      // 各プレイヤーにルームIDを通知
      player.send("""{"type": "roomId", "content": "${roomId.toString()}"}""")
    }
  }
}
