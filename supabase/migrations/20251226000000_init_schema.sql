-- 1. Table Schema

-- tenants: Stores information about each tenant (organization or customer account).
CREATE TABLE tenants (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- tenant_memberships: Links users to the tenants they belong to.
CREATE TABLE tenant_memberships (
  tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  role TEXT NOT NULL DEFAULT 'member', -- For future use (e.g., 'admin', 'member')
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  PRIMARY KEY (tenant_id, user_id)
);

-- conversations: Stores conversation threads, which are scoped to a single tenant.
CREATE TABLE conversations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  title TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- messages: Stores individual chat messages within a conversation.
CREATE TABLE messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  content TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Row-Level Security (RLS) Policies

-- Enable RLS on all tables
ALTER TABLE tenants ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_memberships ENABLE ROW LEVEL SECURITY;
ALTER TABLE conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;

-- Helper Function: Check if a user is a member of a specific tenant.
CREATE OR REPLACE FUNCTION is_member_of_tenant(p_tenant_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
  RETURN EXISTS (
    SELECT 1
    FROM tenant_memberships
    WHERE tenant_id = p_tenant_id AND user_id = auth.uid()
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Policies for tenants: Users can see tenants they are a member of.
CREATE POLICY "Allow read access to own tenants"
ON tenants FOR SELECT
USING (is_member_of_tenant(id));

-- Policies for tenant_memberships: Users can see their own memberships and other memberships within their tenants.
CREATE POLICY "Allow read access to memberships in own tenants"
ON tenant_memberships FOR SELECT
USING (is_member_of_tenant(tenant_id));

-- Policies for conversations: Users can only read/write conversations within their active tenants.
CREATE POLICY "Allow full access to conversations in own tenants"
ON conversations FOR ALL
USING (is_member_of_tenant(tenant_id));

-- Policies for messages: Users can only read/write messages if they are part of the conversation's tenant.
CREATE POLICY "Allow full access to messages in own tenants"
ON messages FOR ALL
USING (
  is_member_of_tenant(
    (SELECT tenant_id FROM conversations WHERE id = messages.conversation_id)
  )
);
