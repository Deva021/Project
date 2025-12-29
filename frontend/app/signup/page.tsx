import Link from 'next/link'
import SignupForm from '@/components/auth/SignupForm'

export default function SignupPage({
  searchParams,
}: {
  searchParams: { error?: string }
}) {
  const error = searchParams.error

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-md space-y-8 bg-white p-8 rounded-xl shadow-lg border border-gray-100">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Create your tenant account
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            Already have an account?{' '}
            <Link
              href="/login"
              className="font-medium text-indigo-600 hover:text-indigo-500 transition-colors"
            >
              Sign in
            </Link>
          </p>
        </div>

        <SignupForm error={error} />
      </div>
    </div>
  )
}
