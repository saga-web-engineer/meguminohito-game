import { createFileRoute, useNavigate } from '@tanstack/react-router';
import { Loader2 } from 'lucide-react';
import { useEffect, useState } from 'react';

import { MetaData } from '../../components/MetaData';
import { SITE_NAME, SITE_URL } from '../../utils/siteSetting';

export const Route = createFileRoute('/matching/')({
  component: RouteComponent,
});

function RouteComponent() {
  const title = `ルームに接続中 | ${SITE_NAME}`;
  const description = 'ルームに接続中です';
  const url = `${SITE_URL}/matching`;
  const [message, setMessage] = useState<string>('対戦相手を探しています');
  const navigate = useNavigate(); // tanstack routerのnavigateを使用

  useEffect(() => {
    const socket = new WebSocket(`ws://127.0.0.1:8080/ws/matching`);

    socket.onopen = () => {
      console.log('[INFO] WebSocket connected');
      socket.send(JSON.stringify({ type: 'matching' })); // 接続が確立された後にメッセージを送信
    };

    socket.onmessage = (event) => {
      const message = event.data;
      console.log('event', event);
      console.log('[INFO] Received message:', message);

      if (message.startsWith('ルームに移動します: ')) {
        const roomId = message.replace('ルームに移動します: ', '');
        navigate({ to: `/room/${roomId}` }); // tanstack routerのnavigateを使用してリダイレクト
      } else {
        setMessage('対戦相手が見つかりました');
      }
    };

    // クリーンアップ処理を追加
    return () => {
      console.log('[INFO] WebSocket disconnected');
      socket.close(); // WebSocketを閉じる
    };
  }, []);

  return (
    <>
      <MetaData title={title} description={description} url={url} />
      <div className="flex h-full flex-col items-center justify-center gap-16">
        <h1 className="mt-auto text-2xl">{message}</h1>
        <div className="relative mb-auto animate-spin border-transparent px-4 py-2 text-center text-2xl">
          め
          <Loader2 className="absolute inset-0 m-auto animate-spin" size={56} />
        </div>
      </div>
    </>
  );
}
