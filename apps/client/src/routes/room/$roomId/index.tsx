import { createFileRoute } from '@tanstack/react-router';
import { useEffect, useRef, useState } from 'react';

import { PORT } from '../../../utils/siteSetting';

export const Route = createFileRoute('/room/$roomId/')({
  component: RouteComponent,
});

function RouteComponent() {
  const { roomId } = Route.useParams();
  const socketRef = useRef<WebSocket | null>(null);
  const [question, setQuestion] = useState('');

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
      }
    };

    return () => {
      console.log('[INFO] WebSocket disconnected');
      if (socketRef.current) {
        socketRef.current.close();
        socketRef.current = null;
      }
    };
  }, [roomId]);

  return (
    <>
      <div>{question}</div>
    </>
  );
}
