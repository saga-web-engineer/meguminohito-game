import type { FC } from 'react';

import { useCounterStore } from '../../stores/counter';
import { CounterButton } from './CounterButton';

export const Counter: FC = () => {
  const count = useCounterStore((state) => state.count);

  return (
    <div className="py-10">
      <div className="flex items-center justify-center">カウント：{count}</div>
      <div className="mt-10 flex items-center justify-center gap-4">
        <CounterButton type="+" step={1} />
        <CounterButton type="-" step={1} />
      </div>
    </div>
  );
};
