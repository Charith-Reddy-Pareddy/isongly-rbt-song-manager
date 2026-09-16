import { useState } from 'react';
import BrowsePanel from './components/BrowsePanel';
import SpecDemoPanel from './components/SpecDemoPanel';
import TrendingPanel from './components/TrendingPanel';
import './App.css';

const TABS = [
  { key: 'browse', label: 'Browse & Search' },
  { key: 'trending', label: 'Trending' },
  { key: 'demo', label: 'Original Assignment API' },
];

const PANELS = {
  browse: BrowsePanel,
  trending: TrendingPanel,
  demo: SpecDemoPanel,
};

export default function App() {
  const [tab, setTab] = useState('browse');
  const ActivePanel = PANELS[tab];

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

      <ActivePanel />
    </div>
  );
}
