import { createFileRoute } from '@tanstack/react-router';
import { Loader2 } from 'lucide-react';

import { MetaData } from '../../components/MetaData';
import { SITE_NAME, SITE_URL } from '../../utils/siteSetting';

export const Route = createFileRoute('/matching/')({
  component: RouteComponent,
});

function RouteComponent() {
  const title = `ルームに接続中 | ${SITE_NAME}`;
  const description = 'ルームに接続中です';
  const url = `${SITE_URL}/matching`;

  return (
    <>
      <MetaData title={title} description={description} url={url} />
      <div className="flex h-full flex-col items-center justify-center gap-16">
        <h1 className="mt-auto text-4xl">ルームに接続中です</h1>
        <div className="relative mb-auto animate-spin border-transparent px-4 py-2 text-center text-2xl">
          め
          <Loader2 className="absolute inset-0 m-auto animate-spin" size={56} />
        </div>
      </div>
    </>
  );
}
