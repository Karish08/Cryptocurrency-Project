import React from 'react';
import { Nav } from 'react-bootstrap';
import { Link, useLocation } from 'react-router-dom';
import { 
  FaTachometerAlt, 
  FaAddressBook, 
  FaTags, 
  FaChartPie, 
  FaCog 
} from 'react-icons/fa';
import './Sidebar.css';

const Sidebar = () => {
  const location = useLocation();

  const menuItems = [
    { path: '/dashboard', icon: FaTachometerAlt, label: 'Dashboard' },
    { path: '/addresses', icon: FaAddressBook, label: 'Addresses' },
    { path: '/categories', icon: FaTags, label: 'Categories' },
    { path: '/reports', icon: FaChartPie, label: 'Reports' },
    { path: '/settings', icon: FaCog, label: 'Settings' },
  ];

  return (
    <div className="sidebar">
      <Nav className="flex-column">
        {menuItems.map((item) => (
          <Nav.Link
            key={item.path}
            as={Link}
            to={item.path}
            active={location.pathname === item.path}
            className="sidebar-link"
          >
            <item.icon className="me-3" />
            {item.label}
          </Nav.Link>
        ))}
      </Nav>
    </div>
  );
};

export default Sidebar;