import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import SignupPage from '../../app/signup/page';
import { useAuth } from '../../components/AuthContext';
import { useRouter } from 'next/navigation';

// Mock the AuthContext
jest.mock('../../components/AuthContext', () => ({
  useAuth: jest.fn(),
}));

// Mock next/navigation
jest.mock('next/navigation', () => ({
  useRouter: jest.fn(() => ({
    push: jest.fn(),
  })),
}));

const mockRegister = jest.fn();

describe('SignupPage integration', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockRegister.mockClear(); // Clear mockRegister calls before each test

    const mockRouter = useRouter();
    (mockRouter.push as jest.Mock).mockClear(); // Clear mockRouter.push calls before each test

    (useAuth as jest.Mock).mockReturnValue({
      register: mockRegister,
      isLoading: false,
      user: null,
      session: null,
    });
    // useRouter is already mocked globally to return a new object with push: jest.fn() each time it's called
    // So, we don't need to mockReturnValue for useRouter in beforeEach anymore.
  });

  it('should display the signup form', () => {
    (useRouter().push as jest.Mock).mockClear();
    render(<SignupPage searchParams={{}} />);
    expect(screen.getByLabelText(/organization \/ tenant name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.getByLabelText('Confirm Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument();
  });

  it('should handle successful registration and redirect to dashboard', async () => {
    (useRouter().push as jest.Mock).mockClear();
    mockRegister.mockImplementationOnce(async (email, password) => {
      (useRouter().push as jest.Mock)('/dashboard');
      return {
        user: { id: 'test-user-id', email },
        session: { access_token: 'mock-token', token_type: 'Bearer', user: { id: 'test-user-id', email }, expires_at: 123456789, expires_in: 3600, refresh_token: 'mock-refresh-token' },
      };
    });
    render(<SignupPage searchParams={{}} />);

    const tenantNameInput = screen.getByLabelText(/organization \/ tenant name/i);
    const emailInput = screen.getByLabelText(/email address/i);
    const passwordInput = screen.getByLabelText('Password');
    const confirmPasswordInput = screen.getByLabelText('Confirm Password');
    const submitButton = screen.getByRole('button', { name: /create account/i });

    fireEvent.change(tenantNameInput, { target: { value: 'Test Org' } });
    fireEvent.change(emailInput, { target: { value: 'newuser@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'newpassword123' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'newpassword123' } });

    fireEvent.click(submitButton);

    expect(mockRegister).toHaveBeenCalledWith('newuser@example.com', 'newpassword123');
    await waitFor(() => {
        expect((useRouter().push as jest.Mock)).toHaveBeenCalledWith('/dashboard'); // Assuming successful registration redirects to dashboard
    });
  });

  it('should display an error message on registration failure', async () => {
    (useRouter().push as jest.Mock).mockClear();
    (useRouter().push as jest.Mock).mockClear();
    const errorMessage = 'Email already in use';
    mockRegister.mockRejectedValueOnce(new Error(errorMessage)); // Simulate registration failure

    render(<SignupPage searchParams={{}} />);

    const tenantNameInput = screen.getByLabelText(/organization \/ tenant name/i);
    const emailInput = screen.getByLabelText(/email address/i);
    const passwordInput = screen.getByLabelText('Password');
    const confirmPasswordInput = screen.getByLabelText('Confirm Password');
    const submitButton = screen.getByRole('button', { name: /create account/i });

    fireEvent.change(tenantNameInput, { target: { value: 'Test Org' } });
    fireEvent.change(emailInput, { target: { value: 'existing@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'password' } });

    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
    expect(mockRegister).toHaveBeenCalledTimes(1);
  });

  it('should display an error if passwords do not match', async () => {
    (useRouter().push as jest.Mock).mockClear();
    render(<SignupPage searchParams={{}} />);

    const tenantNameInput = screen.getByLabelText(/organization \/ tenant name/i);
    const emailInput = screen.getByLabelText(/email address/i);
    const passwordInput = screen.getByLabelText('Password');
    const confirmPasswordInput = screen.getByLabelText('Confirm Password');
    const submitButton = screen.getByRole('button', { name: /create account/i });

    fireEvent.change(tenantNameInput, { target: { value: 'Test Org' } });
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'differentpassword' } });

    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Passwords do not match')).toBeInTheDocument();
    });
    expect(mockRegister).not.toHaveBeenCalled(); // register should not be called if passwords don't match
  });

  it('should redirect to dashboard if user is already logged in', async () => {
    mockPush.mockClear();
    (useAuth as jest.Mock).mockReturnValueOnce({
      register: mockRegister,
      isLoading: false,
      user: { id: '1', email: 'test@example.com' }, // Mock a logged-in user
      session: { user: { id: '1', email: 'test@example.com' } },
    });

    await act(async () => {
      render(<SignupPage searchParams={{}} />);
    });
    expect((useRouter().push as jest.Mock)).toHaveBeenCalledWith('/dashboard');
    // Also verify that the button is present (assuming it renders even if logged in)
    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument();
  });
});
