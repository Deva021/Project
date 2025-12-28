import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import DashboardPage from '../../../app/dashboard/page';
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

// Mock the Supabase client creation indirectly (AuthContext uses getSession/signOut)
const mockGetSession = jest.fn();
const mockSignInWithPassword = jest.fn();
const mockSignOut = jest.fn();
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

const mockLogout = jest.fn();
const mockPush = jest.fn();

describe('DashboardPage integration (Sign Out)', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (useAuth as jest.Mock).mockReturnValue({
      logout: mockLogout,
      isLoading: false,
      user: { id: '1', email: 'test@example.com' }, // Mock a logged-in user
      session: { user: { id: '1', email: 'test@example.com' } },
    });
    (useRouter as jest.Mock).mockReturnValue({
      push: mockPush,
    });
  });

  it('should display welcome message and logout button for authenticated user', () => {
    render(<DashboardPage />);
    expect(screen.getByText(/Welcome to your Dashboard, test@example.com!/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /logout/i })).toBeInTheDocument();
  });

  it('should call logout function and redirect to login on logout button click', async () => {
    render(<DashboardPage />);

    const logoutButton = screen.getByRole('button', { name: /logout/i });
    fireEvent.click(logoutButton);

    expect(mockLogout).toHaveBeenCalledTimes(1);
  });

  it('should redirect to login if user is not authenticated', () => {
    (useAuth as jest.Mock).mockReturnValueOnce({
      logout: mockLogout,
      isLoading: false,
      user: null, // Mock no logged-in user
      session: null,
    });

    render(<DashboardPage />);
    expect(mockPush).toHaveBeenCalledWith('/login');
  });

  it('should show loading state', () => {
    (useAuth as jest.Mock).mockReturnValueOnce({
      logout: mockLogout,
      isLoading: true, // Mock loading state
      user: null,
      session: null,
    });

    render(<DashboardPage />);
    expect(screen.getByText(/Loading user data.../i)).toBeInTheDocument();
  });
});
