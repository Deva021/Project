import Image from "next/image";

export default function Home() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-zinc-50 font-sans dark:bg-black">
      <main className="flex w-full max-w-7xl flex-col items-center justify-center py-16 px-8 sm:px-16">
        {/* Hero Section */}
        <section className="flex flex-col items-center text-center py-20">
          <h1 className="text-5xl font-bold tracking-tight text-black dark:text-zinc-50 sm:text-6xl">
            Welcome to Multi-tenant Live Chat
          </h1>
          <p className="mt-6 max-w-3xl text-lg leading-8 text-zinc-600 dark:text-zinc-400">
            Multi-tenant live chat is a comprehensive customer support solution designed to
            streamline communication between businesses and their customers.
            It provides a real-time chat widget for visitors and a powerful
            agent dashboard for efficient conversation management.
          </p>
          <div className="mt-10 flex items-center justify-center gap-x-6">
            <a
              href="/widget"
              className="rounded-md bg-indigo-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
            >
              Try the Chat Widget
            </a>
            <a
              href="/dashboard"
              className="text-sm font-semibold leading-6 text-zinc-900 dark:text-zinc-50"
            >
              Agent Dashboard <span aria-hidden="true">→</span>
            </a>
          </div>
        </section>

        {/* Features Section */}
        <section className="w-full py-20 border-t border-zinc-200 dark:border-zinc-800">
          <h2 className="text-4xl font-bold tracking-tight text-center text-black dark:text-zinc-50 sm:text-5xl">
            Key Features
          </h2>
          <div className="mt-16 grid grid-cols-1 gap-8 md:grid-cols-2 lg:grid-cols-3">
            <div className="flex flex-col items-center text-center p-6 bg-white dark:bg-zinc-900 rounded-lg shadow-md">
              <Image
                src="/window.svg" // Placeholder, assuming an icon for chat widget
                alt="Chat Widget Icon"
                width={64}
                height={64}
                className="mb-4 dark:invert"
              />
              <h3 className="text-xl font-semibold text-black dark:text-zinc-50">
                Real-time Chat Widget
              </h3>
              <p className="mt-2 text-base text-zinc-600 dark:text-zinc-400">
                Allow your website visitors to instantly connect with support
                agents through an intuitive and customizable chat interface.
              </p>
            </div>
            <div className="flex flex-col items-center text-center p-6 bg-white dark:bg-zinc-900 rounded-lg shadow-md">
              <Image
                src="/globe.svg" // Placeholder, assuming an icon for agent dashboard
                alt="Agent Dashboard Icon"
                width={64}
                height={64}
                className="mb-4 dark:invert"
              />
              <h3 className="text-xl font-semibold text-black dark:text-zinc-50">
                Agent Dashboard
              </h3>
              <p className="mt-2 text-base text-zinc-600 dark:text-zinc-400">
                Empower your support team with a centralized dashboard to
                manage conversations, respond to inquiries, and track customer interactions.
              </p>
            </div>
            <div className="flex flex-col items-center text-center p-6 bg-white dark:bg-zinc-900 rounded-lg shadow-md">
              <Image
                src="/file.svg" // Placeholder, assuming an icon for multi-tenant
                alt="Multi-tenant Icon"
                width={64}
                height={64}
                className="mb-4 dark:invert"
              />
              <h3 className="text-xl font-semibold text-black dark:text-zinc-50">
                Multi-Tenant Support
              </h3>
              <p className="mt-2 text-base text-zinc-600 dark:text-zinc-400">
                Designed to support multiple independent organizations or
                clients within a single instance, ensuring data isolation and
                customization.
              </p>
            </div>
          </div>
        </section>

        {/* Embed Widget Section */}
        <section className="w-full py-20 border-t border-zinc-200 dark:border-zinc-800">
          <h2 className="text-4xl font-bold tracking-tight text-center text-black dark:text-zinc-50 sm:text-5xl">
            Embed the Widget
          </h2>
          <p className="mt-6 max-w-3xl text-lg leading-8 text-zinc-600 dark:text-zinc-400 text-center mx-auto">
            Integrate the Multi-tenant live chat widget into your website by adding
            the following script tag to your HTML. Replace `YOUR_TENANT_ID` with
            your unique tenant identifier.
          </p>
          <div className="mt-10 mx-auto w-full max-w-2xl bg-zinc-800 rounded-md p-4 overflow-x-auto">
            <pre>
              <code className="text-sm text-white">
                {`<div id="app-chat-widget" data-tenant-id="YOUR_TENANT_ID"></div>
<script src="http://localhost:3000/widget/bundle.js" async></script>`}
              </code>
            </pre>
          </div>
        </section>

        {/* Multi-Tenant Demonstration */}
        <section className="w-full py-20 border-t border-zinc-200 dark:border-zinc-800">
          <h2 className="text-4xl font-bold tracking-tight text-center text-black dark:text-zinc-50 sm:text-5xl">
            Multi-Tenant Demonstration
          </h2>
          <p className="mt-6 max-w-3xl text-lg leading-8 text-zinc-600 dark:text-zinc-400 text-center mx-auto">
            Multi-tenant live chat supports multiple tenants, allowing each client to have a
            isolated and customized chat experience. Click the links below to
            see how different tenant IDs would load different widget instances.
          </p>
          <div className="mt-10 flex flex-wrap items-center justify-center gap-x-6 gap-y-4">
            <a
              href="/widget?tenantId=tenantA"
              className="rounded-md bg-blue-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600"
            >
              Widget for Tenant A
            </a>
            <a
              href="/widget?tenantId=tenantB"
              className="rounded-md bg-green-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-green-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-green-600"
            >
              Widget for Tenant B
            </a>
            <a
              href="/widget?tenantId=tenantC"
              className="rounded-md bg-purple-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-purple-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-purple-600"
            >
              Widget for Tenant C
            </a>
          </div>
        </section>
      </main>
    </div>
  );
}
