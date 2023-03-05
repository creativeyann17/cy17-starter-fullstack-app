import React from 'react'
import { render, screen } from '@testing-library/react'
import DefaultLayout from './layouts/DefaultLayout'

test('renders learn react link', () => {
  render(<DefaultLayout />)
  const linkElement = screen.getByText('Loading ...')
  expect(linkElement).toBeInTheDocument()
})
