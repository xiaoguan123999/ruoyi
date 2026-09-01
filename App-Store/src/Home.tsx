import { useMemo, useState } from 'react'
import { apps } from './data'
import { AppLine, Side } from './ui'
import { useUI } from './theme'

export function Home() {
  const { t } = useUI()
  const [q, setQ] = useState('')
  const [cat, setCat] = useState('all')

  const list = useMemo(() => {
    const word = q.trim().toLowerCase()
    return apps.filter((app) => {
      const hitCat = cat === 'all' || app.category === cat
      const blob = `${app.name.zh} ${app.name.en} ${app.desc.zh} ${app.desc.en}`.toLowerCase()
      return hitCat && (!word || blob.includes(word))
    })
  }, [q, cat])

  return (
    <div className="frame">
      <Side cat={cat} onCat={setCat} q={q} onQ={setQ} />
      <div className="stage">
        <section className="hero">
          <div className="hero-sky" aria-hidden>
            <span className="orb orb-earth" />
            <span className="orb orb-glow" />
            <span className="sat" />
          </div>
          <div className="hero-copy">
            <h1>{t.slogan}</h1>
            <p>{t.subSlogan}</p>
          </div>
        </section>

        <div className="notice">
          <span className="notice-tag">{t.noticeTag}</span>
          <div className="notice-mask">
            <p className="notice-run">{t.notice}</p>
          </div>
        </div>

        <div className="panel">
          {list.length === 0 && <p className="empty">{t.empty}</p>}
          <div className="list">
            {list.map((app) => (
              <AppLine key={app.id} app={app} />
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
