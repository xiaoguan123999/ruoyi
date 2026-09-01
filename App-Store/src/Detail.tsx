import { Link, useNavigate, useParams } from 'react-router-dom'
import { getApp } from './data'
import { BrandMark, Icon } from './ui'
import { useUI } from './theme'

function AndroidMark() {
  return (
    <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden>
      <path
        fill="currentColor"
        d="M17.6 9.5c.9 0 1.6.7 1.6 1.6v6.3c0 .9-.7 1.6-1.6 1.6s-1.6-.7-1.6-1.6v-6.3c0-.9.7-1.6 1.6-1.6zm-11.2 0c.9 0 1.6.7 1.6 1.6v6.3c0 .9-.7 1.6-1.6 1.6S4.8 18.3 4.8 17.4v-6.3c0-.9.7-1.6 1.6-1.6zM7.3 8.1c0-2.6 2.1-4.7 4.7-4.7s4.7 2.1 4.7 4.7H7.3zM8.2 19.1h7.6c.7 0 1.3-.6 1.3-1.3V9.3H6.9v8.5c0 .7.6 1.3 1.3 1.3z"
      />
    </svg>
  )
}

function AppleMark() {
  return (
    <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden>
      <path
        fill="currentColor"
        d="M16.7 12.6c0-2.3 1.9-3.4 2-3.5-1.1-1.6-2.8-1.8-3.4-1.8-1.4-.1-2.8.9-3.5.9s-1.8-.8-3-.8c-1.5 0-3 .9-3.8 2.3-1.6 2.8-.4 7 1.2 9.3.8 1.1 1.7 2.3 2.9 2.3 1.2 0 1.6-.7 3-.7s1.8.7 3 .7 2-.1 2.9-2.3c.7-1.1 1-2.2 1-2.2s-1.9-.7-1.9-2.9zm-1.8-5.3c.6-.8 1.1-1.8 1-2.9-1 .1-2.1.7-2.8 1.5-.6.7-1.2 1.8-1 2.8 1.1.1 2.1-.5 2.8-1.4z"
      />
    </svg>
  )
}

function BackMark() {
  return (
    <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden>
      <path
        fill="none"
        stroke="currentColor"
        strokeWidth="2.2"
        strokeLinecap="round"
        strokeLinejoin="round"
        d="M15 5 8 12l7 7"
      />
    </svg>
  )
}

export function Detail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { lang, t } = useUI()
  const app = getApp(id ?? '')

  const goBack = () => {
    if (window.history.length > 1) navigate(-1)
    else navigate('/')
  }

  if (!app) {
    return (
      <div className="dl">
        <header className="dl-bar">
          <button type="button" className="dl-back" onClick={goBack}>
            <BackMark />
            {t.back}
          </button>
          <span>{t.downloadTitle}</span>
        </header>
        <p className="empty">{t.empty}</p>
        <Link to="/">{t.back}</Link>
      </div>
    )
  }

  return (
    <div className="dl">
      <header className="dl-bar">
        <button type="button" className="dl-back" onClick={goBack}>
          <BackMark />
          {t.back}
        </button>
        <span>{t.downloadTitle}</span>
      </header>

      <div className="dl-main">
        <Icon app={app} size={92} />
        <h1>{app.name[lang]}</h1>
        <p className="dl-desc">{app.desc[lang]}</p>

        <div className="dl-actions">
          {app.links.android && (
            <a className="dl-btn dl-android" href={app.links.android} target="_blank" rel="noreferrer">
              <AndroidMark />
              {t.androidGet}
            </a>
          )}
          {app.links.ios && (
            <a className="dl-btn dl-ios" href={app.links.ios} target="_blank" rel="noreferrer">
              <AppleMark />
              {t.iosGet}
            </a>
          )}
          {app.links.web && (
            <a className="dl-web" href={app.links.web} target="_blank" rel="noreferrer">
              {t.webGet}
            </a>
          )}
        </div>
      </div>

      <p className="dl-foot">
        <BrandMark size={22} />
        {t.brand}
      </p>
    </div>
  )
}
