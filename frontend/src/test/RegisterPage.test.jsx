import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import RegisterPage from '../pages/RegisterPage';
import client from '../api/client';

vi.mock('../api/client');

describe('RegisterPage', () => {
  beforeEach(() => vi.clearAllMocks());

  it('рендерит форму с тремя полями и кнопкой', () => {
    render(<RegisterPage />);
    expect(screen.getByPlaceholderText('user@example.com')).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/6 символов/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Ромашка/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /зарегистрироваться/i })).toBeInTheDocument();
  });

  it('при успехе показывает userId и companyId', async () => {
    client.post = vi.fn().mockResolvedValue({
      data: { userId: 1, companyId: 10, email: 'a@b.ru', companyName: 'Тест' },
    });

    render(<RegisterPage />);

    fireEvent.change(screen.getByPlaceholderText('user@example.com'), {
      target: { value: 'a@b.ru' },
    });
    fireEvent.change(screen.getByPlaceholderText(/6 символов/i), {
      target: { value: 'secret' },
    });
    fireEvent.change(screen.getByPlaceholderText(/Ромашка/i), {
      target: { value: 'Тест' },
    });
    fireEvent.click(screen.getByRole('button', { name: /зарегистрироваться/i }));

    await waitFor(() => expect(screen.getByText('Готово!')).toBeInTheDocument());
    expect(screen.getByText('1')).toBeInTheDocument();  // userId
    expect(screen.getByText('10')).toBeInTheDocument(); // companyId
  });

  it('при ошибке 409 показывает сообщение из ответа', async () => {
    client.post = vi.fn().mockRejectedValue({
      response: { data: { message: 'Пользователь уже существует' } },
    });

    render(<RegisterPage />);

    fireEvent.change(screen.getByPlaceholderText('user@example.com'), {
      target: { value: 'dup@test.ru' },
    });
    fireEvent.change(screen.getByPlaceholderText(/6 символов/i), {
      target: { value: 'secret' },
    });
    fireEvent.change(screen.getByPlaceholderText(/Ромашка/i), {
      target: { value: 'Компания' },
    });
    fireEvent.click(screen.getByRole('button', { name: /зарегистрироваться/i }));

    await waitFor(() =>
      expect(screen.getByText('Пользователь уже существует')).toBeInTheDocument()
    );
  });
});
