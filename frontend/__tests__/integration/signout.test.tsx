import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import DashboardPage from '../../app/dashboard/page';
import { useAuth } from '../../components/AuthContext';
import { useRouter } from 'next/navigation';
import apiClient from '../../lib/api'; // Import apiClient if ConversationList uses it

// Mock the apiClient
jest.mock('../../lib/api');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

// Mock the AuthContext
jest.mock('../../components/AuthContext', () => ({
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

// Mock ConversationList and ChatWindow
jest.mock('../../components/ConversationList', () => {
  import React from 'react';
  return function DummyConversationList({ agentId, onSelectConversation }: { agentId: string; onSelectConversation: (id: string) => void }) {
    // Simulate a conversation being selected
    React.useEffect(() => {
      onSelectConversation('mock-conv-1');
    }, [onSelectConversation]);
    return <div data-testid="conversation-list">Conversations for {agentId}</div>;
  };
});
jest.mock('../../components/ChatWindow', () => {
  return function DummyChatWindow({ tenantId, conversationId }: { tenantId: string; conversationId?: string }) {
    return <div data-testid="chat-window">Chatting in {conversationId} for {tenantId}</div>;
  };
});

const mockLogout = jest.fn();
const mockPush = jest.fn();

describe('DashboardPage integration (Sign Out)', () => {
  const mockUser = { id: '1', email: 'test@example.com' };
  const mockSession = { user: mockUser, access_token: 'mock-token' };

  beforeEach(() => {
    jest.clearAllMocks();
    mockLogout.mockImplementation(async () => {
      await mockSupabaseClient.auth.signOut();
      mockPush('/login');
    });
    (useAuth as jest.Mock).mockReturnValue({
      logout: mockLogout,
      isLoading: false,
      user: mockUser,
      session: mockSession,
    });
    (useRouter as jest.Mock).mockReturnValue({
      push: mockPush,
    });
    mockGetSession.mockResolvedValue({ data: { session: mockSession }, error: null });
    
    // Mock apiClient methods if ConversationList or ChatWindow directly use them
    // For now, only mocking getSession through createBrowserClient mock.
    // If ConversationList or ChatWindow make direct apiClient calls, those also need mocking here.
    mockedApiClient.listConversations.mockResolvedValue({ data: [], error: null });
    mockedApiClient.getMessageHistory.mockResolvedValue({ data: [], error: null });
    mockedApiClient.createConversation.mockResolvedValue({ data: {}, error: null });
    mockedApiClient.sendMessage.mockResolvedValue({ data: {}, error: null });
  });

  it('should display dashboard elements for authenticated user', async () => {
    await render(<DashboardPage />);
    expect(await screen.findByText('Dashboard')).toBeInTheDocument();
    expect(await screen.findByTestId('conversation-list')).toBeInTheDocument();
    expect(await screen.findByTestId('chat-window')).toBeInTheDocument();
    expect(await screen.findByRole('button', { name: /logout/i })).toBeInTheDocument();
  });

  it('should call logout function and redirect to login on logout button click', async () => {
    await render(<DashboardPage />);

    const logoutButton = await screen.findByRole('button', { name: /logout/i });
    fireEvent.click(logoutButton);

    expect(mockLogout).toHaveBeenCalledTimes(1);
    await waitFor(() => {
        expect(mockPush).toHaveBeenCalledWith('/login');
    });
  });

  it('should redirect to login if user is not authenticated', async () => {
    (useAuth as jest.Mock).mockReturnValueOnce({
      logout: mockLogout,
      isLoading: false,
      user: null, // Mock no logged-in user
      session: null,
    });

    await render(<DashboardPage />);
    await waitFor(() => {
        expect(mockPush).toHaveBeenCalledWith('/login');
    });
  });

  it('should show loading state', async () => {
    (useAuth as jest.Mock).mockReturnValueOnce({
      logout: mockLogout,
      isLoading: true, // Mock loading state
      user: null,
      session: null,
    });

    await render(<DashboardPage />);
    expect(screen.getByText(/Loading user data.../i)).toBeInTheDocument();
  });
});