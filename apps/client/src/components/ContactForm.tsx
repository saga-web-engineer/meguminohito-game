import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';

const schema = z.object({
  name: z.string().min(1, 'お名前を入力してください').max(30, '30文字以内で入力してください'),
  message: z.string().min(1, 'メッセージを入力してください'),
});

type FormValues = z.infer<typeof schema>;

export const ContactForm = () => {
  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
    trigger,
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    mode: 'onBlur',
  });

  const onSubmit = (data: FormValues) => alert(`お名前: ${data.name}\nメッセージ: ${data.message}`);

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      className="mx-auto mt-10 max-w-md space-y-4 rounded border p-4"
    >
      <div>
        <label htmlFor="name" className="block font-bold">
          お名前
        </label>
        <input
          id="name"
          type="text"
          {...register('name')}
          onBlur={() => trigger('name')}
          className="w-full rounded border p-2"
        />
        {errors.name && <p className="text-sm text-red-500">{errors.name.message}</p>}
      </div>
      <div>
        <label htmlFor="message" className="block font-bold">
          メッセージ
        </label>
        <textarea
          id="message"
          {...register('message')}
          onBlur={() => trigger('message')}
          className="w-full rounded border p-2"
          rows={4}
        />
        {errors.message && <p className="text-sm text-red-500">{errors.message.message}</p>}
      </div>
      <button
        type="submit"
        disabled={!isValid}
        className={`w-full rounded p-2 ${isValid ? 'bg-blue-500 text-white' : 'cursor-not-allowed bg-gray-300 text-gray-500'}`}
      >
        送信
      </button>
    </form>
  );
};
