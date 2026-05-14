import React, { useState, useEffect } from 'react';
import {
  Row,
  Col,
  Card,
  Table,
  Button,
  Badge,
  Form,
  InputGroup,
  Pagination,
  Modal,
} from 'react-bootstrap';
import { useDispatch, useSelector } from 'react-redux';
import {
  fetchAddresses,
  deleteAddress,
  refreshBalance,
  searchAddresses,
} from '../store/slices/addressSlice';
import { fetchCategories } from '../store/slices/categorySlice';
import { FaSearch, FaPlus, FaStar, FaRegStar, FaSync, FaTrash, FaEdit } from 'react-icons/fa';
import AddressForm from '../components/addresses/AddressForm';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';

const Addresses = () => {
  const dispatch = useDispatch();
  const { addresses, totalPages, currentPage, isLoading } = useSelector(
    (state) => state.addresses
  );
  const { categories } = useSelector((state) => state.categories);
  
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingAddress, setEditingAddress] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [page, setPage] = useState(0);

  useEffect(() => {
    dispatch(fetchAddresses({ page, size: 10 }));
    dispatch(fetchCategories());
  }, [dispatch, page]);

  const handleSearch = () => {
    if (searchTerm.trim()) {
      dispatch(searchAddresses({ query: searchTerm, page: 0, size: 10 }));
      setPage(0);
    } else {
      dispatch(fetchAddresses({ page: 0, size: 10 }));
    }
  };

  const handleDelete = (id) => {
    if (window.confirm('Are you sure you want to delete this address?')) {
      dispatch(deleteAddress(id));
    }
  };

  const handleRefresh = (id) => {
    dispatch(refreshBalance(id));
  };

  const handleEdit = (address) => {
    setEditingAddress(address);
    setShowAddModal(true);
  };

  const handleCloseModal = () => {
    setShowAddModal(false);
    setEditingAddress(null);
  };

  const paginationItems = [];
  for (let number = 0; number < totalPages; number++) {
    paginationItems.push(
      <Pagination.Item
        key={number}
        active={number === currentPage}
        onClick={() => setPage(number)}
      >
        {number + 1}
      </Pagination.Item>
    );
  }

  return (
    <div className="addresses">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>Addresses</h2>
        <Button variant="primary" onClick={() => setShowAddModal(true)}>
          <FaPlus className="me-2" /> Add Address
        </Button>
      </div>

      {/* Search and Filter */}
      <Card className="mb-4">
        <Card.Body>
          <Row>
            <Col md={6}>
              <InputGroup>
                <Form.Control
                  placeholder="Search by address, label, or notes..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                />
                <Button variant="outline-secondary" onClick={handleSearch}>
                  <FaSearch />
                </Button>
              </InputGroup>
            </Col>
            <Col md={4}>
              <Form.Select
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
              >
                <option value="">All Categories</option>
                {categories.map((cat) => (
                  <option key={cat.id} value={cat.id}>
                    {cat.name}
                  </option>
                ))}
              </Form.Select>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Addresses Table */}
      <Card>
        <Card.Body>
          <Table responsive hover>
            <thead>
              <tr>
                <th>Label</th>
                <th>Address</th>
                <th>Blockchain</th>
                <th>Balance (USD)</th>
                <th>Categories</th>
                <th>Favorite</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {addresses.map((address) => (
                <tr key={address.id}>
                  <td>
                    <Link to={`/addresses/${address.id}`}>
                      {address.label || 'Unnamed'}
                    </Link>
                  </td>
                  <td>
                    <small className="text-muted">
                      {address.address.substring(0, 10)}...
                      {address.address.substring(address.address.length - 8)}
                    </small>
                  </td>
                  <td>
                    <Badge bg="info">{address.blockchain}</Badge>
                  </td>
                  <td>${address.totalBalanceUsd?.toLocaleString() || '0'}</td>
                  <td>
                    {address.categoryIds?.map((catId) => {
                      const category = categories.find(c => c.id === catId);
                      return category ? (
                        <Badge
                          key={catId}
                          bg="secondary"
                          className="me-1"
                          style={{ backgroundColor: category.color }}
                        >
                          {category.name}
                        </Badge>
                      ) : null;
                    })}
                  </td>
                  <td>
                    {address.favorite ? (
                      <FaStar className="text-warning" />
                    ) : (
                      <FaRegStar className="text-secondary" />
                    )}
                  </td>
                  <td>
                    <Button
                      variant="link"
                      size="sm"
                      onClick={() => handleRefresh(address.id)}
                      title="Refresh Balance"
                    >
                      <FaSync className="text-primary" />
                    </Button>
                    <Button
                      variant="link"
                      size="sm"
                      onClick={() => handleEdit(address)}
                      title="Edit"
                    >
                      <FaEdit className="text-info" />
                    </Button>
                    <Button
                      variant="link"
                      size="sm"
                      onClick={() => handleDelete(address.id)}
                      title="Delete"
                    >
                      <FaTrash className="text-danger" />
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="d-flex justify-content-center mt-3">
              <Pagination>
                <Pagination.Prev
                  onClick={() => setPage(Math.max(0, currentPage - 1))}
                  disabled={currentPage === 0}
                />
                {paginationItems}
                <Pagination.Next
                  onClick={() => setPage(Math.min(totalPages - 1, currentPage + 1))}
                  disabled={currentPage === totalPages - 1}
                />
              </Pagination>
            </div>
          )}
        </Card.Body>
      </Card>

      {/* Add/Edit Modal */}
      <Modal show={showAddModal} onHide={handleCloseModal} size="lg">
        <Modal.Header closeButton>
          <Modal.Title>{editingAddress ? 'Edit Address' : 'Add New Address'}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <AddressForm
            address={editingAddress}
            categories={categories}
            onSuccess={handleCloseModal}
            onCancel={handleCloseModal}
          />
        </Modal.Body>
      </Modal>
    </div>
  );
};

export default Addresses;