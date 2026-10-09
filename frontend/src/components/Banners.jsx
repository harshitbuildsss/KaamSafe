export function ErrorBanner({ t }) {
  return <div className="banner err"><b>✕ {t.noRoute}</b><span>{t.noRouteMsg}</span></div>
}
export function CachedBanner({ t }) {
  return <div className="banner warn"><b>⚠ {t.liveDown}</b><span>{t.liveDownMsg}</span><em>{t.useCached}</em></div>
}
