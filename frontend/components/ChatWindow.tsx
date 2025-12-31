'use client';

import { useState, useEffect } from 'react';
import apiClient, { Message } from '../lib/api';

interface ChatWindowProps {
  tenantId: string;
  conversationId?: string;
  agentId?: string;
  senderType: 'visitor' | 'agent';
}

export default function ChatWindow({ tenantId, conversationId: initialConversationId, agentId, senderType }: ChatWindowProps) {
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputText, setInputText] = useState('');
  const [conversationId, setConversationId] = useState<string | undefined>(initialConversationId);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (conversationId) {
      setLoading(true);
      apiClient.getMessageHistory(conversationId)
        .then(response => {
          if (response.data) {
            setMessages(response.data);
          } else {
            setError('Failed to fetch message history.');
          }
        })
        .finally(() => setLoading(false));

      const pollingInterval = setInterval(async () => {
        try {
          const response = await apiClient.getMessageHistory(conversationId);
          if (response.data) {
            // Only add new messages to avoid duplicates and preserve existing order
            setMessages(prevMessages => {
              const newMessages = response.data.filter(
                (newMessage) => !prevMessages.some((oldMessage) => oldMessage.id === newMessage.id)
              );
              return [...prevMessages, ...newMessages].sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
            });
          }
        } catch (err) {
          console.error("Error polling for new messages:", err);
        }
      }, 3000); // Poll every 3 seconds

      return () => clearInterval(pollingInterval); // Cleanup interval on unmount or conversationId change
    }
  }, [conversationId]);

  const handleSendMessage = async () => {
    if (inputText.trim() === '') return;

    setLoading(true);
    setError(null);

    try {
      let currentConversationId = conversationId;

      if (senderType === 'agent') {
        if (!currentConversationId) {
          throw new Error('Agent cannot send message without an active conversation.');
        }
        const sendMessageResponse = await apiClient.sendMessage(currentConversationId, inputText, senderType, agentId);
        if (sendMessageResponse.data) {
          setMessages(prevMessages => [...prevMessages, sendMessageResponse.data!]);
        } else {
          throw new Error('Failed to send message.');
        }
      } else { // senderType === 'visitor'
        if (!currentConversationId) {
          const createConversationResponse = await apiClient.createConversation(tenantId, 'New Chat', inputText);
          if (createConversationResponse.data) {
            currentConversationId = createConversationResponse.data.conversation.id;
            setConversationId(currentConversationId);
            setMessages([createConversationResponse.data.initialMessage]);
          } else {
            throw new Error('Failed to create conversation.');
          }
        } else {
          const sendMessageResponse = await apiClient.sendMessage(currentConversationId, inputText, senderType);
          if (sendMessageResponse.data) {
            setMessages(prevMessages => [...prevMessages, sendMessageResponse.data!]);
          } else {
            throw new Error('Failed to send message.');
          }
        }
      }
      setInputText('');
    } catch (err: unknown) {
      setError((err as Error).message || 'An error occurred.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col h-full text-gray-900">
      <div className="flex-1 p-4 overflow-y-auto">
        {loading && messages.length === 0 && <p>Loading messages...</p>}
        {error && <p className="text-red-500">{error}</p>}
        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`flex ${
              msg.senderType === 'visitor' ? 'justify-end' : 'justify-start'
            } mb-2`}
          >
            <div
              data-testid="message"
              className={`rounded-lg px-3 py-2 ${
                msg.senderType === 'visitor'
                  ? 'bg-blue-500 text-white'
                  : 'bg-gray-700 text-white'
              }`}
            >
              {msg.text}
            </div>
          </div>
        ))}
      </div>
      <div className="flex p-4 border-t">
        <input
          type="text"
          value={inputText}
          onChange={(e) => setInputText(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
          className="flex-1 px-3 py-2 border rounded-l-md placeholder-gray-500"
          placeholder="Type a message..."
          disabled={loading}
        />
        <button
          onClick={handleSendMessage}
          className="px-4 py-2 bg-blue-500 text-white rounded-r-md"
          disabled={loading}
        >
          {loading ? 'Sending...' : 'Send'}
        </button>
      </div>
    </div>
  );
}