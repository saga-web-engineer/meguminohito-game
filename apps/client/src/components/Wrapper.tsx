import type { FC } from 'react';

export const Wrapper: FC<{ children: React.ReactNode }> = ({ children }) => {
  return <div className="mx-auto h-full w-[min(90%,1060px)]">{children}</div>;
};
