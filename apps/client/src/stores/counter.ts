import { create } from 'zustand';

interface State {
  count: number;
  adjustCount: (type: '+' | '-', step: number) => void;
}

export const useCounterStore = create<State>()((set) => ({
  count: 0,
  adjustCount: (type, step) => {
    set((state) => {
      switch (type) {
        case '+':
          return { count: state.count + step };
        case '-':
          return { count: state.count - step };
        default:
          throw new Error('無効なアクション');
      }
    });
  },
}));
