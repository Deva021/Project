import { createClient } from '@/utils/supabase/server'
import { redirect } from 'next/navigation'

export default async function DashboardPage() {
  const supabase = await createClient()

  const {
    data: { user },
  } = await supabase.auth.getUser()

  if (!user) {
    return redirect('/login')
  }

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gray-50 p-4">
      <div className="w-full max-w-4xl bg-white p-8 rounded-xl shadow-lg border border-gray-100">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
          <form action="/api/auth/signout" method="POST">
            <button
              type="submit"
              className="px-4 py-2 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 transition-colors"
            >
              Sign out
            </button>
          </form>
        </div>
        
        <div className="bg-indigo-50 p-6 rounded-lg border border-indigo-100">
          <h2 className="text-xl font-semibold text-indigo-900 mb-2">Welcome, {user.email}!</h2>
          <p className="text-indigo-700">
            You are successfully authenticated. This is a protected route.
          </p>
        </div>

        <div className="mt-8 grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="p-6 border border-gray-200 rounded-lg">
            <h3 className="font-bold text-gray-900 mb-2">User ID</h3>
            <code className="text-sm bg-gray-100 p-1 rounded">{user.id}</code>
          </div>
          <div className="p-6 border border-gray-200 rounded-lg">
            <h3 className="font-bold text-gray-900 mb-2">Auth Status</h3>
            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
              Authenticated
            </span>
          </div>
        </div>
      </div>
    </div>
  )
}
