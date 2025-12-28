import { createClient } from '@/utils/supabase/server'
import { createClient as createAdminClient } from '@supabase/supabase-js'
import { NextResponse } from 'next/server'

export async function POST(request: Request) {
  const requestUrl = new URL(request.url)
  const formData = await request.formData()
  const email = String(formData.get('email'))
  const password = String(formData.get('password'))
  const tenantName = String(formData.get('tenantName'))

  // Explicit check for environment variables
  const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL
  const supabaseAnonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY
  const supabaseServiceKey = process.env.SUPABASE_SERVICE_ROLE_KEY

  if (!supabaseUrl || !supabaseAnonKey || !supabaseServiceKey) {
    const missing = [
      !supabaseUrl && 'NEXT_PUBLIC_SUPABASE_URL',
      !supabaseAnonKey && 'NEXT_PUBLIC_SUPABASE_ANON_KEY',
      !supabaseServiceKey && 'SUPABASE_SERVICE_ROLE_KEY'
    ].filter(Boolean).join(', ')
    
    return NextResponse.redirect(
      new URL(`/signup?error=${encodeURIComponent('Missing environment variables: ' + missing)}`, request.url),
      { status: 303 }
    )
  }

  const supabase = await createClient()

  try {
    // 1. Sign up the user
    const { data: authData, error: authError } = await supabase.auth.signUp({
      email,
      password,
      options: {
        emailRedirectTo: `${requestUrl.origin}/api/auth/callback`,
      },
    })

    if (authError) {
      return NextResponse.redirect(
        new URL(`/signup?error=${encodeURIComponent(authError.message)}`, request.url),
        { status: 303 }
      )
    }

    if (!authData.user) {
      return NextResponse.redirect(
        new URL(`/signup?error=${encodeURIComponent('User creation failed')}`, request.url),
        { status: 303 }
      )
    }

    // 2. Create Tenant and Membership (using Admin Client to bypass RLS)
    const adminAuthClient = createAdminClient(
      process.env.NEXT_PUBLIC_SUPABASE_URL!,
      process.env.SUPABASE_SERVICE_ROLE_KEY!,
      {
        auth: {
          autoRefreshToken: false,
          persistSession: false,
        },
      }
    )

    // Create Tenant
    const { data: tenantData, error: tenantError } = await adminAuthClient
      .from('tenants')
      .insert({ name: tenantName })
      .select()
      .single()

    if (tenantError) {
      return NextResponse.redirect(
        new URL(`/signup?error=${encodeURIComponent('Tenant creation failed: ' + tenantError.message)}`, request.url),
        { status: 303 }
      )
    }

    // Create Membership
    const { error: membershipError } = await adminAuthClient
      .from('tenant_memberships')
      .insert({
        tenant_id: tenantData.id,
        user_id: authData.user.id,
        role: 'owner',
      })

    if (membershipError) {
      return NextResponse.redirect(
        new URL(`/signup?error=${encodeURIComponent('Membership creation failed: ' + membershipError.message)}`, request.url),
        { status: 303 }
      )
    }

    return NextResponse.redirect(new URL('/dashboard', request.url), {
      status: 303,
    })
  } catch (err: unknown) {
    const errorMessage = err instanceof Error ? err.message : 'An unexpected error occurred';
    return NextResponse.redirect(
      new URL(`/signup?error=${encodeURIComponent(errorMessage)}`, request.url),
      { status: 303 }
    )
  }
}
