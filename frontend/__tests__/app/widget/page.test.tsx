
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import WidgetPage from '@/app/widget/page';

// Mock the ChatWindow component
jest.mock('../../../components/ChatWindow', () => {
  return function DummyChatWindow() {
    return <div data-testid="chat-window"></div>;
  };
});

describe('WidgetPage', () => {
  it('renders the chat button and the chat window is visible by default', () => {
    render(<WidgetPage />);
    expect(screen.getByText('Close')).toBeInTheDocument();
    expect(screen.getByTestId('chat-window')).toBeInTheDocument();
  });

  it('hides the chat window when the close button is clicked', () => {
    render(<WidgetPage />);
    const closeButton = screen.getByText('Close');
    fireEvent.click(closeButton);

    expect(screen.queryByTestId('chat-window')).not.toBeInTheDocument();
    expect(screen.getByText('Chat')).toBeInTheDocument();
  });

  it('shows the chat window when the chat button is clicked', () => {
    render(<WidgetPage />);
    const closeButton = screen.getByText('Close');
    fireEvent.click(closeButton); // First click to hide

    const chatButton = screen.getByText('Chat');
    fireEvent.click(chatButton); // Second click to show

    expect(screen.getByTestId('chat-window')).toBeInTheDocument();
    expect(screen.getByText('Close')).toBeInTheDocument();
  });
});
