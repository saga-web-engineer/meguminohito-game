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

  useEffect(() => {
    // WebSocketが既に存在する場合は作成しない
    if (socketRef.current) return;

    const socket = new WebSocket(`ws://${PORT}/ws/${roomId}`);
    socketRef.current = socket;

    socket.onmessage = (event) => {
      console.log('event.data', event.data);
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

  return (
    <>
      <form action="" onSubmit={(e) => e.preventDefault()}>
        {' '}
        {/* フォームのデフォルト動作を防止 */}
        <input
          type="text"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)} // 入力値を更新
        />
        <button type="button" onClick={handleSend}>
          送信
        </button>
      </form>
    </>
  );
}
