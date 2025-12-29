'use client';

import { useAuth } from '../../components/AuthContext';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import ConversationList from '../../components/ConversationList';
import ChatWindow from '../../components/ChatWindow';

export default function DashboardPage() {
  const { user, isLoading, logout } = useAuth();
  const router = useRouter();
  const [selectedConversationId, setSelectedConversationId] = useState<string | null>(null);

  useEffect(() => {
    if (!isLoading && !user) {
      router.push('/login');
    }
  }, [user, isLoading, router]);

  if (isLoading) {
    return (
      <div className="flex min-h-full flex-1 flex-col justify-center px-6 py-12 lg:px-8">
        <p className="text-center text-gray-900">Loading user data...</p>
      </div>
    );
  }

  if (!user) {
    // Should be redirected by useEffect, but return null for safety
    return null;
  }

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Sidebar for ConversationList */}
      <aside className="w-64 border-r bg-white">
        <div className="p-4 border-b">
          <h2 className="text-2xl font-bold">Dashboard</h2>
        </div>
        {user.id && (
          <ConversationList agentId={user.id} onSelectConversation={setSelectedConversationId} />
        )}
      </aside>

      {/* Main content area */}
      <main className="flex-1 flex flex-col">
        <header className="flex items-center justify-between p-4 border-b bg-white shadow-sm">
          <h1 className="text-xl font-bold">
            {selectedConversationId ? `Conversation: ${selectedConversationId}` : 'Select a Conversation'}
          </h1>
          <button
            onClick={logout}
            disabled={isLoading}
            className="rounded-md bg-red-600 px-3 py-1.5 text-sm font-semibold leading-6 text-white shadow-sm hover:bg-red-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-600"
          >
            {isLoading ? 'Logging out...' : 'Logout'}
          </button>
        </header>

        <section className="flex-1 flex flex-col p-4 bg-gray-100">
          {selectedConversationId ? (
            <ChatWindow tenantId="test-tenant-id" conversationId={selectedConversationId} agentId={user.id} senderType="agent" />
          ) : (
            <div className="flex items-center justify-center h-full text-gray-500">
              <p>Please select a conversation from the list.</p>
            </div>
          )}
        </section>
      </main>
    </div>
  );
}
