import { useState, useEffect, useRef } from 'react';
import apiClient, { Message } from '../lib/api';
import { toast } from 'sonner';
import { Skeleton } from './ui/Skeleton';
import { motion, AnimatePresence } from 'framer-motion';
import { Send, User, Bot, Loader2, XCircle } from 'lucide-react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

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
  const [isSending, setIsSending] = useState(false);
  const [showCloseModal, setShowCloseModal] = useState(false);
  const [conversationStatus, setConversationStatus] = useState<string>('OPEN');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    // Sync state with prop when conversationId changes (e.g. agent switching chats)
    setConversationId(initialConversationId);
    if (initialConversationId) {
      setMessages([]); // Clear messages while loading new ones
    } else {
      setMessages([]); // Clear if no conversation selected
    }
  }, [initialConversationId]);

  useEffect(() => {
    if (conversationId) {
      setLoading(true);
      
      // Fetch initial status and messages
      Promise.all([
        apiClient.getConversation(conversationId),
        apiClient.getMessageHistory(conversationId)
      ]).then(([convRes, msgRes]) => {
        if (convRes.data) setConversationStatus(convRes.data.status);
        if (msgRes.data) setMessages(msgRes.data);
        if (convRes.error || msgRes.error) toast.error('Failed to load conversation details.');
      }).finally(() => setLoading(false));

      const pollingInterval = setInterval(async () => {
        try {
          const [convRes, msgRes] = await Promise.all([
            apiClient.getConversation(conversationId),
            apiClient.getMessageHistory(conversationId)
          ]);

          if (convRes.data) setConversationStatus(convRes.data.status);

          if (msgRes.data) {
            setMessages(prevMessages => {
              const newMessages = msgRes.data!.filter(newMessage => {
                const existsById = prevMessages.some(oldMsg => oldMsg.id === newMessage.id);
                if (existsById) return false;

                const isOptimisticMatch = prevMessages.some(oldMsg => 
                  oldMsg.id.startsWith('temp-') && 
                  oldMsg.text === newMessage.text && 
                  oldMsg.senderType === newMessage.senderType
                );
                
                return !isOptimisticMatch;
              });

              if (newMessages.length > 0) {
                return [...prevMessages, ...newMessages].sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
              }
              return prevMessages;
            });
          }
        } catch (err) {
          console.error("Error polling for updates:", err);
        }
      }, 3000);

      return () => clearInterval(pollingInterval);
    }
  }, [conversationId]);

  const handleSendMessage = async () => {
    if (inputText.trim() === '' || isSending) return;

    setIsSending(true);
    const tempId = `temp-${Date.now()}`;
    const optimisticMessage: Message = {
      id: tempId,
      conversationId: conversationId || 'temp',
      senderId: agentId || 'visitor',
      senderType,
      text: inputText,
      createdAt: new Date().toISOString(),
    };

    // Optimistic update
    setMessages(prev => [...prev, optimisticMessage]);
    setInputText('');

    try {
      let currentConversationId = conversationId;

      if (senderType === 'agent') {
        if (!currentConversationId) {
          throw new Error('Agent cannot send message without an active conversation.');
        }
        const response = await apiClient.sendMessage(currentConversationId, optimisticMessage.text, senderType, agentId);
        if (response.data) {
          setMessages(prev => prev.map(msg => msg.id === tempId ? response.data! : msg));
        } else {
          throw new Error('Failed to send message.');
        }
      } else { // senderType === 'visitor'
        if (!currentConversationId) {
          const response = await apiClient.createConversation(tenantId, 'New Chat', optimisticMessage.text);
          if (response.data) {
            currentConversationId = response.data.conversation.id;
            setConversationId(currentConversationId);
            // Replace optimistic message with real initial message
            setMessages([response.data.initialMessage]);
          } else {
            throw new Error('Failed to create conversation.');
          }
        } else {
          const response = await apiClient.sendMessage(currentConversationId, optimisticMessage.text, senderType);
          if (response.data) {
             setMessages(prev => prev.map(msg => msg.id === tempId ? response.data! : msg));
          } else {
            throw new Error('Failed to send message.');
          }
        }
      }
    } catch (err: unknown) {
      toast.error((err as Error).message || 'An error occurred.');
      setMessages(prev => prev.filter(msg => msg.id !== tempId));
      setInputText(optimisticMessage.text);
    } finally {
      setIsSending(false);
    }
  };

  const handleCloseConversation = () => {
    if (!conversationId) return;
    setShowCloseModal(true);
  };

  const confirmClose = async () => {
    if (!conversationId) return;
    try {
      const response = await apiClient.updateConversationStatus(conversationId, 'CLOSED');
      if (response.error) throw new Error(response.error.message);
      toast.success('Conversation closed.');
      window.location.reload();
    } catch (err: unknown) {
      toast.error((err as Error).message || 'Failed to close conversation.');
    } finally {
      setShowCloseModal(false);
    }
  };

  return (
    <div className="flex flex-col h-full overflow-hidden bg-background/50 backdrop-blur-xl border border-white/10 shadow-2xl rounded-2xl">
      {/* Header */}
      <div className="px-6 py-4 border-b border-white/10 flex items-center gap-3 bg-white/5">
        <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center text-primary">
          <Bot size={20} />
        </div>
        <div className="flex-1">
          <h3 className="font-semibold text-sm">Support Assistant</h3>
          <div className="flex items-center gap-1.5">
            <span className={cn(
              "w-2 h-2 rounded-full animate-pulse",
              conversationStatus === 'OPEN' ? "bg-green-500" : "bg-red-500"
            )} />
            <span className="text-[10px] text-muted-foreground uppercase tracking-wider font-medium">
              {conversationStatus === 'OPEN' ? 'Online' : 'Closed'}
            </span>
          </div>
        </div>
        {senderType === 'agent' && conversationId && (
          <button
            onClick={handleCloseConversation}
            className="flex items-center gap-2 px-3 py-1.5 text-xs font-medium text-red-400 hover:bg-red-400/10 rounded-lg transition-all border border-red-400/20"
          >
            <XCircle size={14} />
            Close Chat
          </button>
        )}
      </div>

      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto p-6 space-y-6 custom-scrollbar">
        {loading && messages.length === 0 ? (
          <div className="space-y-6">
             <div className="flex justify-start"><Skeleton className="h-12 w-2/3 rounded-2xl rounded-tl-none" /></div>
             <div className="flex justify-end"><Skeleton className="h-12 w-1/2 rounded-2xl rounded-tr-none" /></div>
             <div className="flex justify-start"><Skeleton className="h-12 w-3/4 rounded-2xl rounded-tl-none" /></div>
          </div>
        ) : (
          <AnimatePresence initial={false}>
            {messages.map((msg) => {
              const isMe = msg.senderType === senderType;
              return (
                <motion.div
                  key={msg.id}
                  initial={{ opacity: 0, y: 10, scale: 0.95 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  transition={{ duration: 0.2 }}
                  className={cn(
                    "flex items-end gap-2",
                    isMe ? 'flex-row-reverse' : 'flex-row'
                  )}
                >
                  <div className={cn(
                    "w-8 h-8 rounded-full flex items-center justify-center text-[10px] font-bold shrink-0",
                    isMe ? 'bg-primary/20 text-primary' : 'bg-zinc-800 text-zinc-400'
                  )}>
                    {msg.senderType === 'visitor' ? <User size={14} /> : <Bot size={14} />}
                  </div>
                  <div
                    className={cn(
                      "relative px-4 py-2.5 rounded-2xl text-sm shadow-sm max-w-[75%]",
                      isMe
                        ? 'bg-primary text-white rounded-br-none'
                        : 'bg-zinc-900 text-white border border-white/5 rounded-bl-none',
                      msg.id.startsWith('temp-') && 'opacity-70'
                    )}
                  >
                    {msg.text}
                    <div className={cn(
                      "text-[10px] mt-1 opacity-50",
                      isMe ? 'text-right' : 'text-left'
                    )}>
                      {new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </div>
                  </div>
                </motion.div>
              );
            })}
          </AnimatePresence>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input Area */}
      <div className="p-4 bg-white/5 border-t border-white/10">
        {conversationStatus === 'OPEN' ? (
          <div className="relative flex items-center gap-2 bg-white/5 rounded-xl border border-white/10 p-1 focus-within:border-primary/50 transition-colors">
            <input
              type="text"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
              className="flex-1 bg-transparent px-4 py-2 text-sm focus:outline-none placeholder:text-muted-foreground"
              placeholder="Type your message..."
              disabled={isSending}
            />
            <button
              onClick={handleSendMessage}
              disabled={!inputText.trim() || isSending}
              className="p-2 bg-primary hover:bg-primary-hover text-white rounded-lg transition-all disabled:opacity-50 disabled:scale-95 active:scale-90"
            >
              {isSending ? <Loader2 size={18} className="animate-spin" /> : <Send size={18} />}
            </button>
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center py-2 px-4 bg-red-500/5 border border-red-500/20 rounded-xl text-center">
            <p className="text-xs font-medium text-red-400">This conversation is closed.</p>
            <p className="text-[10px] text-red-400/60">You can no longer send messages here.</p>
          </div>
        )}
      </div>

      {/* Close Conversation Modal */}
      <AnimatePresence>
        {showCloseModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: 20 }}
              className="bg-zinc-900 border border-white/10 p-8 rounded-3xl max-w-sm w-full shadow-2xl text-center space-y-6"
            >
              <div className="w-16 h-16 bg-red-500/10 text-red-500 rounded-full flex items-center justify-center mx-auto">
                <XCircle size={32} />
              </div>
              <div>
                <h3 className="text-xl font-bold text-white mb-2">Close Conversation?</h3>
                <p className="text-sm text-muted-foreground leading-relaxed">
                  This conversation will be marked as closed and permanently deleted in <span className="text-white font-semibold">24 hours</span>.
                </p>
              </div>
              <div className="flex flex-col gap-3 pt-2">
                <button
                  onClick={confirmClose}
                  className="w-full py-3 bg-red-500 hover:bg-red-600 text-white font-semibold rounded-xl transition-all active:scale-95"
                >
                  Yes, Close Chat
                </button>
                <button
                  onClick={() => setShowCloseModal(false)}
                  className="w-full py-3 bg-white/5 hover:bg-white/10 text-white font-semibold rounded-xl transition-all active:scale-95"
                >
                  Cancel
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}