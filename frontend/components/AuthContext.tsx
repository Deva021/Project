'use client';

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { getSession, signIn as supabaseSignIn, signOut as supabaseSignOut, createClient } from '../lib/supabase';
import { Session, User } from '@supabase/supabase-js';
import { useRouter } from 'next/navigation';

interface AuthContextType {
  session: Session | null;
  user: User | null;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [session, setSession] = useState<Session | null>(null);
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  // Function to load session on initial render and re-establish on refresh
  const loadSession = useCallback(async () => {
    setIsLoading(true);
    try {
      const currentSession = await getSession();
      setSession(currentSession);
      setUser(currentSession?.user || null);
    } catch (error) {
      console.error('Failed to load session:', error);
      setSession(null);
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadSession();
  }, [loadSession]);

  const login = useCallback(async (email: string, password: string) => {
    setIsLoading(true);
    try {
      await supabaseSignIn(email, password);
      await loadSession(); // Reload session after successful sign-in
      router.push('/dashboard'); // T014: Implement post-login redirection
    } catch (error) {
      console.error('Login failed:', error);
      // Depending on error type, might want to set a local error state
      throw error; // Re-throw to allow UI to handle specific errors
    } finally {
      setIsLoading(false);
    }
  }, [loadSession, router]);

  const logout = useCallback(async () => {
    setIsLoading(true);
    try {
      await supabaseSignOut();
      setSession(null);
      setUser(null);
      router.push('/login'); // T018: Implement post-logout redirection
    } catch (error) {
      console.error('Logout failed:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  }, [router]);

  const register = useCallback(async (email: string, password: string) => {
    setIsLoading(true);
    try {
      const supabase = createClient();
      const { data, error } = await supabase.auth.signUp({ email, password });
      if (error) {
        throw error;
      }
      setSession(data.session);
      setUser(data.user);
      router.push('/dashboard'); // T015: Implement post-registration redirection
    } catch (error) {
      console.error('Registration failed:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  }, [router]);

  return (
    <AuthContext.Provider value={{ session, user, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
