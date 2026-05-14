import React from 'react';
import { Form, Button, Row, Col } from 'react-bootstrap';
import { Formik } from 'formik';
import * as Yup from 'yup';
import { useDispatch } from 'react-redux';
import { createAddress, updateAddress } from '../../store/slices/addressSlice';

const addressSchema = Yup.object({
  address: Yup.string().required('Address is required'),
  blockchain: Yup.string().required('Blockchain is required'),
  label: Yup.string(),
  notes: Yup.string(),
  favorite: Yup.boolean(),
  categoryIds: Yup.array(),
});

const blockchains = [
  'ethereum',
  'bitcoin',
  'binance',
  'polygon',
  'solana',
  'avalanche',
  'arbitrum',
  'optimism',
];

const AddressForm = ({ address, categories, onSuccess, onCancel }) => {
  const dispatch = useDispatch();

  const initialValues = {
    address: address?.address || '',
    blockchain: address?.blockchain || 'ethereum',
    label: address?.label || '',
    notes: address?.notes || '',
    favorite: address?.favorite || false,
    categoryIds: address?.categoryIds || [],
  };

  const handleSubmit = async (values, { setSubmitting }) => {
    try {
      if (address) {
        await dispatch(updateAddress({ id: address.id, ...values }));
      } else {
        await dispatch(createAddress(values));
      }
      onSuccess();
    } catch (error) {
      console.error('Failed to save address:', error);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Formik
      initialValues={initialValues}
      validationSchema={addressSchema}
      onSubmit={handleSubmit}
    >
      {({
        handleSubmit,
        handleChange,
        handleBlur,
        values,
        touched,
        errors,
        isSubmitting,
        setFieldValue,
      }) => (
        <Form onSubmit={handleSubmit}>
          <Form.Group className="mb-3">
            <Form.Label>Address *</Form.Label>
            <Form.Control
              type="text"
              name="address"
              placeholder="Enter cryptocurrency address"
              value={values.address}
              onChange={handleChange}
              onBlur={handleBlur}
              isInvalid={touched.address && errors.address}
            />
            <Form.Control.Feedback type="invalid">
              {errors.address}
            </Form.Control.Feedback>
          </Form.Group>

          <Row>
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>Blockchain *</Form.Label>
                <Form.Select
                  name="blockchain"
                  value={values.blockchain}
                  onChange={handleChange}
                  isInvalid={touched.blockchain && errors.blockchain}
                >
                  {blockchains.map((chain) => (
                    <option key={chain} value={chain}>
                      {chain.charAt(0).toUpperCase() + chain.slice(1)}
                    </option>
                  ))}
                </Form.Select>
                <Form.Control.Feedback type="invalid">
                  {errors.blockchain}
                </Form.Control.Feedback>
              </Form.Group>
            </Col>
            <Col md={6}>
              <Form.Group className="mb-3">
                <Form.Label>Label</Form.Label>
                <Form.Control
                  type="text"
                  name="label"
                  placeholder="e.g., Main Wallet"
                  value={values.label}
                  onChange={handleChange}
                />
              </Form.Group>
            </Col>
          </Row>

          <Form.Group className="mb-3">
            <Form.Label>Categories</Form.Label>
            <div className="border rounded p-3">
              {categories.map((category) => (
                <Form.Check
                  key={category.id}
                  type="checkbox"
                  id={`cat-${category.id}`}
                  label={
                    <span>
                      <span
                        style={{
                          display: 'inline-block',
                          width: '12px',
                          height: '12px',
                          backgroundColor: category.color,
                          borderRadius: '3px',
                          marginRight: '5px',
                        }}
                      />
                      {category.name}
                    </span>
                  }
                  checked={values.categoryIds.includes(category.id)}
                  onChange={(e) => {
                    const newCategoryIds = e.target.checked
                      ? [...values.categoryIds, category.id]
                      : values.categoryIds.filter((id) => id !== category.id);
                    setFieldValue('categoryIds', newCategoryIds);
                  }}
                  className="mb-2"
                />
              ))}
            </div>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Notes</Form.Label>
            <Form.Control
              as="textarea"
              rows={3}
              name="notes"
              placeholder="Additional notes about this address"
              value={values.notes}
              onChange={handleChange}
            />
          </Form.Group>

          <Form.Group className="mb-4">
            <Form.Check
              type="checkbox"
              id="favorite"
              name="favorite"
              label="Mark as favorite"
              checked={values.favorite}
              onChange={handleChange}
            />
          </Form.Group>

          <div className="d-flex justify-content-end gap-2">
            <Button variant="secondary" onClick={onCancel}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Saving...' : address ? 'Update' : 'Create'}
            </Button>
          </div>
        </Form>
      )}
    </Formik>
  );
};

export default AddressForm;