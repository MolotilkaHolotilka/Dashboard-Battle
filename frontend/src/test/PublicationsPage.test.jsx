import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import PublicationsPage from '../pages/PublicationsPage';
import client from '../api/client';

vi.mock('../api/client');

const mockPubs = [
  { publicationId: 77, channelId: 1, channelCode: 'TELEGRAM', destinationId: 5, status: 'PUBLISHED', externalId: 'tg-abc' },
  { publicationId: 78, channelId: 1, channelCode: 'TELEGRAM', destinationId: 5, status: 'RECALLED',  externalId: 'tg-xyz' },
];

describe('PublicationsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    localStorage.setItem('companySnapshots', JSON.stringify([{ id: 10, name: 'Тестовая компания' }]));
  });

  it('рендерит обе секции страницы', () => {
    render(<PublicationsPage />);
    expect(screen.getByText('Опубликовать отчёт')).toBeInTheDocument();
    expect(screen.getByText('История публикаций')).toBeInTheDocument();
  });

  it('список публикаций отображает строки и статус-бейджи', async () => {
    client.get = vi.fn().mockImplementation((url) => {
      if (url === '/publications') return Promise.resolve({ data: mockPubs });
      if (url === '/companies/10') return Promise.resolve({ data: { id: 10, name: 'Тестовая компания' } });
      if (url === '/publish/destinations') return Promise.resolve({ data: [] });
      return Promise.resolve({ data: [] });
    });

    render(<PublicationsPage />);
    fireEvent.change(screen.getByLabelText('Компания'), { target: { value: '10' } });
    fireEvent.click(screen.getByRole('button', { name: /загрузить/i }));

    await waitFor(() => expect(screen.getByText('tg-abc')).toBeInTheDocument());
    expect(screen.getByText('Опубликован')).toBeInTheDocument();
    expect(screen.getByText('Отозван')).toBeInTheDocument();
  });

  it('кнопка Отменить есть только у PUBLISHED публикаций', async () => {
    client.get = vi.fn().mockImplementation((url) => {
      if (url === '/publications') return Promise.resolve({ data: mockPubs });
      if (url === '/companies/10') return Promise.resolve({ data: { id: 10, name: 'Тестовая компания' } });
      if (url === '/publish/destinations') return Promise.resolve({ data: [] });
      return Promise.resolve({ data: [] });
    });

    render(<PublicationsPage />);
    fireEvent.change(screen.getByLabelText('Компания'), { target: { value: '10' } });
    fireEvent.click(screen.getByRole('button', { name: /загрузить/i }));

    await waitFor(() => expect(screen.getByText('tg-abc')).toBeInTheDocument());

    const cancelBtns = screen.getAllByRole('button', { name: /отменить/i });
    expect(cancelBtns).toHaveLength(1); // только для PUBLISHED
  });

  it('после отмены статус строки обновляется на RECALLED', async () => {
    client.get = vi.fn().mockImplementation((url) => {
      if (url === '/publications') return Promise.resolve({ data: mockPubs });
      if (url === '/companies/10') return Promise.resolve({ data: { id: 10, name: 'Тестовая компания' } });
      if (url === '/publish/destinations') return Promise.resolve({ data: [] });
      return Promise.resolve({ data: [] });
    });
    client.post = vi.fn().mockResolvedValue({
      data: { publicationId: 77, channelId: 1, channelCode: 'TELEGRAM', destinationId: 5, status: 'RECALLED', externalId: 'tg-abc' },
    });

    render(<PublicationsPage />);
    fireEvent.change(screen.getByLabelText('Компания'), { target: { value: '10' } });
    fireEvent.click(screen.getByRole('button', { name: /загрузить/i }));

    await waitFor(() => screen.getByRole('button', { name: /отменить/i }));
    fireEvent.click(screen.getByRole('button', { name: /отменить/i }));

    await waitFor(() => expect(screen.getAllByText('Отозван')).toHaveLength(2));
    expect(screen.queryByRole('button', { name: /отменить/i })).not.toBeInTheDocument();
  });

  it('пустой список показывает сообщение', async () => {
    client.get = vi.fn().mockImplementation((url) => {
      if (url === '/publications') return Promise.resolve({ data: [] });
      if (url === '/companies/10') return Promise.resolve({ data: { id: 10, name: 'Тестовая компания' } });
      if (url === '/publish/destinations') return Promise.resolve({ data: [] });
      return Promise.resolve({ data: [] });
    });

    render(<PublicationsPage />);
    fireEvent.change(screen.getByLabelText('Компания'), { target: { value: '10' } });
    fireEvent.click(screen.getByRole('button', { name: /загрузить/i }));

    await waitFor(() =>
      expect(screen.getByText('Публикаций пока нет')).toBeInTheDocument()
    );
  });
});
