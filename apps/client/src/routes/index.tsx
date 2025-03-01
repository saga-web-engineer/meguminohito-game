import { createFileRoute } from '@tanstack/react-router';

import { Counter } from '../components/counter/Counter';
import { MetaData } from '../components/MetaData';
import { SITE_DESCRIPTION, SITE_NAME, SITE_URL } from '../utils/siteSetting';

export const Route = createFileRoute('/')({
  component: Index,
});

function Index() {
  return (
    <>
      <MetaData title={SITE_NAME} description={SITE_DESCRIPTION} url={SITE_URL} />
      <Counter />
    </>
  );
}
