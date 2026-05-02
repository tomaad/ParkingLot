import { NavLink, Outlet, useLocation } from 'react-router-dom'

const navigationItems = [
  { to: '/', label: 'Dashboard', icon: '📊', description: 'Live lot summary' },
  { to: '/park', label: 'Park Vehicle', icon: '🅿️', description: 'Issue a parking ticket' },
  { to: '/unpark', label: 'Unpark Vehicle', icon: '🚗', description: 'Close a parking session' },
  { to: '/lookup', label: 'Vehicle Lookup', icon: '🔎', description: 'Find parked vehicles' },
  { to: '/availability', label: 'Availability', icon: '🏢', description: 'Per-level slot details' },
]

function Layout() {
  const location = useLocation()
  const activeItem =
    navigationItems.find((item) =>
      item.to === '/'
        ? location.pathname === '/'
        : location.pathname.startsWith(item.to),
    ) ?? navigationItems[0]

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar__brand">
          <div className="sidebar__brand-icon">🅿️</div>
          <div>
            <h1>Parking Lot</h1>
            <p>Management System</p>
          </div>
        </div>

        <nav className="sidebar__nav" aria-label="Main navigation">
          {navigationItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) =>
                `sidebar__link${isActive ? ' sidebar__link--active' : ''}`
              }
            >
              <span className="sidebar__icon" aria-hidden="true">
                {item.icon}
              </span>
              <span>
                <strong>{item.label}</strong>
                <br />
                <span className="sidebar__footer">{item.description}</span>
              </span>
            </NavLink>
          ))}
        </nav>

        <p className="sidebar__footer">
          Manage parking, monitor occupancy, and locate vehicles from one dashboard.
        </p>
      </aside>

      <main className="main-content">
        <div className="content-frame">
          <section className="page-shell">
            <header className="page-header">
              <div>
                <h2>{activeItem.label}</h2>
                <p>{activeItem.description}</p>
              </div>
              <div className="header-chip">Live API · localhost:8080</div>
            </header>
            <Outlet />
          </section>
        </div>
      </main>
    </div>
  )
}

export default Layout
