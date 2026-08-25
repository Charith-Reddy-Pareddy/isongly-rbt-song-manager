import { useState } from 'react';
import BrowsePanel from './components/BrowsePanel';
import SpecDemoPanel from './components/SpecDemoPanel';
import './App.css';

const TABS = [
  { key: 'browse', label: 'Browse & Search' },
  { key: 'demo', label: 'Original Assignment API' },
];

export default function App() {
  const [tab, setTab] = useState('browse');

  return (
    <div className="app">
      <header className="app-header">
        <h1>iSongly</h1>
        <p className="subtitle">
          A song library backed by a hand-built Red-Black Tree, served over a Spring Boot REST API.
        </p>
        <nav className="tabs">
          {TABS.map((t) => (
            <button
              key={t.key}
              type="button"
              className={`tab${tab === t.key ? ' active' : ''}`}
              onClick={() => setTab(t.key)}
            >
              {t.label}
            </button>
          ))}
        </nav>
      </header>

      {tab === 'browse' ? <BrowsePanel /> : <SpecDemoPanel />}
    </div>
  );
}
