import { afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';
import '@testing-library/jest-dom';

// This runs a cleanup function after each test case, which is good practice.
afterEach(() => {
  cleanup();
});