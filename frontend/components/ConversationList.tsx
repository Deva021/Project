'use client';

import { useState, useEffect } from 'react';
import apiClient, { Conversation } from '../lib/api';
import { Search, MessageSquare, Clock, CheckCircle2, AlertCircle } from 'lucide-react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

interface ConversationListProps {
  tenantId?: string;
  agentId?: string;
  onSelectConversation: (id: string) => void;
  selectedId?: string;
}

const ConversationList: React.FC<ConversationListProps> = ({ tenantId, agentId, onSelectConversation, selectedId }) => {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    const fetchConversations = async () => {
      const idToUse = tenantId || agentId;
      if (idToUse) {
        try {
          const response = await apiClient.listConversations(idToUse, 'OPEN');
          if (response.data) {
            setConversations(prev => {
              // Only update if there are changes to avoid unnecessary re-renders
              if (JSON.stringify(prev) !== JSON.stringify(response.data)) {
                return response.data!;
              }
              return prev;
            });
          } else if (response.error && !conversations.length) {
            setError(response.error.message);
          }
        } catch (err: unknown) {
          if (!conversations.length) {
            setError((err as Error).message || 'Failed to fetch conversations.');
          }
        }
      }
    };

    const idToUse = tenantId || agentId;
    if (idToUse) {
      setIsLoading(true);
      fetchConversations().finally(() => setIsLoading(false));

      const interval = setInterval(fetchConversations, 5000);
      return () => clearInterval(interval);
    }
  }, [tenantId, agentId]);

  const filteredConversations = conversations.filter(c => 
    c.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
    c.id.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const getStatusIcon = (status: string) => {
    switch (status.toUpperCase()) {
      case 'OPEN': return <Clock size={14} className="text-blue-500" />;
      case 'CLOSED': return <CheckCircle2 size={14} className="text-green-500" />;
      default: return <AlertCircle size={14} className="text-gray-400" />;
    }
  };

  return (
    <div className="flex flex-col h-full bg-white dark:bg-zinc-900 border-r border-gray-200 dark:border-zinc-800">
      <div className="p-6 border-b border-gray-200 dark:border-zinc-800">
        <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
          <MessageSquare className="text-primary" size={20} />
          Conversations
        </h2>
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
          <input
            type="text"
            placeholder="Search chats..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-2 bg-gray-100 dark:bg-zinc-800 border-none rounded-xl text-sm focus:ring-2 focus:ring-primary/50 transition-all"
          />
        </div>
      </div>

      <div className="flex-1 overflow-y-auto custom-scrollbar">
        {isLoading ? (
          <div className="p-6 space-y-4">
            {[1, 2, 3].map(i => (
              <div key={i} className="h-16 bg-gray-100 dark:bg-zinc-800 rounded-xl animate-pulse" />
            ))}
          </div>
        ) : error ? (
          <div className="p-6 text-center">
            <p className="text-sm text-red-500 bg-red-50 dark:bg-red-900/20 p-3 rounded-lg">{error}</p>
          </div>
        ) : filteredConversations.length === 0 ? (
          <div className="p-10 text-center text-gray-400">
            <p className="text-sm">No conversations found.</p>
          </div>
        ) : (
          <div className="p-2 space-y-1">
            {filteredConversations.map(conversation => (
              <button
                key={conversation.id}
                onClick={() => onSelectConversation(conversation.id)}
                className={cn(
                  "w-full text-left p-4 rounded-xl transition-all group relative overflow-hidden",
                  selectedId === conversation.id 
                    ? "bg-primary/10 text-primary shadow-sm" 
                    : "hover:bg-gray-50 dark:hover:bg-zinc-800/50 text-gray-600 dark:text-gray-300"
                )}
              >
                {selectedId === conversation.id && (
                  <div className="absolute left-0 top-0 bottom-0 w-1 bg-primary" />
                )}
                <div className="flex justify-between items-start mb-1">
                  <h3 className={cn(
                    "font-semibold text-sm truncate pr-4",
                    selectedId === conversation.id ? "text-primary" : "text-gray-900 dark:text-white"
                  )}>
                    {conversation.title || 'Untitled Chat'}
                  </h3>
                  <span className="text-[10px] opacity-50 whitespace-nowrap">
                    {new Date(conversation.createdAt).toLocaleDateString()}
                  </span>
                </div>
                <div className="flex items-center gap-2 text-[11px] opacity-70">
                  {getStatusIcon(conversation.status)}
                  <span className="uppercase tracking-wider font-medium">{conversation.status}</span>
                </div>
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default ConversationList;
