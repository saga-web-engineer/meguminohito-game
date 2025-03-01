import type { FC } from 'react';

interface Props {
  title: string;
  description: string;
  url: string;
}

export const MetaData: FC<Props> = ({ title, description, url }) => {
  return (
    <>
      <title>{title}</title>
      <meta name="title" content={title} />
      <meta property="og:title" content={title} />
      <meta property="twitter:title" content={title} />
      <meta name="description" content={description} />
      <meta property="og:description" content={description} />
      <meta property="twitter:description" content={description} />
      <meta property="og:url" content={url} />
      <meta property="twitter:url" content={url} />
    </>
  );
};
