import { createClient, getSession, signIn, signOut } from '../../lib/supabase';
import { createBrowserClient } from '@supabase/ssr';
import { Session } from '@supabase/supabase-js';

// Mock the @supabase/ssr module
jest.mock('@supabase/ssr', () => ({
  createBrowserClient: jest.fn(() => ({
    auth: {
      getSession: jest.fn(),
      signInWithPassword: jest.fn(),
      signOut: jest.fn(),
    },
  })),
}));

// Mock environment variables
process.env.NEXT_PUBLIC_SUPABASE_URL = 'http://localhost:54321';
process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY = 'anon-key';

const mockSupabase = createBrowserClient(
  process.env.NEXT_PUBLIC_SUPABASE_URL!,
  process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!
);

describe('Supabase Client Utilities', () => {
  beforeEach(() => {
    // Reset mocks before each test
    jest.clearAllMocks();
    (createBrowserClient as jest.Mock).mockReturnValue(mockSupabase);
  });

  describe('createClient', () => {
    it('should initialize the Supabase browser client', () => {
      createClient();
      expect(createBrowserClient).toHaveBeenCalledWith(
        'http://localhost:54321',
        'anon-key'
      );
    });
  });

  describe('getSession', () => {
    it('should return session data if successful', async () => {
      const mockSession: Session = {
        access_token: 'mock-access-token',
        refresh_token: 'mock-refresh-token',
        expires_in: 3600,
        expires_at: 1234567890,
        token_type: 'Bearer',
        user: {
          id: 'user-id',
          aud: 'authenticated',
          role: 'authenticated',
          email: 'test@example.com',
          email_confirmed_at: '2025-01-01T00:00:00Z',
          phone: '',
          confirmed_at: '2025-01-01T00:00:00Z',
          last_sign_in_at: '2025-01-01T00:00:00Z',
          app_metadata: { provider: 'email' },
          user_metadata: {},
          created_at: '2025-01-01T00:00:00Z',
          updated_at: '2025-01-01T00:00:00Z',
          identities: [],
        },
      };
      (mockSupabase.auth.getSession as jest.Mock).mockResolvedValueOnce({ data: { session: mockSession }, error: null });

      const session = await getSession();
      expect(session).toEqual(mockSession);
      expect(mockSupabase.auth.getSession).toHaveBeenCalledTimes(1);
    });

    it('should return null and log error if fetching session fails', async () => {
      const mockError = new Error('Failed to fetch session');
      (mockSupabase.auth.getSession as jest.Mock).mockResolvedValueOnce({ data: { session: null }, error: mockError });
      const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

      const session = await getSession();
      expect(session).toBeNull();
      expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching session:', mockError);
      expect(mockSupabase.auth.getSession).toHaveBeenCalledTimes(1);
      consoleErrorSpy.mockRestore();
    });
  });

  describe('signIn', () => {
    it('should call signInWithPassword with provided credentials', async () => {
      (mockSupabase.auth.signInWithPassword as jest.Mock).mockResolvedValueOnce({ data: { user: {} }, error: null });

      await signIn('test@example.com', 'password123');
      expect(mockSupabase.auth.signInWithPassword).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123',
      });
    });

    it('should throw error if signInWithPassword fails', async () => {
      const mockError = new Error('Invalid credentials');
      (mockSupabase.auth.signInWithPassword as jest.Mock).mockResolvedValueOnce({ data: { user: null }, error: mockError });
      const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

      await expect(signIn('test@example.com', 'wrongpassword')).rejects.toThrow(mockError);
      expect(consoleErrorSpy).toHaveBeenCalledWith('Error signing in:', mockError);
      consoleErrorSpy.mockRestore();
    });
  });

  describe('signOut', () => {
    it('should call signOut method', async () => {
      (mockSupabase.auth.signOut as jest.Mock).mockResolvedValueOnce({ error: null });

      await signOut();
      expect(mockSupabase.auth.signOut).toHaveBeenCalledTimes(1);
    });

    it('should throw error if signOut fails', async () => {
      const mockError = new Error('Failed to sign out');
      (mockSupabase.auth.signOut as jest.Mock).mockResolvedValueOnce({ error: mockError });
      const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

      await expect(signOut()).rejects.toThrow(mockError);
      expect(consoleErrorSpy).toHaveBeenCalledWith('Error signing out:', mockError);
      consoleErrorSpy.mockRestore();
    });
  });
});
