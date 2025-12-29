import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import DashboardPage from '../../app/dashboard/page';
import { useAuth } from '../../components/AuthContext';
import { useRouter } from 'next/navigation';
import apiClient from '../../lib/api';
import { Session } from '@supabase/supabase-js';

// Mock the AuthContext
jest.mock('../../components/AuthContext', () => ({
  useAuth: jest.fn(),
}));

// Mock next/navigation
jest.mock('next/navigation', () => ({
  useRouter: jest.fn(),
}));

// Mock the apiClient
jest.mock('../../lib/api');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

describe('Agent Dashboard Integration', () => {
  const mockAgentUser = { id: 'agent-123', email: 'agent@example.com' };
  const mockAgentSession: Session = {
    access_token: 'mock-agent-token',
    token_type: 'Bearer',
    user: mockAgentUser,
    expires_in: 3600,
    expires_at: Math.floor(Date.now() / 1000) + 3600,
    refresh_token: 'mock-refresh-token',
  };

  const mockConversations = [
    { id: 'conv-1', tenantId: 'tenant-a', title: 'Conversation 1', status: 'OPEN', createdAt: '2023-01-01T10:00:00Z', updatedAt: '2023-01-01T10:00:00Z' },
    { id: 'conv-2', tenantId: 'tenant-a', title: 'Conversation 2', status: 'CLOSED', createdAt: '2023-01-01T11:00:00Z', updatedAt: '2023-01-01T11:00:00Z' },
  ];

  const mockMessages = [
    { id: 'msg-1', conversationId: 'conv-1', senderId: 'visitor-456', senderType: 'visitor', text: 'Hi, I need help!', createdAt: '2023-01-01T10:01:00Z' },
    { id: 'msg-2', conversationId: 'conv-1', senderId: 'agent-123', senderType: 'agent', text: 'How can I assist you?', createdAt: '2023-01-01T10:02:00Z' },
  ];

  const mockPush = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useAuth as jest.Mock).mockReturnValue({
      logout: jest.fn(),
      isLoading: false,
      user: mockAgentUser,
      session: mockAgentSession,
    });
    (useRouter as jest.Mock).mockReturnValue({
      push: mockPush,
    });

    mockedApiClient.listConversations.mockResolvedValue({ data: mockConversations, error: null });
    mockedApiClient.getMessageHistory.mockResolvedValue({ data: mockMessages, error: null });
    mockedApiClient.sendMessage.mockResolvedValue({ data: { id: 'new-msg-3', conversationId: 'conv-1', senderId: mockAgentUser.id, senderType: 'agent', text: 'Test reply', createdAt: new Date().toISOString() }, error: null });
    mockedApiClient.createConversation.mockResolvedValue({ data: {}, error: null }); // Should not be called by agent
  });

  it('should display dashboard elements for authenticated agent and load conversations', async () => {
    render(<DashboardPage />);

    expect(await screen.findByText('Dashboard')).toBeInTheDocument();
    expect(screen.getByText('Conversation 1')).toBeInTheDocument();
    expect(screen.getByText('Conversation 2')).toBeInTheDocument();
    expect(screen.getByText('Select a Conversation')).toBeInTheDocument(); // Initial state
  });

  it('should select a conversation and display its messages', async () => {
    render(<DashboardPage />);

    // Select Conversation 1
    const conv1 = await screen.findByText('Conversation 1');
    fireEvent.click(conv1);

    await waitFor(() => {
      expect(screen.getByText('Conversation: conv-1')).toBeInTheDocument();
      expect(screen.getByText('Hi, I need help!')).toBeInTheDocument();
      expect(screen.getByText('How can I assist you?')).toBeInTheDocument();
    });

    expect(mockedApiClient.getMessageHistory).toHaveBeenCalledWith('conv-1');
  });

  it('should send an agent reply and display it in the chat window', async () => {
    render(<DashboardPage />);

    // Select Conversation 1
    const conv1 = await screen.findByText('Conversation 1');
    fireEvent.click(conv1);

    await waitFor(() => {
      expect(screen.getByPlaceholderText('Type a message...')).toBeInTheDocument();
    });

    const messageInput = screen.getByPlaceholderText('Type a message...') as HTMLInputElement;
    fireEvent.change(messageInput, { target: { value: 'Test reply' } });
    fireEvent.click(screen.getByRole('button', { name: 'Send' }));

    await waitFor(() => {
      expect(mockedApiClient.sendMessage).toHaveBeenCalledWith(
        'conv-1',
        'Test reply',
        'agent',
        mockAgentUser.id
      );
    });

    // Verify the new message appears in the chat window (this relies on the polling mechanism or re-fetching)
    // For this test, we mock the sendMessage response to directly contain the new message
    // and assume ChatWindow handles adding it to its internal state correctly.
    await waitFor(() => {
        expect(screen.getByText('Test reply')).toBeInTheDocument();
    });

    expect(messageInput.value).toBe(''); // Input should be cleared
  });

  it('should redirect to login if not authenticated', async () => {
    (useAuth as jest.Mock).mockReturnValueOnce({
      logout: jest.fn(),
      isLoading: false,
      user: null, // No authenticated user
      session: null,
    });

    render(<DashboardPage />);

    await waitFor(() => {
      expect(mockPush).toHaveBeenCalledWith('/login');
    });
  });
});
