import type { FC } from 'react';

import { Wrapper } from './Wrapper';

export const Footer: FC = () => {
  return (
    <footer className="border-t py-4">
      <Wrapper>
        <p className="text-center">
          <small>&copy; 〇〇〇 2023 - {new Date().getFullYear()}</small>
        </p>
      </Wrapper>
    </footer>
  );
};
