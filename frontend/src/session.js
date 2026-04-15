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

export function saveCompanySnapshot(company) {
  if (!company?.id) return;
  const existing = getCompanySnapshots();
  const compact = existing.filter(item => item.id !== company.id);
  compact.unshift({ id: company.id, name: company.name ?? `Компания #${company.id}` });
  localStorage.setItem('companySnapshots', JSON.stringify(compact.slice(0, 30)));
}

export function getCompanySnapshots() {
  const raw = localStorage.getItem('companySnapshots');
  if (!raw) return [];
  try {
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    return parsed.filter(item => item && item.id);
  } catch {
    return [];
  }
}
