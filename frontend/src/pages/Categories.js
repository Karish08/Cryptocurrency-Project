import React, { useState, useEffect } from 'react';
import {
  Row,
  Col,
  Card,
  Button,
  Table,
  Modal,
  Form,
  Badge,
} from 'react-bootstrap';
import { useDispatch, useSelector } from 'react-redux';
import {
  fetchCategories,
  createCategory,
  updateCategory,
  deleteCategory,
} from '../store/slices/categorySlice';
import { FaPlus, FaEdit, FaTrash } from 'react-icons/fa';
import { Formik } from 'formik';
import * as Yup from 'yup';

const categorySchema = Yup.object({
  name: Yup.string().required('Category name is required'),
  color: Yup.string().required('Color is required'),
  description: Yup.string(),
});

const Categories = () => {
  const dispatch = useDispatch();
  const { categories, isLoading } = useSelector((state) => state.categories);
  
  const [showModal, setShowModal] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);

  useEffect(() => {
    dispatch(fetchCategories());
  }, [dispatch]);

  const handleEdit = (category) => {
    setEditingCategory(category);
    setShowModal(true);
  };

  const handleDelete = (id) => {
    if (window.confirm('Are you sure you want to delete this category?')) {
      dispatch(deleteCategory(id));
    }
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingCategory(null);
  };

  const handleSubmit = (values, { resetForm }) => {
    if (editingCategory) {
      dispatch(updateCategory({ id: editingCategory.id, ...values }));
    } else {
      dispatch(createCategory(values));
    }
    handleCloseModal();
    resetForm();
  };

  const colors = [
    '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEEAD',
    '#D4A5A5', '#9B59B6', '#3498DB', '#E67E22', '#2ECC71',
    '#F1C40F', '#E74C3C', '#1ABC9C', '#34495E', '#7F8C8D',
  ];

  return (
    <div className="categories">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>Categories</h2>
        <Button variant="primary" onClick={() => setShowModal(true)}>
          <FaPlus className="me-2" /> New Category
        </Button>
      </div>

      <Row>
        {categories.map((category) => (
          <Col md={4} key={category.id} className="mb-4">
            <Card>
              <Card.Body>
                <div className="d-flex align-items-center mb-3">
                  <div
                    style={{
                      width: '30px',
                      height: '30px',
                      backgroundColor: category.color,
                      borderRadius: '5px',
                      marginRight: '10px',
                    }}
                  />
                  <h5 className="mb-0">{category.name}</h5>
                </div>
                <p className="text-muted small mb-3">
                  {category.description || 'No description'}
                </p>
                <div className="d-flex justify-content-end">
                  <Button
                    variant="link"
                    size="sm"
                    onClick={() => handleEdit(category)}
                  >
                    <FaEdit className="text-info" />
                  </Button>
                  <Button
                    variant="link"
                    size="sm"
                    onClick={() => handleDelete(category.id)}
                  >
                    <FaTrash className="text-danger" />
                  </Button>
                </div>
              </Card.Body>
            </Card>
          </Col>
        ))}
      </Row>

      {/* Category Modal */}
      <Modal show={showModal} onHide={handleCloseModal}>
        <Modal.Header closeButton>
          <Modal.Title>
            {editingCategory ? 'Edit Category' : 'Create Category'}
          </Modal.Title>
        </Modal.Header>
        <Formik
          initialValues={{
            name: editingCategory?.name || '',
            color: editingCategory?.color || '#6366F1',
            description: editingCategory?.description || '',
          }}
          validationSchema={categorySchema}
          onSubmit={handleSubmit}
          enableReinitialize
        >
          {({ handleSubmit, handleChange, values, touched, errors }) => (
            <Form onSubmit={handleSubmit}>
              <Modal.Body>
                <Form.Group className="mb-3">
                  <Form.Label>Name *</Form.Label>
                  <Form.Control
                    type="text"
                    name="name"
                    value={values.name}
                    onChange={handleChange}
                    isInvalid={touched.name && errors.name}
                  />
                  <Form.Control.Feedback type="invalid">
                    {errors.name}
                  </Form.Control.Feedback>
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Color</Form.Label>
                  <div className="d-flex flex-wrap gap-2 mb-2">
                    {colors.map((color) => (
                      <div
                        key={color}
                        onClick={() => handleChange({ target: { name: 'color', value: color } })}
                        style={{
                          width: '30px',
                          height: '30px',
                          backgroundColor: color,
                          borderRadius: '5px',
                          cursor: 'pointer',
                          border: values.color === color ? '3px solid #000' : 'none',
                        }}
                      />
                    ))}
                  </div>
                  <Form.Control
                    type="color"
                    name="color"
                    value={values.color}
                    onChange={handleChange}
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Description</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={3}
                    name="description"
                    value={values.description}
                    onChange={handleChange}
                  />
                </Form.Group>
              </Modal.Body>
              <Modal.Footer>
                <Button variant="secondary" onClick={handleCloseModal}>
                  Cancel
                </Button>
                <Button variant="primary" type="submit">
                  {editingCategory ? 'Update' : 'Create'}
                </Button>
              </Modal.Footer>
            </Form>
          )}
        </Formik>
      </Modal>
    </div>
  );
};

export default Categories;