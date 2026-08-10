import React from 'react';
import { render, screen } from '@testing-library/react';
import App from './App';

test('renders the login route for signed-out users', () => {
  localStorage.clear();
  render(<App />);
  expect(screen.getByRole('heading', { name: /login/i })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
});
