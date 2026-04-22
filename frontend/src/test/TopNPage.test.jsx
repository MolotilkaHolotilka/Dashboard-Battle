import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import TopNPage from '../pages/TopNPage';
import client from '../api/client';

vi.mock('../api/client');

const mockReport = {
  id: 50,
  companyId: 10,
  companyName: 'Тестовая компания',
  createdAt: '2026-04-15T10:00:00Z',
  status: 'PENDING',
  periodStart: '2026-02-01',
  periodEnd: '2026-03-01',
  entries: [
    { id: 1, rank: 1, employeeName: 'Иванов И.', revenue: 1500000, margin: 350000, favoriteProduct: 'Ноутбук' },
    { id: 2, rank: 2, employeeName: 'Петров П.', revenue: 1200000, margin: 280000, favoriteProduct: 'Мышь' },
  ],
};

describe('TopNPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    localStorage.setItem('lastUserId', '1');
    localStorage.setItem('companySnapshots', JSON.stringify([{ id: 10, name: 'Тестовая компания', userId: 1 }]));
    client.get = vi.fn().mockImplementation((url) => {
      if (url === '/reports/top-n') {
        return Promise.resolve({ data: [mockReport] });
      }
      if (url === '/companies') {
        return Promise.resolve({ data: [{ id: 10, name: 'Тестовая компания' }] });
      }
      return Promise.resolve({ data: [] });
    });
  });

  it('рендерит форму с полями компании и N', () => {
    render(<TopNPage />);
    expect(screen.getByText('Компания')).toBeInTheDocument();
    expect(screen.getByText('Количество (N)')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /запросить/i })).toBeInTheDocument();
  });

  it('автоматически показывает ранее сформированные отчёты по компании', async () => {
    render(<TopNPage />);
    fireEvent.change(screen.getByLabelText('Компания'), { target: { value: '10' } });

    await waitFor(() => expect(screen.getByText('Отчёт #50')).toBeInTheDocument());
    expect(screen.getByText('Иванов И.')).toBeInTheDocument();
  });

  it('после запроса показывает таблицу с сотрудниками', async () => {
    client.post = vi.fn().mockResolvedValue({ data: { ...mockReport, id: 51 } });

    render(<TopNPage />);
    fireEvent.change(screen.getByLabelText('Компания'), { target: { value: '10' } });
    fireEvent.click(screen.getByRole('button', { name: /запросить/i }));

    await waitFor(() => expect(screen.getByText('Иванов И.')).toBeInTheDocument());
    expect(screen.getByText('Петров П.')).toBeInTheDocument();
    expect(screen.getByText('Ноутбук')).toBeInTheDocument();
    expect(screen.getAllByRole('row')).toHaveLength(3);
    expect(screen.getByText('Отчёт #51')).toBeInTheDocument();
  });

  it('при статусе PENDING показывает кнопку подтвердить', async () => {
    client.post = vi.fn().mockResolvedValue({ data: mockReport });

    render(<TopNPage />);
    fireEvent.change(screen.getByLabelText('Компания'), { target: { value: '10' } });
    fireEvent.click(screen.getByRole('button', { name: /запросить/i }));

    await waitFor(() =>
      expect(screen.getByRole('button', { name: /подтвердить/i })).toBeInTheDocument()
    );
  });

  it('после подтверждения статус меняется на CONFIRMED', async () => {
    client.post = vi.fn()
      .mockResolvedValueOnce({ data: mockReport })
      .mockResolvedValueOnce({ data: { ...mockReport, status: 'CONFIRMED' } });

    render(<TopNPage />);
    fireEvent.change(screen.getByLabelText('Компания'), { target: { value: '10' } });
    fireEvent.click(screen.getByRole('button', { name: /запросить/i }));

    await waitFor(() =>
      expect(screen.getByRole('button', { name: /подтвердить/i })).toBeInTheDocument()
    );
    fireEvent.click(screen.getByRole('button', { name: /подтвердить/i }));

    await waitFor(() => expect(screen.getAllByText('Подтверждён').length).toBeGreaterThan(0));
    expect(screen.queryByRole('button', { name: /подтвердить/i })).not.toBeInTheDocument();
  });

  it('позволяет архивировать отчёт и убирает его из списка', async () => {
    client.post = vi.fn().mockResolvedValue({ data: { ...mockReport, status: 'ARCHIVED' } });

    render(<TopNPage />);
    fireEvent.change(screen.getByLabelText('Компания'), { target: { value: '10' } });

    await waitFor(() => expect(screen.getByText('Отчёт #50')).toBeInTheDocument());
    fireEvent.click(screen.getByRole('button', { name: /архивировать/i }));

    await waitFor(() =>
      expect(screen.getByText('По выбранной компании пока нет сформированных отчётов')).toBeInTheDocument()
    );
    expect(screen.queryByText('Отчёт #50')).not.toBeInTheDocument();
  });

  it('при ошибке 404 показывает сообщение', async () => {
    client.post = vi.fn().mockRejectedValue({
      response: { data: { message: 'Компания не найдена: 10' } },
    });

    render(<TopNPage />);
    fireEvent.change(screen.getByLabelText('Компания'), { target: { value: '10' } });
    fireEvent.click(screen.getByRole('button', { name: /запросить/i }));

    await waitFor(() =>
      expect(screen.getByText('Компания не найдена: 10')).toBeInTheDocument()
    );
  });
});
