import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import { ParentDemo } from './ParentDemo'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ParentDemo />
  </StrictMode>,
)
