import { useCallback, useEffect, useState } from 'react';
import client from '../api/client';
import { getCompanySnapshots, getLastUserId } from '../session';

export default function useUserCompanies() {
  const [companies, setCompanies] = useState(() => getCompanySnapshots());

  const refreshCompanies = useCallback(async () => {
    const userId = getLastUserId();
    if (!userId) {
      setCompanies(getCompanySnapshots());
      return;
    }
    try {
      const { data } = await client.get('/companies', { params: { userId } });
      if (Array.isArray(data) && data.length > 0) {
        setCompanies(data);
      } else {
        setCompanies(getCompanySnapshots());
      }
    } catch {
      setCompanies(getCompanySnapshots());
    }
  }, []);

  useEffect(() => {
    refreshCompanies();
  }, [refreshCompanies]);

  return { companies, refreshCompanies };
}
