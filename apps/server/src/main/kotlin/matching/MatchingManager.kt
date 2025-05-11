package com.example.matching

import io.ktor.websocket.* // WebSocketセッションを扱うためのインポート
import kotlinx.coroutines.sync.Mutex // 同期処理を行うためのミューテックス
import kotlinx.coroutines.sync.withLock // ミューテックスを使ったロック処理
import kotlinx.coroutines.delay // 一定時間待機するための関数
import kotlinx.coroutines.Job // コルーチンのジョブ管理
import kotlinx.coroutines.CoroutineScope // コルーチンスコープ
import kotlinx.coroutines.Dispatchers // ディスパッチャー指定
import kotlinx.coroutines.launch // コルーチン起動

class MatchingManager {
  // 待機中プレイヤーのセット（重複防止）
  private val waitingPlayers = mutableSetOf<WebSocketSession>()
  // 排他制御用ミューテックス
  private val mutex = Mutex()
  private var timerJob: Job? = null
  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  suspend fun addPlayer(session: WebSocketSession) {
    println("[DEBUG] addPlayer() セッション: $session")
    // 最初のプレイヤーかどうか
    var isFirstPlayer = false
    // 即時開始フラグ
    var shouldStartImmediately = false

    mutex.withLock {
      // 既に参加済み or 上限超過なら何もしない
      if (!waitingPlayers.add(session) || waitingPlayers.size > MAX_PLAYERS) return
      logWaitingPlayers()
      notifyPlayerCount()
      isFirstPlayer = waitingPlayers.size == 1
      shouldStartImmediately = waitingPlayers.size == MAX_PLAYERS
    }

    when {
      shouldStartImmediately -> {
        timerJob?.cancel()
        timerJob = null
        startGameWithLock()
      }

      isFirstPlayer -> {
        timerJob?.cancel()
        timerJob = coroutineScope.launch {
          delay(WAIT_TIME_MS)
          println("[DEBUG] タイマーが終了。待機プレイヤー: ${waitingPlayers.map { it.hashCode() }}")
          println("[INFO] タイマーが終了。待機プレイヤー数: ${waitingPlayers.size}")
          startGameWithLock()
        }
      }
    }
  }

  suspend fun removePlayer(session: WebSocketSession) {
    mutex.withLock {
      if (waitingPlayers.remove(session)) {
        logWaitingPlayers()
        notifyPlayerCount()
      }
    }
  }

  private suspend fun startGameWithLock() = mutex.withLock {
    // 待機プレイヤーがいれば最大人数分ピックアップしてゲーム開始
    if (waitingPlayers.isNotEmpty()) {
      // 最大人数分取得
      val playersToStart = waitingPlayers.take(MAX_PLAYERS)
      // セットから削除
      waitingPlayers.removeAll(playersToStart.toSet())
      startGame(playersToStart)
    }
  }

  private suspend fun notifyPlayerCount() {
    val count = waitingPlayers.size.toString()
    waitingPlayers.forEach { player ->
      player.send("""{"type": "player", "content": "$count"}""")
    }
  }

  private fun logWaitingPlayers() {
    println("[DEBUG] 現在の待機プレイヤー: ${waitingPlayers.map { it.hashCode() }}")
    println("[INFO] 現在の待機プレイヤー数: ${waitingPlayers.size}")
  }

  private suspend fun startGame(players: List<WebSocketSession>) {
    val roomId = System.currentTimeMillis()
    println("[INFO] ルーム $roomId が作成されました。プレイヤー: ${players.map { it.hashCode() }}")
    players.forEach { player ->
      player.send("""{"type": "roomId", "content": "$roomId"}""")
    }
  }

  companion object {
    // 1ルームの最大人数
    private const val MAX_PLAYERS = 5
    // タイマー待機時間（ミリ秒）
    private const val WAIT_TIME_MS = 10_000L
  }
}
