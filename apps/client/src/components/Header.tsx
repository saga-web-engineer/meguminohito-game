import { Link, useMatchRoute } from '@tanstack/react-router';
import type { FC } from 'react';

import { MarkHiraganaMeh } from './MarkHiraganaMeh';
import { Wrapper } from './Wrapper';

export const Header: FC = () => {
  const matchRoute = useMatchRoute();
  const isHomePage = matchRoute({ to: '/' });

  return (
    <header className="border-b py-4">
      <Wrapper>
        <div className="flex items-center justify-between">
          <Link to="/" className="text-2xl">
            {isHomePage ? (
              <p className="text-2xl">
                <MarkHiraganaMeh />
                組のひとゲーム
              </p>
            ) : (
              <h1 className="text-2xl">
                <MarkHiraganaMeh />
                組のひとゲーム
              </h1>
            )}
          </Link>
          <button className="cursor-pointer rounded border px-4 py-1">遊び方</button>
        </div>
      </Wrapper>
    </header>
  );
};
