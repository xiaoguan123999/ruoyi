import { useId, useState } from 'react'
import { Link } from 'react-router-dom'
import { categoryLabel, categoryOrder, type AppItem } from './data'
import { useUI } from './theme'

export function BrandMark({ size = 32 }: { size?: number }) {
  const uid = useId().replace(/:/g, '')
  return (
    <svg className="brand-mark" width={size} height={size} viewBox="0 0 64 64" aria-hidden>
      <defs>
        <linearGradient id={uid} x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor="#4db3ff" />
          <stop offset="100%" stopColor="#0b62e8" />
        </linearGradient>
      </defs>
      <circle cx="32" cy="32" r="32" fill={`url(#${uid})`} />
      <path fill="#fff" d="M20 47c1.2-16.5 12.5-29 29-35.2C43.4 22 41.2 33 43.2 47H20z" />
      <path fill="none" stroke="#fff" strokeWidth="2.4" strokeLinecap="round" d="M17.5 47h28" />
    </svg>
  )
}

export function Icon({ app, size }: { app: AppItem; size?: number }) {
  const [broken, setBroken] = useState(false)
  const box = size ? { width: size, height: size } : undefined
  if (broken) {
    return (
      <div
        className="app-icon"
        style={{ ...box, background: app.color, fontSize: size ? size * 0.34 : undefined }}
      >
        {app.mark}
      </div>
    )
  }
  return <img className="app-icon" src={app.icon ?? `/logos/${app.id}.png`} alt="" style={box} onError={() => setBroken(true)} />
}

export function AppLine({ app }: { app: AppItem }) {
  const { lang, t } = useUI()
  return (
    <Link to={`/app/${app.id}`} className="line">
      <span className="icon-clip">
        <Icon app={app} />
      </span>
      <div className="line-copy">
        <strong>{app.name[lang]}</strong>
        <p>{app.desc[lang]}</p>
      </div>
      <span className="get-mini">{t.get}</span>
    </Link>
  )
}

export function Side({
  cat,
  onCat,
  q,
  onQ,
}: {
  cat: string
  onCat: (id: string) => void
  q: string
  onQ: (value: string) => void
}) {
  const { lang, t, toggleLang } = useUI()
  return (
    <aside className="rail">
      <div className="rail-top">
        <Link to="/" className="brand" aria-label={t.brand}>
          <BrandMark size={28} />
          <span className="brand-name">
            <strong>{t.brand}</strong>
            <small>{t.store}</small>
          </span>
        </Link>
        <button type="button" className="icon-btn" onClick={toggleLang}>
          {lang === 'zh' ? 'EN' : '中'}
        </button>
      </div>
      <label className="find">
        <svg className="find-ico" viewBox="0 0 24 24" width="16" height="16" aria-hidden>
          <circle cx="11" cy="11" r="6.5" fill="none" stroke="currentColor" strokeWidth="1.8" />
          <path d="M16.2 16.2 20 20" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
        </svg>
        <input
          type="search"
          value={q}
          placeholder={t.search}
          enterKeyHint="search"
          onChange={(e) => onQ(e.target.value)}
        />
      </label>
      <nav>
        <button type="button" className={cat === 'all' ? 'is-on' : ''} onClick={() => onCat('all')}>
          {t.all}
        </button>
        {categoryOrder.map((id) => (
          <button key={id} type="button" className={cat === id ? 'is-on' : ''} onClick={() => onCat(id)}>
            {categoryLabel[id][lang]}
          </button>
        ))}
      </nav>
    </aside>
  )
}
