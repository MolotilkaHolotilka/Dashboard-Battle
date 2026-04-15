import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import RegisterPage from '../pages/RegisterPage';
import client from '../api/client';

vi.mock('../api/client');

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('RegisterPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    mockNavigate.mockReset();
  });

  it('рендерит форму с тремя полями и кнопкой', () => {
    render(<MemoryRouter><RegisterPage /></MemoryRouter>);
    expect(screen.getByPlaceholderText('user@example.com')).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/6 символов/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Ромашка/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /зарегистрироваться/i })).toBeInTheDocument();
  });

  it('при успехе редиректит на /integrations', async () => {
    client.post = vi.fn()
      .mockResolvedValueOnce({ data: { userId: 1, companyId: 10, email: 'a@b.ru', companyName: 'Тест' } })
      .mockResolvedValueOnce({ data: { token: 'jwt-token', userId: 1, companyId: 10, companyName: 'Тест' } });

    render(<MemoryRouter><RegisterPage /></MemoryRouter>);

    fireEvent.change(screen.getByPlaceholderText('user@example.com'), { target: { value: 'a@b.ru' } });
    fireEvent.change(screen.getByPlaceholderText(/6 символов/i), { target: { value: 'secret' } });
    fireEvent.change(screen.getByPlaceholderText(/Ромашка/i), { target: { value: 'Тест' } });
    fireEvent.click(screen.getByRole('button', { name: /зарегистрироваться/i }));

    await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/integrations'));
  });

  it('при ошибке 409 показывает сообщение из ответа', async () => {
    client.post = vi.fn().mockRejectedValue({
      response: { data: { message: 'Пользователь уже существует' } },
    });

    render(<MemoryRouter><RegisterPage /></MemoryRouter>);

    fireEvent.change(screen.getByPlaceholderText('user@example.com'), { target: { value: 'dup@test.ru' } });
    fireEvent.change(screen.getByPlaceholderText(/6 символов/i), { target: { value: 'secret' } });
    fireEvent.change(screen.getByPlaceholderText(/Ромашка/i), { target: { value: 'Компания' } });
    fireEvent.click(screen.getByRole('button', { name: /зарегистрироваться/i }));

    await waitFor(() =>
      expect(screen.getByText('Пользователь уже существует')).toBeInTheDocument()
    );
  });
});
