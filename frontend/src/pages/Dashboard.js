import React, { useEffect, useState } from 'react';
import { Row, Col, Card, Spinner, Alert } from 'react-bootstrap';
import { useDispatch, useSelector } from 'react-redux';
import { fetchDashboardStats } from '../store/slices/dashboardSlice';
import DashboardStats from '../components/dashboard/DashboardStats';
import BalanceChart from '../components/dashboard/BalanceChart';
import CategoryPieChart from '../components/dashboard/CategoryPieChart';
import RecentAddresses from '../components/dashboard/RecentAddresses';
import RecentTransactions from '../components/dashboard/RecentTransactions';
import { motion } from 'framer-motion';

const Dashboard = () => {
  const dispatch = useDispatch();
  const { stats, isLoading, error } = useSelector((state) => state.dashboard);
  const [greeting, setGreeting] = useState('');

  useEffect(() => {
    dispatch(fetchDashboardStats());
    
    const hour = new Date().getHours();
    if (hour < 12) setGreeting('Good Morning');
    else if (hour < 18) setGreeting('Good Afternoon');
    else setGreeting('Good Evening');
  }, [dispatch]);

  if (isLoading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '400px' }}>
        <Spinner animation="border" variant="primary" />
      </div>
    );
  }

  if (error) {
    return (
      <Alert variant="danger">
        <Alert.Heading>Error Loading Dashboard</Alert.Heading>
        <p>{error}</p>
      </Alert>
    );
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
    >
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="mb-1">Dashboard</h2>
          <p className="text-muted mb-0">{greeting}, welcome back!</p>
        </div>
      </div>

      {/* Statistics Cards */}
      {stats && <DashboardStats stats={stats} />}

      {/* Charts Row */}
      <Row className="mb-4">
        <Col lg={6} className="mb-4 mb-lg-0">
          <Card className="h-100 shadow-sm">
            <Card.Header className="bg-white border-bottom-0 pt-4">
              <h5 className="mb-0">Balance Overview</h5>
              <small className="text-muted">Last 7 days</small>
            </Card.Header>
            <Card.Body>
              <BalanceChart />
            </Card.Body>
          </Card>
        </Col>
        <Col lg={6}>
          <Card className="h-100 shadow-sm">
            <Card.Header className="bg-white border-bottom-0 pt-4">
              <h5 className="mb-0">Address Distribution</h5>
              <small className="text-muted">By blockchain</small>
            </Card.Header>
            <Card.Body>
              <CategoryPieChart data={stats?.blockchainDistribution} />
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Recent Data Row */}
      <Row>
        <Col lg={6} className="mb-4 mb-lg-0">
          <Card className="shadow-sm">
            <Card.Header className="bg-white border-bottom-0 pt-4">
              <h5 className="mb-0">Recent Addresses</h5>
              <small className="text-muted">Recently added or updated</small>
            </Card.Header>
            <Card.Body>
              <RecentAddresses addresses={stats?.recentAddresses} />
            </Card.Body>
          </Card>
        </Col>
        <Col lg={6}>
          <Card className="shadow-sm">
            <Card.Header className="bg-white border-bottom-0 pt-4">
              <h5 className="mb-0">Recent Transactions</h5>
              <small className="text-muted">Latest blockchain activity</small>
            </Card.Header>
            <Card.Body>
              <RecentTransactions transactions={stats?.recentTransactions} />
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </motion.div>
  );
};

export default Dashboard;