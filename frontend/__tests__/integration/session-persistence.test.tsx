import { render, screen, waitFor } from '@testing-library/react';
import { AuthProvider, useAuth } from '../../components/AuthContext';
// import { createBrowserClient } from '@supabase/ssr'; // No longer directly used here for mocking
import { Session } from '@supabase/supabase-js';
import { useRouter } from 'next/navigation';
import React from 'react';

// Mock the Supabase client creation indirectly
const mockGetSession = jest.fn();
const mockSupabaseClient = {
  auth: {
    getSession: mockGetSession,
    signInWithPassword: jest.fn(), // Include if AuthProvider uses it
    signOut: jest.fn(), // Include if AuthProvider uses it
  },
};
jest.mock('@supabase/ssr', () => ({
  createBrowserClient: jest.fn(() => mockSupabaseClient),
}));


// Mock next/navigation
jest.mock('next/navigation', () => ({
  useRouter: jest.fn(),
}));

// const mockSupabaseGetSession = (createBrowserClient as jest.Mock)().auth.getSession; // This line is no longer needed
const mockPush = jest.fn();

// A test component to consume the AuthContext
const TestConsumer = () => {
  const { user, isLoading, session } = useAuth();

  if (isLoading) {
    return <div>Loading auth...</div>;
  }

  return (
    <div>
      {session ? (
        <span data-testid="user-email">{user?.email}</span>
      ) : (
        <span data-testid="no-session">No session</span>
      )}
    </div>
  );
};

describe('Session Persistence Integration', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (useRouter as jest.Mock).mockReturnValue({
      push: mockPush,
    });
  });

  it('should re-establish session automatically on page load if valid session exists', async () => {
    const mockSession: Session = {
      access_token: 'mock-access-token',
      refresh_token: 'mock-refresh-token',
      expires_in: 3600,
      expires_at: Date.now() / 1000 + 3600,
      token_type: 'Bearer',
      user: {
        id: 'user-id',
        aud: 'authenticated',
        role: 'authenticated',
        email: 'persistent@example.com',
        email_confirmed_at: '2025-01-01T00:00:00Z',
        phone: '',
        confirmed_at: '2025-01-01T00:00:00Z',
        last_sign_in_at: '2025-01-01T00:00:00Z',
        app_metadata: { provider: 'email' },
        user_metadata: {},
        created_at: '2025-01-01T00:00:00Z',
        updated_at: '2025-01-01T00:00:00Z',
        identities: [],
      },
    };

    mockGetSession.mockResolvedValueOnce({ data: { session: mockSession }, error: null });

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    expect(screen.getByText('Loading auth...')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByTestId('user-email')).toHaveTextContent('persistent@example.com');
    });
    expect(mockGetSession).toHaveBeenCalledTimes(1);
    expect(mockPush).not.toHaveBeenCalled(); // Should not redirect if session is found
  });

  it('should not re-establish session and show no session if no valid session exists', async () => {
    mockGetSession.mockResolvedValueOnce({ data: { session: null }, error: null });

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    expect(screen.getByText('Loading auth...')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByTestId('no-session')).toHaveTextContent('No session');
    });
    expect(mockGetSession).toHaveBeenCalledTimes(1);
    expect(mockPush).not.toHaveBeenCalledWith('/login'); // Redirection to login is handled by components if not logged in
  });

  it('should log error if getSession fails', async () => {
    const mockError = new Error('Network error');
    mockGetSession.mockResolvedValueOnce({ data: { session: null }, error: mockError });
    const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('no-session')).toBeInTheDocument();
    });
    expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching session:', mockError);
    consoleErrorSpy.mockRestore();
  });
});
