
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import ConversationList from '../../components/ConversationList';
import apiClient, { Conversation } from '../../lib/api';

// Mock the apiClient
jest.mock('../../lib/api');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

describe('ConversationList', () => {
  const agentId = 'test-agent-id';
  const onSelectConversation = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders loading state initially', () => {
    mockedApiClient.listConversations.mockReturnValue(new Promise(() => {})); // Never resolves
    render(<ConversationList agentId={agentId} onSelectConversation={onSelectConversation} />);
    expect(screen.getByText('Loading conversations...')).toBeInTheDocument();
  });

  it('renders error state if API call fails', async () => {
    const errorMessage = 'Failed to fetch conversations';
    mockedApiClient.listConversations.mockResolvedValue({ data: null, error: { message: errorMessage, code: '', status: 500 } });
    render(<ConversationList agentId={agentId} onSelectConversation={onSelectConversation} />);

    await waitFor(() => {
      expect(screen.getByText(`Error: ${errorMessage}`)).toBeInTheDocument();
    });
  });

  it('renders a list of conversations and handles selection', async () => {
    const conversations: Conversation[] = [
      { id: 'conv-1', title: 'Conversation 1', status: 'OPEN', tenantId: 't-1', createdAt: '', updatedAt: '' },
      { id: 'conv-2', title: 'Conversation 2', status: 'CLOSED', tenantId: 't-1', createdAt: '', updatedAt: '' },
    ];
    mockedApiClient.listConversations.mockResolvedValue({ data: conversations, error: null });

    render(<ConversationList agentId={agentId} onSelectConversation={onSelectConversation} />);

    await waitFor(() => {
      expect(screen.getByText('Conversation 1')).toBeInTheDocument();
      expect(screen.getByText('Conversation 2')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Conversation 1'));

    expect(onSelectConversation).toHaveBeenCalledWith('conv-1');

    // Check for selection highlight
    expect(screen.getByText('Conversation 1').closest('li')).toHaveClass('bg-blue-200');
  });

  it('renders "No conversations found" if the list is empty', async () => {
    mockedApiClient.listConversations.mockResolvedValue({ data: [], error: null });
    render(<ConversationList agentId={agentId} onSelectConversation={onSelectConversation} />);

    await waitFor(() => {
      expect(screen.getByText('No conversations found.')).toBeInTheDocument();
    });
  });
});
