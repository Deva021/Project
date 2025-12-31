-- Fix schema inconsistencies in conversations and messages tables

-- 1. Ensure conversations table has both 'title' and 'status'
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='conversations' AND column_name='title') THEN
        ALTER TABLE public.conversations ADD COLUMN title TEXT;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='conversations' AND column_name='status') THEN
        ALTER TABLE public.conversations ADD COLUMN status TEXT NOT NULL CHECK (status IN ('OPEN', 'ACTIVE', 'PENDING', 'CLOSED')) DEFAULT 'OPEN';
    END IF;
END $$;

-- 2. Ensure messages table has correct columns (sender_id, sender_type, text)
-- Note: Some migrations used 'user_id' and 'content', while others used 'sender_id', 'sender_type', and 'text'.
-- We will unify to the latter as expected by the Java code.

DO $$ 
BEGIN 
    -- Rename user_id to sender_id if it exists and sender_id doesn't
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='messages' AND column_name='user_id') 
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='messages' AND column_name='sender_id') THEN
        ALTER TABLE public.messages RENAME COLUMN user_id TO sender_id;
    END IF;

    -- Add sender_id if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='messages' AND column_name='sender_id') THEN
        ALTER TABLE public.messages ADD COLUMN sender_id UUID REFERENCES auth.users(id) ON DELETE SET NULL;
    END IF;

    -- Add sender_type if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='messages' AND column_name='sender_type') THEN
        ALTER TABLE public.messages ADD COLUMN sender_type TEXT NOT NULL CHECK (sender_type IN ('visitor', 'agent')) DEFAULT 'visitor';
    END IF;

    -- Rename content to text if it exists and text doesn't
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='messages' AND column_name='content') 
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='messages' AND column_name='text') THEN
        ALTER TABLE public.messages RENAME COLUMN content TO text;
    END IF;

    -- Add text if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='messages' AND column_name='text') THEN
        ALTER TABLE public.messages ADD COLUMN text TEXT;
    END IF;
END $$;
