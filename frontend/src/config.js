/** Разрешить страницу Danger Zone и отправку debug-заголовков (только вместе с ALLOW_DEBUG_HEADERS на сервере). */
export const dangerZoneEnabled = import.meta.env.VITE_ENABLE_DANGER_ZONE === 'true';
