import { createFileRoute, useNavigate } from '@tanstack/react-router';
import { Loader2 } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';

import { MetaData } from '../../components/MetaData';
import { PORT, SITE_NAME, SITE_URL } from '../../utils/siteSetting';

export const Route = createFileRoute('/matching/')({
  component: RouteComponent,
});

function RouteComponent() {
  const title = `対戦相手を探しています | ${SITE_NAME}`;
  const description = '対戦相手を探しています';
  const url = `${SITE_URL}/matching`;

  const navigate = useNavigate();
  const [stateMessage, setStateMessage] = useState('対戦相手を探しています');
  const [playerCount, setPlayerCount] = useState(0);
  const socketRef = useRef<WebSocket | null>(null);

  useEffect(() => {
    // WebSocketが既に存在する場合は作成しない
    if (socketRef.current) return;

    const socket = new WebSocket(`ws://${PORT}/ws/matching`);
    socketRef.current = socket;

    socket.onopen = () => {
      console.log('[INFO] WebSocket connected');
      // 接続が確立された後にメッセージを送信
      socket.send(JSON.stringify({ type: 'matching' }));
    };

    socket.onmessage = (event) => {
      const data = JSON.parse(event.data);

      switch (data.type) {
        case 'player':
          console.log('[INFO] プレイヤー人数:', data.content);
          setPlayerCount(data.content);
          break;

        case 'roomId':
          console.log('[INFO] ルームID:', data.content);
          setStateMessage('ルームへ移動します');
          setTimeout(() => {
            navigate({ to: `/room/${data.content}` });
          }, 2000);
          break;

        default:
          break;
      }
    };

    socket.onerror = (error) => console.error('[ERROR] WebSocket error:', error);

    return () => {
      console.log('[INFO] WebSocket disconnected');
      if (socketRef.current) {
        socketRef.current.close();
        socketRef.current = null;
      }
    };
  }, [navigate]);

  return (
    <>
      <MetaData title={title} description={description} url={url} />
      <div className="flex h-full flex-col items-center justify-center gap-16">
        <div className="mt-auto">
          <h1 className="text-2xl">{stateMessage}</h1>
          <p className="mt-2 flex items-center justify-center">プレイヤー人数: {playerCount}</p>
        </div>
        <div className="relative mb-auto animate-spin border-transparent px-4 py-2 text-center text-2xl">
          め
          <Loader2 className="absolute inset-0 m-auto animate-spin" size={56} />
        </div>
      </div>
    </>
  );
}
