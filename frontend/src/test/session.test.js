import { beforeEach, describe, expect, it } from 'vitest';
import {
  getCompanySnapshots,
  getLastUserId,
  saveCompanySnapshot,
} from '../session';

describe('session company snapshots', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('stores companies separately for different users', () => {
    localStorage.setItem('lastUserId', '1');
    expect(getLastUserId()).toBe(1);
    saveCompanySnapshot({ id: 10, name: 'ООО Гусь' }, 1);

    localStorage.setItem('lastUserId', '2');
    saveCompanySnapshot({ id: 20, name: 'ООО Тест' }, 2);

    expect(getCompanySnapshots(1)).toEqual([
      { id: 10, name: 'ООО Гусь', userId: 1 },
    ]);
    expect(getCompanySnapshots(2)).toEqual([
      { id: 20, name: 'ООО Тест', userId: 2 },
    ]);
  });

  it('ignores legacy snapshots without user binding', () => {
    localStorage.setItem('companySnapshots', JSON.stringify([
      { id: 10, name: 'Старая компания' },
      { id: 20, name: 'Новая компания', userId: 7 },
    ]));

    expect(getCompanySnapshots(7)).toEqual([
      { id: 20, name: 'Новая компания', userId: 7 },
    ]);
  });
});
