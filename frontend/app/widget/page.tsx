'use client';

import { useState } from 'react';
import ChatWindow from '../../components/ChatWindow';

export default function WidgetPage() {
  const [isOpen, setIsOpen] = useState(true);

  return (
    <div className="fixed bottom-4 right-4">
      {isOpen && (
        <div className="w-80 h-96 bg-white rounded-lg shadow-lg flex flex-col">
          <ChatWindow tenantId="test-tenant" />
        </div>
      )}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="mt-4 px-4 py-2 bg-blue-500 text-white rounded-full"
      >
        {isOpen ? 'Close' : 'Chat'}
      </button>
    </div>
  );
}
