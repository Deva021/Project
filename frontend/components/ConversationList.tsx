'use client';

import { useState, useEffect } from 'react';
import apiClient, { Conversation } from '../lib/api';

interface ConversationListProps {
  agentId: string;
  onSelectConversation: (conversationId: string) => void;
}

export default function ConversationList({ agentId, onSelectConversation }: ConversationListProps) {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedConversationId, setSelectedConversationId] = useState<string | null>(null);

  useEffect(() => {
    const fetchConversations = async () => {
      if (agentId) {
        setLoading(true);
        setError(null);
        try {
          const response = await apiClient.listConversations(agentId);
          if (response.data) {
            setConversations(response.data);
          } else if (response.error) {
            setError(response.error.message);
          }
        } catch (err: unknown) {
          setError((err as Error).message || 'Failed to fetch conversations.');
        } finally {
          setLoading(false);
        }
      }
    };

    fetchConversations();
  }, [agentId]);

  const handleConversationClick = (conversationId: string) => {
    setSelectedConversationId(conversationId);
    onSelectConversation(conversationId);
  };

  if (loading) {
    return <div className="p-4">Loading conversations...</div>;
  }

  if (error) {
    return <div className="p-4 text-red-500">Error: {error}</div>;
  }

  return (
    <div className="w-64 bg-gray-100 h-full overflow-y-auto border-r">
      <h2 className="text-xl font-bold p-4 border-b">Conversations</h2>
      {conversations.length === 0 ? (
        <p className="p-4 text-gray-500">No conversations found.</p>
      ) : (
        <ul>
          {conversations.map(conversation => (
            <li
              key={conversation.id}
              className={`p-4 border-b cursor-pointer hover:bg-gray-200 ${
                selectedConversationId === conversation.id ? 'bg-blue-200' : ''
              }`}
              onClick={() => handleConversationClick(conversation.id)}
            >
              <h3 className="font-semibold">{conversation.title}</h3>
              <p className="text-sm text-gray-600">Status: {conversation.status}</p>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
