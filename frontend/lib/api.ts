import { getSession } from './supabase'; // Assuming getSession is available
import type { Session } from '@supabase/supabase-js'; // Import Session type for context

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || ''; // Base path, e.g., /backend for local Tomcat, or full URL for production

// Define standardized API Error structure (from data-model.md)
export interface ApiError {
  code: string;
  message: string;
  status: number;
  details?: object;
}

// Define standardized API Response structure (from data-model.md)
export interface ApiResponse<T> {
  data: T | null;
  error: ApiError | null;
}

// Define specific DTOs for API calls (derived from contracts/api.yaml and data-model.md)
// These should ideally be generated from OpenAPI spec
export interface Conversation {
  id: string;
  tenantId: string;
  title: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface Message {
  id: string;
  conversationId: string;
  senderId: string;
  senderType: 'visitor' | 'agent';
  text: string;
  createdAt: string;
}


class ApiClient {
  private async fetchWithAuth<T>( // Add generic type T
    endpoint: string,
    options: RequestInit = {}
  ): Promise<ApiResponse<T>> { // Change 'any' to 'T'
    const session: Session | null = await getSession();
    const headers = {
      ...options.headers,
      'Content-Type': 'application/json',
    };

    if (session?.access_token) {
      headers['Authorization'] = `Bearer ${session.access_token}`;
    }

    try {
      const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        ...options,
        headers,
      });

      if (!response.ok) {
        let errorData: ApiError;
        try {
          const body = await response.json();
          errorData = {
            code: body.code || 'API_ERROR',
            message: body.message || response.statusText,
            status: response.status,
            details: body.details,
          };
        } catch (_e: unknown) { // Use _e here
          console.error("Error parsing API error response:", _e);
          errorData = {
            code: 'HTTP_ERROR',
            message: response.statusText || `Request failed with status ${response.status}`,
            status: response.status,
          };
        }
        return { data: null, error: errorData };
      }

      const data = await response.json();
      return { data, error: null };
    } catch (networkError: unknown) { // Changed any to unknown
      const errorData: ApiError = {
        code: 'NETWORK_ERROR',
        message: (networkError instanceof Error) ? networkError.message : 'Network request failed',
        status: 0,
      };
      return { data: null, error: errorData };
    }
  }

  // Generic HTTP methods
  public async get<T>(endpoint: string): Promise<ApiResponse<T>> {
    return this.fetchWithAuth(endpoint, { method: 'GET' });
  }

  public async post<T>(endpoint: string, body: unknown): Promise<ApiResponse<T>> { // Changed any to unknown
    return this.fetchWithAuth(endpoint, {
      method: 'POST',
      body: JSON.stringify(body),
    });
  }

  public async put<T>(endpoint: string, body: unknown): Promise<ApiResponse<T>> { // Changed any to unknown
    return this.fetchWithAuth(endpoint, {
      method: 'PUT',
      body: JSON.stringify(body),
    });
  }

  public async delete<T>(endpoint: string, body?: unknown): Promise<ApiResponse<T>> { // Changed any to unknown, made body optional
    return this.fetchWithAuth(endpoint, { method: 'DELETE', body: body ? JSON.stringify(body) : undefined });
  }

  // Specific API methods for backend endpoints
  public async getHealthStatus(): Promise<ApiResponse<{ status: string }>> {
    return this.get('/api/health');
  }

  public async listConversations(tenantId: string): Promise<ApiResponse<Conversation[]>> {
    return this.get(`/api/conversations?tenantId=${tenantId}`);
  }

  public async createConversation(
    tenant_id: string,
    title: string,
    message: string
  ): Promise<ApiResponse<{ conversation: Conversation; initialMessage: Message }>> {
    return this.post('/api/conversations', { tenant_id, title, message });
  }

  public async getMessageHistory(conversationId: string): Promise<ApiResponse<Message[]>> {
    return this.get(`/api/conversations/${conversationId}/messages`);
  }

  public async sendMessage(
    conversationId: string,
    text: string,
    senderType: 'visitor' | 'agent',
    senderId?: string
  ): Promise<ApiResponse<Message>> {
    return this.post(`/api/conversations/${conversationId}/messages`, { text, sender_type: senderType, sender_id: senderId });
  }

  public async updateConversationStatus(conversationId: string, status: string): Promise<ApiResponse<void>> {
    return this.put(`/api/conversations/${conversationId}/status`, { status });
  }

  public async pollForEvents(): Promise<ApiResponse<unknown[]>> { // Changed any[] to unknown[]
    return this.get('/api/poll');
  }
}

const apiClient = new ApiClient();
export default apiClient;
