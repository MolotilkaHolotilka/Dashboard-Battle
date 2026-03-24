import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import TopNPage from '../pages/TopNPage';
import client from '../api/client';

vi.mock('../api/client');

const mockReport = {
  id: 50,
  companyId: 10,
  status: 'PENDING',
  periodStart: '2026-02-01',
  periodEnd: '2026-03-01',
  entries: [
    { id: 1, rank: 1, employeeName: 'Иванов И.', revenue: 1500000, margin: 350000, favoriteProduct: 'Ноутбук' },
    { id: 2, rank: 2, employeeName: 'Петров П.', revenue: 1200000, margin: 280000, favoriteProduct: 'Мышь' },
  ],
};

describe('TopNPage', () => {
  beforeEach(() => vi.clearAllMocks());

  it('рендерит форму с полями companyId и N', () => {
    render(<TopNPage />);
    expect(screen.getByText('ID компании')).toBeInTheDocument();
    expect(screen.getByText('Количество (N)')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /запросить/i })).toBeInTheDocument();
  });

  it('после запроса показывает таблицу с сотрудниками', async () => {
    client.post = vi.fn().mockResolvedValue({ data: mockReport });

    render(<TopNPage />);
    fireEvent.change(screen.getAllByRole('spinbutton')[0], { target: { value: '10' } });
    fireEvent.click(screen.getByRole('button', { name: /запросить/i }));

    await waitFor(() => expect(screen.getByText('Иванов И.')).toBeInTheDocument());
    expect(screen.getByText('Петров П.')).toBeInTheDocument();
    expect(screen.getByText('Ноутбук')).toBeInTheDocument();
  });

  it('при статусе PENDING показывает кнопку подтвердить', async () => {
    client.post = vi.fn().mockResolvedValue({ data: mockReport });

    render(<TopNPage />);
    fireEvent.change(screen.getAllByRole('spinbutton')[0], { target: { value: '10' } });
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
    fireEvent.change(screen.getAllByRole('spinbutton')[0], { target: { value: '10' } });
    fireEvent.click(screen.getByRole('button', { name: /запросить/i }));

    await waitFor(() =>
      expect(screen.getByRole('button', { name: /подтвердить/i })).toBeInTheDocument()
    );
    fireEvent.click(screen.getByRole('button', { name: /подтвердить/i }));

    await waitFor(() => expect(screen.getByText('Подтверждён')).toBeInTheDocument());
    expect(screen.queryByRole('button', { name: /подтвердить/i })).not.toBeInTheDocument();
  });

  it('при ошибке 404 показывает сообщение', async () => {
    client.post = vi.fn().mockRejectedValue({
      response: { data: { message: 'Компания не найдена: 999' } },
    });

    render(<TopNPage />);
    fireEvent.change(screen.getAllByRole('spinbutton')[0], { target: { value: '999' } });
    fireEvent.click(screen.getByRole('button', { name: /запросить/i }));

    await waitFor(() =>
      expect(screen.getByText('Компания не найдена: 999')).toBeInTheDocument()
    );
  });
});
