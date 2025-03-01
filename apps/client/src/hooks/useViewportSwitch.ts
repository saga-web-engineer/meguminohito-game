import { useEffect } from 'react';

export const useViewportSwitch = () => {
  useEffect(() => {
    const switchViewport = () => {
      const viewport = document.querySelector<HTMLMetaElement>('meta[name="viewport"]');
      const value = window.outerWidth > 360 ? 'width=device-width,initial-scale=1' : 'width=360';
      if (viewport?.getAttribute('content') !== value) {
        viewport?.setAttribute('content', value);
      }
    };

    const handleResize = () => switchViewport();

    window.addEventListener('resize', handleResize);
    switchViewport();

    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []);
};
