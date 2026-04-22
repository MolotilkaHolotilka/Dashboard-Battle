export function saveToken(token) {
  if (token) {
    localStorage.setItem('authToken', token);
  } else {
    localStorage.removeItem('authToken');
  }
}

export function getToken() {
  return localStorage.getItem('authToken') || null;
}

export function isAuthenticated() {
  return getToken() !== null;
}

export function clearAuth() {
  localStorage.removeItem('authToken');
  localStorage.removeItem('lastUserId');
  localStorage.removeItem('lastCompanyId');
  localStorage.removeItem('lastCompanyName');
}

export function getLastUserId() {
  const raw = localStorage.getItem('lastUserId');
  if (!raw) return null;
  const parsed = Number(raw);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

export function getLastCompanyId() {
  const raw = localStorage.getItem('lastCompanyId');
  if (!raw) return null;
  const parsed = Number(raw);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

export function isRegisteredLocally() {
  return getLastUserId() !== null || getLastCompanyId() !== null;
}

export function saveCompanySnapshot(company, userId = getLastUserId()) {
  if (!company?.id || !userId) return;
  const existing = getAllCompanySnapshots();
  const compact = existing.filter(item => !(item?.id === company.id && item?.userId === userId));
  compact.unshift({
    id: company.id,
    name: company.name ?? `Компания #${company.id}`,
    userId,
  });
  localStorage.setItem('companySnapshots', JSON.stringify(compact.slice(0, 30)));
}

export function getCompanySnapshots(userId = getLastUserId()) {
  if (!userId) return [];
  return getAllCompanySnapshots().filter(item => item.userId === userId);
}

function getAllCompanySnapshots() {
  const raw = localStorage.getItem('companySnapshots');
  if (!raw) return [];
  try {
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    return parsed.filter(item => item && item.id && item.userId);
  } catch {
    return [];
  }
}
