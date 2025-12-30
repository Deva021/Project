'use client';

import { useAuth } from '../../components/AuthContext';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import ConversationList from '../../components/ConversationList';
import ChatWindow from '../../components/ChatWindow';
import { LogOut, LayoutDashboard, Settings, User as UserIcon, MessageSquare } from 'lucide-react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { toast } from 'sonner';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export default function DashboardPage() {
  const { user, isLoading, logout } = useAuth();
  const router = useRouter();
  const [selectedConversationId, setSelectedConversationId] = useState<string | null>(null);

  const handleNotImplemented = (feature: string) => {
    toast.info(`${feature} feature coming soon!`, {
      description: "We're working hard to bring this to you.",
    });
  };

  useEffect(() => {
    if (!isLoading && !user) {
      router.push('/login');
    }
  }, [user, isLoading, router]);

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background">
        <div className="flex flex-col items-center gap-4">
          <div className="w-12 h-12 border-4 border-primary/20 border-t-primary rounded-full animate-spin" />
          <p className="text-sm font-medium text-muted-foreground">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  if (!user) return null;

  return (
    <div className="flex h-screen bg-background text-foreground overflow-hidden">
      {/* Sidebar Navigation (Slim) */}
      <aside className="w-16 border-r border-white/10 bg-white/5 flex flex-col items-center py-6 gap-8">
        <div className="w-10 h-10 bg-primary rounded-xl flex items-center justify-center text-white shadow-lg shadow-primary/20">
          <MessageSquare size={24} />
        </div>
        <nav className="flex flex-col gap-4">
          <button 
            onClick={() => toast.success("You are already on the Dashboard")}
            className="p-3 rounded-xl bg-primary/10 text-primary transition-all"
          >
            <LayoutDashboard size={20} />
          </button>
          <button 
            onClick={() => handleNotImplemented("User Profile")}
            className="p-3 rounded-xl text-muted-foreground hover:bg-white/5 transition-all"
          >
            <UserIcon size={20} />
          </button>
          <button 
            onClick={() => handleNotImplemented("Settings")}
            className="p-3 rounded-xl text-muted-foreground hover:bg-white/5 transition-all"
          >
            <Settings size={20} />
          </button>
        </nav>
        <div className="mt-auto">
          <button 
            onClick={logout}
            className="p-3 rounded-xl text-red-400 hover:bg-red-400/10 transition-all"
            title="Logout"
          >
            <LogOut size={20} />
          </button>
        </div>
      </aside>

      {/* Conversation Sidebar */}
      <aside className="w-80 border-r border-white/10 flex flex-col">
          <ConversationList 
            tenantId={user?.app_metadata?.tenant_id as string || 'a0000000-0000-0000-0000-000000000001'}
            agentId={user?.id} 
            onSelectConversation={setSelectedConversationId}
            selectedId={selectedConversationId || undefined}
          />
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col bg-gray-50/50 dark:bg-transparent">
        <header className="h-16 border-b border-white/10 flex items-center justify-between px-8 bg-white/5 backdrop-blur-md">
          <div className="flex items-center gap-4">
            <h1 className="text-lg font-semibold">
              {selectedConversationId ? 'Active Conversation' : 'Dashboard'}
            </h1>
            {selectedConversationId && (
              <span className="px-2 py-0.5 bg-primary/10 text-primary text-[10px] font-bold uppercase tracking-wider rounded-full">
                Live
              </span>
            )}
          </div>
          <div className="flex items-center gap-4">
            <div className="text-right hidden sm:block">
              <p className="text-xs font-medium">{user.email}</p>
              <p className="text-[10px] text-muted-foreground">Support Agent</p>
            </div>
            <div className="w-8 h-8 rounded-full bg-accent/20 flex items-center justify-center text-accent font-bold text-xs border border-accent/20">
              {user.email?.[0].toUpperCase()}
            </div>
          </div>
        </header>

        <section className="flex-1 p-8 overflow-hidden">
          {selectedConversationId ? (
            <div className="h-full max-w-5xl mx-auto">
              <ChatWindow 
                tenantId={user.app_metadata?.tenant_id as string || 'a0000000-0000-0000-0000-000000000001'} 
                conversationId={selectedConversationId || undefined} 
                agentId={user.id} 
                senderType="agent" 
              />
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center h-full text-center space-y-6">
              <div className="w-24 h-24 bg-primary/5 rounded-full flex items-center justify-center text-primary/20">
                <MessageSquare size={48} />
              </div>
              <div>
                <h2 className="text-2xl font-bold mb-2">Welcome back!</h2>
                <p className="text-muted-foreground max-w-xs mx-auto">
                  Select a conversation from the sidebar to start assisting your customers in real-time.
                </p>
              </div>
            </div>
          )}
        </section>
      </main>
    </div>
  );
}
