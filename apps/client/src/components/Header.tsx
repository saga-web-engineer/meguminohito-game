import type { FC } from 'react';

import { Nav } from './Nav';
import { Wrapper } from './Wrapper';

export const Header: FC = () => {
  return (
    <header className="border-b py-4">
      <Wrapper>
        <div className="flex items-center justify-between">
          <h1 className="text-2xl">Template React</h1>
          <Nav />
        </div>
      </Wrapper>
    </header>
  );
};
