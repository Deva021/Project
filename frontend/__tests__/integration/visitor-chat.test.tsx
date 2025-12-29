
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import WidgetPage from '../../app/widget/page';
import apiClient from '../../lib/api';

// Mock the apiClient
jest.mock('../../lib/api');

const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

describe('Visitor Chat Flow', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockedApiClient.getMessageHistory.mockResolvedValue({ data: [], error: null });
  });

  it('should open the widget, send a message, and create a new conversation', async () => {
    // Arrange
    const tenantId = 'test-tenant';
    const initialMessage = 'Hello, I need help!';
    const conversation = { id: 'conv-1', tenantId, title: 'New Chat', status: 'OPEN', createdAt: '', updatedAt: '' };
    const message = { id: 'msg-1', conversationId: 'conv-1', senderType: 'visitor' as const, text: initialMessage, createdAt: '', senderId: '' };

    mockedApiClient.createConversation.mockResolvedValue({
      data: { conversation, initialMessage: message },
      error: null,
    });

    render(<WidgetPage />);

    // The widget is open by default, so the button text is 'Close'
    expect(screen.getByText('Close')).toBeInTheDocument();

    const input = screen.getByPlaceholderText('Type a message...');
    const sendButton = screen.getByText('Send');

    fireEvent.change(input, { target: { value: initialMessage } });
    fireEvent.click(sendButton);

    // Assert
    await waitFor(() => {
      expect(mockedApiClient.createConversation).toHaveBeenCalledWith(tenantId, 'New Chat', initialMessage);
    });

    await waitFor(() => {
        expect(screen.getByText(initialMessage)).toBeInTheDocument();
    });
  });
});
