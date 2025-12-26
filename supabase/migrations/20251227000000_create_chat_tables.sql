-- Create conversations table
CREATE TABLE IF NOT EXISTS public.conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES public.tenants(id) ON DELETE CASCADE,
    status TEXT NOT NULL CHECK (status IN ('OPEN', 'ACTIVE', 'PENDING', 'CLOSED')) DEFAULT 'OPEN',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Create messages table
CREATE TABLE IF NOT EXISTS public.messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES public.conversations(id) ON DELETE CASCADE,
    sender_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    sender_type TEXT NOT NULL CHECK (sender_type IN ('visitor', 'agent')),
    text TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Enable RLS
ALTER TABLE public.conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;

-- RLS Policies for conversations
CREATE POLICY "Agents can view conversations for their tenant"
ON public.conversations
FOR SELECT
TO authenticated
USING (
    tenant_id IN (
        SELECT tenant_id FROM public.tenants_users WHERE user_id = auth.uid()
    )
);

CREATE POLICY "Agents can update conversations for their tenant"
ON public.conversations
FOR UPDATE
TO authenticated
USING (
    tenant_id IN (
        SELECT tenant_id FROM public.tenants_users WHERE user_id = auth.uid()
    )
);

-- RLS Policies for messages
CREATE POLICY "Agents can view messages for their tenant's conversations"
ON public.messages
FOR SELECT
TO authenticated
USING (
    conversation_id IN (
        SELECT id FROM public.conversations WHERE tenant_id IN (
            SELECT tenant_id FROM public.tenants_users WHERE user_id = auth.uid()
        )
    )
);

CREATE POLICY "Agents can insert messages for their tenant's conversations"
ON public.messages
FOR INSERT
TO authenticated
WITH CHECK (
    conversation_id IN (
        SELECT id FROM public.conversations WHERE tenant_id IN (
            SELECT tenant_id FROM public.tenants_users WHERE user_id = auth.uid()
        )
    )
);
