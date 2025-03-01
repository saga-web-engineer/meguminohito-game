import type { FC } from 'react';

import { useCounterStore } from '../../stores/counter';

interface Props {
  type: '+' | '-';
  step: number;
}

export const CounterButton: FC<Props> = ({ type, step }) => {
  const adjustCount = useCounterStore((state) => state.adjustCount);

  return (
    <button
      className="flex w-10 items-center justify-center gap-1 rounded border hover:cursor-pointer"
      onClick={() => adjustCount(type, step)}
    >
      {type}
      {step}
    </button>
  );
};
