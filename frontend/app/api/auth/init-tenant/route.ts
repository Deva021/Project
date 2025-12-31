import { createClient as createAdminClient } from '@supabase/supabase-js'
import { NextResponse } from 'next/server'

export async function POST(request: Request) {
  try {
    const { userId, tenantName, email } = await request.json()

    if (!userId || !tenantName) {
      return NextResponse.json({ error: 'Missing required fields' }, { status: 400 })
    }

    const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL
    const supabaseServiceKey = process.env.SUPABASE_SERVICE_ROLE_KEY

    if (!supabaseUrl || !supabaseServiceKey) {
      return NextResponse.json({ error: 'Missing environment variables' }, { status: 500 })
    }

    const adminAuthClient = createAdminClient(supabaseUrl, supabaseServiceKey, {
      auth: {
        autoRefreshToken: false,
        persistSession: false,
      },
    })

    // 1. Create Tenant
    const { data: tenantData, error: tenantError } = await adminAuthClient
      .from('tenants')
      .insert({ name: tenantName })
      .select()
      .single()

    if (tenantError) {
      console.error('Tenant creation failed:', tenantError)
      return NextResponse.json({ error: 'Tenant creation failed: ' + tenantError.message }, { status: 500 })
    }

    // 2. Create Membership
    const { error: membershipError } = await adminAuthClient
      .from('tenant_memberships')
      .insert({
        tenant_id: tenantData.id,
        user_id: userId,
        role: 'owner',
      })

    if (membershipError) {
      console.error('Membership creation failed:', membershipError)
      return NextResponse.json({ error: 'Membership creation failed: ' + membershipError.message }, { status: 500 })
    }

    // 3. Update User Metadata with Tenant ID
    const { error: updateError } = await adminAuthClient.auth.admin.updateUserById(
      userId,
      { app_metadata: { tenant_id: tenantData.id } }
    )

    if (updateError) {
      console.error('Failed to update user metadata:', updateError)
      // We continue even if metadata update fails, but it's logged
    }

    return NextResponse.json({ 
      success: true, 
      tenantId: tenantData.id 
    })

  } catch (err: unknown) {
    const errorMessage = err instanceof Error ? err.message : 'An unexpected error occurred'
    console.error('Init tenant error:', errorMessage)
    return NextResponse.json({ error: errorMessage }, { status: 500 })
  }
}
