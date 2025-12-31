-- Add updated_at to conversations
ALTER TABLE public.conversations ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT now();

-- Trigger to update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

DROP TRIGGER IF EXISTS update_conversations_updated_at ON public.conversations;
CREATE TRIGGER update_conversations_updated_at
    BEFORE UPDATE ON public.conversations
    FOR EACH ROW
    EXECUTE PROCEDURE update_updated_at_column();

-- Function to delete closed conversations older than 24 hours
CREATE OR REPLACE FUNCTION delete_old_closed_conversations()
RETURNS void AS $$
BEGIN
    DELETE FROM public.conversations
    WHERE status = 'CLOSED'
      AND updated_at < now() - interval '24 hours';
END;
$$ LANGUAGE plpgsql;
