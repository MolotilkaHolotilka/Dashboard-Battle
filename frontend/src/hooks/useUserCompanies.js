import { useCallback, useEffect, useState } from 'react';
import client from '../api/client';
import { getCompanySnapshots, getLastUserId } from '../session';

function mergeCompanies(primary, secondary) {
  const merged = [];
  const seen = new Set();
  for (const item of [...primary, ...secondary]) {
    if (!item?.id || seen.has(item.id)) continue;
    seen.add(item.id);
    merged.push(item);
  }
  return merged;
}

export default function useUserCompanies() {
  const [companies, setCompanies] = useState(() => getCompanySnapshots(getLastUserId()));

  const refreshCompanies = useCallback(async () => {
    const userId = getLastUserId();
    if (!userId) {
      setCompanies([]);
      return;
    }
    try {
      const { data } = await client.get('/companies', { params: { userId } });
      const snapshots = getCompanySnapshots(userId);
      if (Array.isArray(data) && data.length > 0) {
        setCompanies(mergeCompanies(data, snapshots));
      } else {
        setCompanies(snapshots);
      }
    } catch {
      setCompanies(getCompanySnapshots(userId));
    }
  }, []);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      refreshCompanies();
    }, 0);

    return () => window.clearTimeout(timeoutId);
  }, [refreshCompanies]);

  return { companies, refreshCompanies };
}
