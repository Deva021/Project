import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import LoginPage from '../../../app/login/page';
import { useAuth } from '../../../components/AuthContext';
// import { createBrowserClient } from '@supabase/ssr'; // No longer directly used here for mocking

// Mock the AuthContext
jest.mock('../../../components/AuthContext', () => ({
  useAuth: jest.fn(),
}));

// Mock next/navigation
jest.mock('next/navigation', () => ({
  useRouter: jest.fn(),
}));

// Mock the Supabase client creation indirectly
const mockGetSession = jest.fn();
const mockSignInWithPassword = jest.fn();
const mockSignOut = jest.fn(); // Also mock signOut if AuthContext uses it
const mockSupabaseClient = {
  auth: {
    getSession: mockGetSession,
    signInWithPassword: mockSignInWithPassword,
    signOut: mockSignOut,
  },
};
jest.mock('@supabase/ssr', () => ({
  createBrowserClient: jest.fn(() => mockSupabaseClient),
}));

const mockLogin = jest.fn();
const mockPush = jest.fn();

describe('LoginPage integration', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (useAuth as jest.Mock).mockReturnValue({
      login: mockLogin,
      isLoading: false,
      user: null,
      session: null,
    });
    (useRouter as jest.Mock).mockReturnValue({
      push: mockPush,
      // Mock other router methods if needed
    });
    // Ensure getSession is mocked to return no session by default
    mockGetSession.mockResolvedValue({ data: { session: null }, error: null });
  });

  it('should display the login form', () => {
    render(<LoginPage />);
    expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('should handle successful login and redirect to dashboard', async () => {
    render(<LoginPage />);

    const emailInput = screen.getByLabelText(/email address/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.click(submitButton);

    expect(mockLogin).toHaveBeenCalledWith('test@example.com', 'password123');
  });

  it('should display an error message on failed login', async () => {
    const errorMessage = 'Invalid credentials';
    mockLogin.mockRejectedValueOnce(new Error(errorMessage)); // Simulate login failure

    render(<LoginPage />);

    const emailInput = screen.getByLabelText(/email address/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'wrongpassword' } });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
    expect(mockLogin).toHaveBeenCalledTimes(1);
  });

  it('should redirect to dashboard if user is already logged in', () => {
    (useAuth as jest.Mock).mockReturnValueOnce({
      login: mockLogin,
      isLoading: false,
      user: { id: '1', email: 'test@example.com' }, // Mock a logged-in user
      session: { user: { id: '1', email: 'test@example.com' } },
    });

    render(<LoginPage />);
    expect(mockPush).toHaveBeenCalledWith('/dashboard');
  });
});
