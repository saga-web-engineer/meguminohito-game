import { createFileRoute } from '@tanstack/react-router';
import { useEffect, useState } from 'react';

export const Route = createFileRoute('/room/$roomId/')({
  component: RouteComponent,
});

function RouteComponent() {
  const { roomId } = Route.useParams();
  const [ws, setWs] = useState<WebSocket | null>(null);
  const [inputValue, setInputValue] = useState(''); // 入力値を管理するステートを追加

  useEffect(() => {
    const socket = new WebSocket(`ws://127.0.0.1:8080/ws/${roomId}`);
    setWs(socket);

    socket.onmessage = (event) => {
      try {
        console.log(event);
      } catch {
        console.warn('Received non-JSON message:', event.data);
      }
    };

    return () => {
      console.log('[INFO] WebSocket disconnected');
      socket.close(); // WebSocketを閉じる
    };
  }, [roomId]); // roomIdが変わったときだけ再実行

  const handleSend = () => {
    if (ws) {
      ws.send(inputValue); // 入力値を送信
      setInputValue(''); // 入力値をリセット
    }
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
