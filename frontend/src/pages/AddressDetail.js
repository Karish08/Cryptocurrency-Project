import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Row,
  Col,
  Card,
  Badge,
  Button,
  Table,
  Spinner,
  Alert,
} from 'react-bootstrap';
import { useDispatch, useSelector } from 'react-redux';
import axios from 'axios';
import {
  FaArrowLeft,
  FaStar,
  FaRegStar,
  FaSync,
  FaTrash,
  FaEdit,
  FaQrcode,
} from 'react-icons/fa';
import QRCode from 'qrcode.react';
import { refreshBalance, deleteAddress } from '../store/slices/addressSlice';
import { toast } from 'react-toastify';

const AddressDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { categories } = useSelector((state) => state.categories);
  
  const [address, setAddress] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [balances, setBalances] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showQR, setShowQR] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchAddressDetails();
    fetchTransactions();
  }, [id]);

  const fetchAddressDetails = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`http://localhost:8080/api/addresses/${id}`);
      setAddress(response.data);
      setError(null);
    } catch (err) {
      setError('Failed to load address details');
      toast.error('Failed to load address details');
    } finally {
      setLoading(false);
    }
  };

  const fetchTransactions = async () => {
    try {
      // This would be implemented based on your blockchain API
      // For demo, using mock data
      setTransactions([
        {
          hash: '0x1234...5678',
          from: '0xabcd...efgh',
          to: '0x1234...5678',
          value: '1.5',
          token: 'ETH',
          timestamp: new Date().toISOString(),
          status: 'confirmed',
        },
        // Add more mock transactions
      ]);
    } catch (err) {
      console.error('Failed to fetch transactions:', err);
    }
  };

  const handleRefresh = () => {
    dispatch(refreshBalance(id)).then(() => {
      fetchAddressDetails();
    });
  };

  const handleDelete = () => {
    if (window.confirm('Are you sure you want to delete this address?')) {
      dispatch(deleteAddress(id)).then(() => {
        navigate('/addresses');
      });
    }
  };

  if (loading) {
    return (
      <div className="text-center py-5">
        <Spinner animation="border" variant="primary" />
        <p className="mt-3">Loading address details...</p>
      </div>
    );
  }

  if (error || !address) {
    return (
      <Alert variant="danger">
        <Alert.Heading>Error</Alert.Heading>
        <p>{error || 'Address not found'}</p>
        <Button variant="outline-danger" onClick={() => navigate('/addresses')}>
          Back to Addresses
        </Button>
      </Alert>
    );
  }

  return (
    <div className="address-detail">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <Button variant="link" onClick={() => navigate('/addresses')} className="p-0">
          <FaArrowLeft className="me-2" /> Back to Addresses
        </Button>
        <div>
          <Button variant="outline-primary" className="me-2" onClick={handleRefresh}>
            <FaSync className="me-2" /> Refresh
          </Button>
          <Button variant="outline-info" className="me-2" onClick={() => setShowQR(!showQR)}>
            <FaQrcode className="me-2" /> QR Code
          </Button>
          <Button variant="outline-warning" className="me-2">
            <FaEdit className="me-2" /> Edit
          </Button>
          <Button variant="outline-danger" onClick={handleDelete}>
            <FaTrash className="me-2" /> Delete
          </Button>
        </div>
      </div>

      <Row>
        <Col md={8}>
          <Card className="mb-4">
            <Card.Header>
              <h5 className="mb-0">Address Information</h5>
            </Card.Header>
            <Card.Body>
              <Row>
                <Col md={6}>
                  <p>
                    <strong>Label:</strong> {address.label || 'Unnamed'}
                  </p>
                  <p>
                    <strong>Blockchain:</strong>{' '}
                    <Badge bg="info">{address.blockchain}</Badge>
                  </p>
                  <p>
                    <strong>Favorite:</strong>{' '}
                    {address.favorite ? (
                      <FaStar className="text-warning" />
                    ) : (
                      <FaRegStar className="text-secondary" />
                    )}
                  </p>
                </Col>
                <Col md={6}>
                  <p>
                    <strong>Total Balance (USD):</strong>{' '}
                    <span className="h5 text-success">
                      ${address.totalBalanceUsd?.toLocaleString() || '0'}
                    </span>
                  </p>
                  <p>
                    <strong>Categories:</strong>
                  </p>
                  <div>
                    {address.categoryIds?.map((catId) => {
                      const category = categories.find(c => c.id === catId);
                      return category ? (
                        <Badge
                          key={catId}
                          className="me-2"
                          style={{
                            backgroundColor: category.color,
                            color: '#fff',
                            padding: '5px 10px',
                          }}
                        >
                          {category.name}
                        </Badge>
                      ) : null;
                    })}
                  </div>
                </Col>
              </Row>
              <hr />
              <p>
                <strong>Address:</strong>
              </p>
              <code className="d-block p-3 bg-light rounded">
                {address.address}
              </code>
              {address.notes && (
                <>
                  <hr />
                  <p>
                    <strong>Notes:</strong>
                  </p>
                  <p className="text-muted">{address.notes}</p>
                </>
              )}
            </Card.Body>
          </Card>

          {/* Transactions */}
          <Card>
            <Card.Header>
              <h5 className="mb-0">Recent Transactions</h5>
            </Card.Header>
            <Card.Body>
              <Table responsive hover>
                <thead>
                  <tr>
                    <th>Hash</th>
                    <th>From/To</th>
                    <th>Value</th>
                    <th>Time</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {transactions.map((tx, index) => (
                    <tr key={index}>
                      <td>
                        <small>{tx.hash}</small>
                      </td>
                      <td>
                        <small>
                          {tx.from === address.address ? (
                            <span className="text-danger">Outgoing</span>
                          ) : (
                            <span className="text-success">Incoming</span>
                          )}
                        </small>
                      </td>
                      <td>
                        {tx.value} {tx.token}
                      </td>
                      <td>
                        <small>{new Date(tx.timestamp).toLocaleString()}</small>
                      </td>
                      <td>
                        <Badge bg="success">{tx.status}</Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </Card.Body>
          </Card>
        </Col>

        <Col md={4}>
          {showQR && (
            <Card className="mb-4">
              <Card.Header>
                <h5 className="mb-0">QR Code</h5>
              </Card.Header>
              <Card.Body className="text-center">
                <QRCode value={address.address} size={200} />
                <Button
                  variant="outline-primary"
                  className="mt-3"
                  onClick={() => {
                    navigator.clipboard.writeText(address.address);
                    toast.success('Address copied to clipboard');
                  }}
                >
                  Copy Address
                </Button>
              </Card.Body>
            </Card>
          )}

          {/* Token Balances */}
          <Card>
            <Card.Header>
              <h5 className="mb-0">Token Balances</h5>
            </Card.Header>
            <Card.Body>
              <Table responsive>
                <thead>
                  <tr>
                    <th>Token</th>
                    <th>Balance</th>
                    <th>Value (USD)</th>
                  </tr>
                </thead>
                <tbody>
                  {balances.map((balance, index) => (
                    <tr key={index}>
                      <td>
                        <strong>{balance.token}</strong>
                      </td>
                      <td>{balance.balance}</td>
                      <td>${balance.valueUsd}</td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default AddressDetail;