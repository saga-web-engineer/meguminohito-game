import { createFileRoute } from '@tanstack/react-router';
import { useEffect, useRef, useState } from 'react';

import { PORT } from '../../../utils/siteSetting';

export const Route = createFileRoute('/room/$roomId/')({
  component: RouteComponent,
});

function RouteComponent() {
  const { roomId } = Route.useParams();
  const [inputValue, setInputValue] = useState(''); // 入力値を管理するステートを追加
  const socketRef = useRef<WebSocket | null>(null);
  const [question, setQuestion] = useState(''); // 質問を管理するステートを追加
  const [isReady, setIsReady] = useState(false); // 追加: 準備状態
  const [gameStarted, setGameStarted] = useState(false); // 追加: ゲーム開始状態

  useEffect(() => {
    // WebSocketが既に存在する場合は作成しない
    if (socketRef.current) return;

    const socket = new WebSocket(`ws://${PORT}/ws/${roomId}`);
    socketRef.current = socket;

    socket.onmessage = (event) => {
      const data = JSON.parse(event.data);
      console.log('event.data', data);
      if (data.type === 'question') {
        setQuestion(data.content);
      } else if (data.type === 'start') {
        setGameStarted(true); // ゲーム開始
      }
    };

    return () => {
      console.log('[INFO] WebSocket disconnected');
      socket.close(); // WebSocketを閉じる
    };
  }, [roomId]); // roomIdが変わったときだけ再実行

  const handleSend = () => {
    // if (ws) {
    //   ws.send(inputValue); // 入力値を送信
    //   setInputValue(''); // 入力値をリセット
    // }
  };

  const handleReady = () => {
    if (socketRef.current) {
      socketRef.current.send(JSON.stringify({ type: 'ready', roomId }));
      setIsReady(true);
    }
  };

  return (
    <>
      {!gameStarted ? (
        <div>
          <button type="button" onClick={handleReady} disabled={isReady}>
            {isReady ? '準備完了' : '準備する'}
          </button>
          {isReady && <p>他の参加者の準備を待っています...</p>}
        </div>
      ) : (
        <form action="" onSubmit={(e) => e.preventDefault()}>
          {/* フォームのデフォルト動作を防止 */}
          <input
            type="text"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)} // 入力値を更新
          />
          <button type="button" onClick={handleSend}>
            送信
          </button>
          <div>{question}</div>
        </form>
      )}
    </>
  );
}
