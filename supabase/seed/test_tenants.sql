-- Seed Test Tenants
INSERT INTO tenants (id, name) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Test Tenant A'),
    ('a0000000-0000-0000-0000-000000000002', 'Test Tenant B'),
    ('a0000000-0000-0000-0000-000000000003', 'Test Tenant C')
ON CONFLICT (id) DO NOTHING;

-- Seed Tenant Memberships for a placeholder user
-- IMPORTANT: Replace '00000000-0000-0000-0000-000000000001' with an actual user_id from your auth.users table
INSERT INTO tenant_memberships (tenant_id, user_id, role) VALUES
    ('a0000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'agent'),
    ('a0000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', 'agent'),
    ('a0000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', 'agent')
ON CONFLICT (tenant_id, user_id) DO NOTHING;
