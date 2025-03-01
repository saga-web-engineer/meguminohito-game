import { Link } from '@tanstack/react-router';
import type { FC } from 'react';

interface Nav {
  path: string;
  label: string;
}

const NavData: Nav[] = [
  {
    path: '/',
    label: 'Home',
  },
  {
    path: '/about',
    label: 'About',
  },
  {
    path: '/contact',
    label: 'Contact',
  },
];

export const Nav: FC = () => {
  return (
    <nav>
      <ul className="flex items-center justify-center gap-4">
        {NavData.map((data) => (
          <li key={data.label}>
            <Link to={data.path} className="[&.active]:font-bold">
              {data.label}
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  );
};
