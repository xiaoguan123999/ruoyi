import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { UIProvider } from './theme'
import { Home } from './Home'
import { Detail } from './Detail'

export default function App() {
  return (
    <UIProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/app/:id" element={<Detail />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </UIProvider>
  )
}
