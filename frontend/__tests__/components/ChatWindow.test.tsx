
import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import '@testing-library/jest-dom';
import ChatWindow from '../../components/ChatWindow';
import apiClient from '../../lib/api'; // Import apiClient


// Mock setInterval and clearInterval to control timer behavior in tests
const MOCK_INTERVAL_ID = 12345;
jest.spyOn(global, 'setInterval').mockReturnValue(MOCK_INTERVAL_ID as number);
jest.spyOn(global, 'clearInterval').mockReturnValue(undefined);

// Mock the apiClient
jest.mock('../../lib/api');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

describe('ChatWindow', () => {
  const tenantId = 'test-tenant-id';

  beforeEach(() => {
    jest.clearAllMocks();
    // Mock getMessageHistory to return an empty array initially
    mockedApiClient.getMessageHistory.mockResolvedValue({ data: [], error: null });
    // Mock createConversation
    mockedApiClient.createConversation.mockImplementation(async (tId, title, messageText) => {
        const conversation = { id: 'new-conv-id', tenantId: tId, title: title, status: 'OPEN', createdAt: '', updatedAt: '' };
        const initialMessage = { id: 'msg-1', conversationId: 'new-conv-id', senderType: 'visitor' as const, text: messageText, createdAt: '', senderId: 'visitor-id' };
        return {
            data: { conversation, initialMessage },
            error: null,
        };
    });
    // Mock sendMessage
    mockedApiClient.sendMessage.mockImplementation(async (convId, messageText, senderType) => {
        return {
            data: { id: 'msg-rand', conversationId: convId, senderType: senderType, text: messageText, createdAt: '', senderId: 'visitor-id' },
            error: null,
        };
    });
  });

  it('renders the chat window', async () => {
    render(<ChatWindow tenantId={tenantId} />);
    await screen.findByPlaceholderText('Type a message...'); // Wait for input to be in document
    await screen.findByRole('button', { name: 'Send' }); // Wait for send button to be in document
  });

  it('allows typing in the input', async () => {
    await act(async () => {
      render(<ChatWindow tenantId={tenantId} />);
    });
    const input = await screen.findByPlaceholderText('Type a message...');
    fireEvent.change(input, { target: { value: 'Hello, world!' } });
    expect(input).toHaveValue('Hello, world!');
  });

  it('adds a message to the chat when sending and calls createConversation', async () => {
    await act(async () => {
      render(<ChatWindow tenantId={tenantId} />);
    });
    const input = screen.getByPlaceholderText('Type a message...');
    const sendButton = await screen.findByRole('button', { name: 'Send' });
    const testMessage = 'This is a test message';

    fireEvent.change(input, { target: { value: testMessage } });
    fireEvent.click(sendButton);

    await waitFor(() => expect(screen.getByRole('button', { name: 'Sending...' })).toBeInTheDocument());
    await waitFor(() => expect(screen.getByRole('button', { name: 'Send' })).toBeInTheDocument());

    expect(mockedApiClient.createConversation).toHaveBeenCalledWith(tenantId, 'New Chat', testMessage);
    await waitFor(() => expect(screen.getByText(testMessage)).toBeInTheDocument());
    expect(input).toHaveValue('');
  });

  it('adds a message to the chat when sending and calls sendMessage if conversation exists', async () => {
    const existingConversationId = 'existing-conv-id';
    await act(async () => {
      render(<ChatWindow tenantId={tenantId} conversationId={existingConversationId} senderType="visitor" />);
    });
    const input = screen.getByPlaceholderText('Type a message...');
    const sendButton = await screen.findByRole('button', { name: 'Send' });
    const testMessage = 'Another message';

    fireEvent.change(input, { target: { value: testMessage } });
    fireEvent.click(sendButton);

    await waitFor(() => expect(screen.getByRole('button', { name: 'Sending...' })).toBeInTheDocument());
    await waitFor(() => expect(screen.getByRole('button', { name: 'Send' })).toBeInTheDocument());

    expect(mockedApiClient.sendMessage).toHaveBeenCalledWith(existingConversationId, testMessage, 'visitor');
    await waitFor(() => expect(screen.getByText(testMessage)).toBeInTheDocument());
    expect(input).toHaveValue('');
  });

  it('does not add a message if the input is empty', async () => {
    await act(async () => {
      render(<ChatWindow tenantId={tenantId} />);
    });
    const sendButton = await screen.findByRole('button', { name: 'Send' });
    fireEvent.click(sendButton);

    await waitFor(() => {
        const messages = screen.queryAllByTestId('message');
        expect(messages.length).toBe(0);
    });
  });

  it('sends a message on Enter key press', async () => {
    await act(async () => {
      render(<ChatWindow tenantId={tenantId} />);
    });
    const input = screen.getByPlaceholderText('Type a message...');
    const testMessage = 'Message on enter';

    fireEvent.change(input, { target: { value: testMessage } });
    fireEvent.keyPress(input, { key: 'Enter', code: 'Enter', charCode: 13 });

    await waitFor(() => expect(screen.getByRole('button', { name: 'Sending...' })).toBeInTheDocument());
    await waitFor(() => expect(screen.getByRole('button', { name: 'Send' })).toBeInTheDocument());

    expect(mockedApiClient.createConversation).toHaveBeenCalledWith(tenantId, 'New Chat', testMessage);
    await waitFor(() => expect(screen.getByText(testMessage)).toBeInTheDocument());
    expect(input).toHaveValue('');
  });
});