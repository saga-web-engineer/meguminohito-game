import { createFileRoute, Link } from '@tanstack/react-router';

import { MarkHiraganaMeh } from '../components/MarkHiraganaMeh';
import { MetaData } from '../components/MetaData';
import { SITE_DESCRIPTION, SITE_NAME, SITE_URL } from '../utils/siteSetting';

export const Route = createFileRoute('/')({
  component: Index,
});

function Index() {
  return (
    <>
      <MetaData title={SITE_NAME} description={SITE_DESCRIPTION} url={SITE_URL} />
      <div className="flex h-full flex-col items-center justify-center gap-16">
        <h1 className="mt-auto text-4xl">
          <MarkHiraganaMeh />
          組のひとゲーム
        </h1>
        <Link to="/matching" className="mb-auto rounded border px-4 py-2">
          ゲームをはじめる
        </Link>
      </div>
    </>
  );
}
