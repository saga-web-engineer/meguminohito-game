import { createFileRoute } from '@tanstack/react-router';
import { useEffect, useState } from 'react';

export const Route = createFileRoute('/room/$roomId/')({
  component: RouteComponent,
});

function RouteComponent() {
  const { roomId } = Route.useParams();
  const [ws, setWs] = useState<WebSocket | null>(null);
  useEffect(() => {
    const socket = new WebSocket(`ws://127.0.0.1:8080/ws/${roomId}`);
    setWs(socket);

    socket.onmessage = (event) => {
      // ここは久米さんが書く
      try {
        // const msg = JSON.parse(event.data);
        // if (msg.type === 'requestName') {
        //   setIsNameRequested(true); // 名前入力をリクエストされた状態にする
        // } else if (msg.type === 'userList') {
        //   setUsers(msg.users); // 参加者リストを更新
        // } else if (msg.type === 'message') {
        //   console.log(msg);
        //   setMessages((prev) => [
        //     ...prev,
        //     { id: msg.id, name: msg.name, text: msg.text, likes: 0 },
        //   ]);
        // } else if (msg.type === 'like') {
        //   setMessages((prev) =>
        //     prev.map((message) =>
        //       message.id === msg.messageId ? { ...message, likes: msg.likes } : message,
        //     ),
        //   );
        // } else if (msg.type === 'image') {
        //   setMessages((prev) => [
        //     ...prev,
        //     {
        //       id: msg.id,
        //       name: msg.name,
        //       type: msg.type,
        //       text: msg.text,
        //       imageData: msg.imageData,
        //       likes: 0,
        //     },
        //   ]);
        // }
        console.log(event);
      } catch {
        console.warn('Received non-JSON message:', event.data);
      }
    };

    return () => socket.close();
  }, []);

  return (
    <>
      <form action="">
        <input type="text" />
        <button type="button" onClick={() => ws?.send('undhi')}>
          送信
        </button>
      </form>
    </>
  );
}
