import { createFileRoute } from '@tanstack/react-router';

import { ContactForm } from '../../components/ContactForm';

export const Route = createFileRoute('/contact/')({
  component: Contact,
});

function Contact() {
  return <ContactForm />;
}
