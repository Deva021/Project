import apiClient from '../../lib/api';
import { getSession } from '../../lib/supabase';

// Mock getSession from supabase.ts
jest.mock('../../lib/supabase', () => ({
  getSession: jest.fn(),
}));

// Mock the global fetch function
const mockFetch = jest.fn();
global.fetch = mockFetch;

// Mock environment variables
process.env.NEXT_PUBLIC_API_BASE_URL = '/test-backend';

describe('ApiClient', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (getSession as jest.Mock).mockResolvedValue(null); // Default: no session
  });

  describe('fetchWithAuth (private method via public getters/setters)', () => {
    it('should include Authorization header if session is available', async () => {
      const mockAccessToken = 'test-access-token';
      (getSession as jest.Mock).mockResolvedValue({ access_token: mockAccessToken });
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({ data: 'test' }),
      });

      const response = await apiClient.get('/test');
      expect(mockFetch).toHaveBeenCalledWith('/test-backend/test', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${mockAccessToken}`,
        },
      });
      expect(response.data).toEqual({ data: 'test' });
    });

    it('should NOT include Authorization header if no session', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({ data: 'test' }),
      });

      const response = await apiClient.get('/test-no-auth');
      expect(mockFetch).toHaveBeenCalledWith('/test-backend/test-no-auth', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      expect(response.data).toEqual({ data: 'test' });
    });

    it('should handle network errors gracefully', async () => {
      const networkError = new Error('Network down');
      mockFetch.mockRejectedValueOnce(networkError);

      const response = await apiClient.get('/network-error');
      expect(response.data).toBeNull();
      expect(response.error).toEqual({
        code: 'NETWORK_ERROR',
        message: 'Network down',
        status: 0,
      });
    });

    it('should parse API-specific error from JSON response', async () => {
      const mockApiError: ApiError = {
        code: 'BAD_REQUEST',
        message: 'Invalid input',
        status: 400,
        details: { field: 'email', reason: 'invalid format' },
      };
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        statusText: 'Bad Request',
        json: () => Promise.resolve(mockApiError),
      });

      const response = await apiClient.post('/error-endpoint', {});
      expect(response.data).toBeNull();
      expect(response.error).toEqual(mockApiError);
    });

    it('should handle non-JSON HTTP errors gracefully', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        statusText: 'Internal Server Error',
        json: () => Promise.reject(new Error('Not JSON')), // Simulate non-JSON response
      });

      const response = await apiClient.get('/non-json-error');
      expect(response.data).toBeNull();
      expect(response.error).toEqual({
        code: 'HTTP_ERROR',
        message: 'Internal Server Error',
        status: 500,
      });
    });
  });

  describe('HTTP Wrapper Methods (get, post, put, delete)', () => {
    it('get should make a GET request', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({ items: [] }),
      });
      await apiClient.get('/items');
      expect(mockFetch).toHaveBeenCalledWith('/test-backend/items', expect.objectContaining({ method: 'GET' }));
    });

    it('post should make a POST request with JSON body', async () => {
      const postData = { name: 'new item' };
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({ id: '1', ...postData }),
      });
      await apiClient.post('/items', postData);
      expect(mockFetch).toHaveBeenCalledWith('/test-backend/items', expect.objectContaining({
        method: 'POST',
        body: JSON.stringify(postData),
        headers: { 'Content-Type': 'application/json' },
      }));
    });

    it('put should make a PUT request with JSON body', async () => {
      const putData = { id: '1', name: 'updated item' };
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(putData),
      });
      await apiClient.put('/items/1', putData);
      expect(mockFetch).toHaveBeenCalledWith('/test-backend/items/1', expect.objectContaining({
        method: 'PUT',
        body: JSON.stringify(putData),
        headers: { 'Content-Type': 'application/json' },
      }));
    });

    it('delete should make a DELETE request', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({ message: 'deleted' }),
      });
      await apiClient.delete('/items/1');
      expect(mockFetch).toHaveBeenCalledWith('/test-backend/items/1', expect.objectContaining({ method: 'DELETE' }));
    });
  });

  describe('Specific API Methods', () => {
    // Test getHealthStatus
    it('getHealthStatus should call /api/health and return status', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({ status: 'OK' }),
      });
      const response = await apiClient.getHealthStatus();
      expect(mockFetch).toHaveBeenCalledWith('/test-backend/api/health', expect.anything());
      expect(response.data).toEqual({ status: 'OK' });
    });

    // Test listConversations
    it('listConversations should call /api/conversations with tenantId', async () => {
      const mockConversations = [{ id: 'conv1', title: 'Chat' }];
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockConversations),
      });
      const response = await apiClient.listConversations('tenant-id-123');
      expect(mockFetch).toHaveBeenCalledWith('/test-backend/api/conversations?tenantId=tenant-id-123', expect.anything());
      expect(response.data).toEqual(mockConversations);
    });

    // Test createConversation
    it('createConversation should call /api/conversations with correct body', async () => {
      const mockNewConv = { conversation: { id: 'new-conv' }, initialMessage: { id: 'msg1' } };
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockNewConv),
      });
      const response = await apiClient.createConversation('tenant-id', 'New Chat', 'Hi');
      expect(mockFetch).toHaveBeenCalledWith('/test-backend/api/conversations', expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({ tenant_id: 'tenant-id', title: 'New Chat', message: 'Hi' }),
      }));
      expect(response.data).toEqual(mockNewConv);
    });

    // Test getMessageHistory
    it('getMessageHistory should call /api/conversations/{id}/messages', async () => {
      const mockMessages = [{ id: 'msg1', text: 'hello' }];
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockMessages),
      });
      const response = await apiClient.getMessageHistory('conv-id-123');
      expect(mockFetch).toHaveBeenCalledWith('/test-backend/api/conversations/conv-id-123/messages', expect.anything());
      expect(response.data).toEqual(mockMessages);
    });

    // Test sendMessage
    it('sendMessage should call /api/conversations/{id}/messages with correct body', async () => {
      const mockMessage = { id: 'sent-msg', text: 'test' };
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockMessage),
      });
      const response = await apiClient.sendMessage('conv-id', 'Hello', 'visitor', 'sender-id');
      expect(mockFetch).toHaveBeenCalledWith('/test-backend/api/conversations/conv-id/messages', expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({ text: 'Hello', sender_type: 'visitor', sender_id: 'sender-id' }),
      }));
      expect(response.data).toEqual(mockMessage);
    });

    // Test updateConversationStatus
    it('updateConversationStatus should call /api/conversations/{id}/status with correct body', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({}), // No content for 204
      });
      const response = await apiClient.updateConversationStatus('conv-id', 'CLOSED');
      expect(mockFetch).toHaveBeenCalledWith('/test-backend/api/conversations/conv-id/status', expect.objectContaining({
        method: 'PUT',
        body: JSON.stringify({ status: 'CLOSED' }),
      }));
      expect(response.data).toEqual({});
    });

    // Test pollForEvents
    it('pollForEvents should call /api/poll', async () => {
      const mockEvents = [{ type: 'new_message' }];
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockEvents),
      });
      const response = await apiClient.pollForEvents();
      expect(mockFetch).toHaveBeenCalledWith('/test-backend/api/poll', expect.anything());
      expect(response.data).toEqual(mockEvents);
    });
  });
});
