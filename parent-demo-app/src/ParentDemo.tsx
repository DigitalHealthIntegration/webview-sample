import React, { useState, useEffect, useMemo } from 'react';

// Define the shape of the data we expect from the child app
// We define this locally to simulate an external application that doesn't share code
interface SessionData {
  userId: string;
  files: any[];
  timestamp: string;
}

interface MessageEventData {
  type: "SESSION_DATA" | "FINISH_SESSION";
  data: SessionData | null;
}

export const ParentDemo: React.FC = () => {
  // State for the input form
  const [userId, setUserId] = useState<string>("");
  
  const [aggregatorUrl, setAggregatorUrl] = useState<string>(window.location.origin + "/setup"); 
  
  // State to manage the session lifecycle
  const [isSessionActive, setIsSessionActive] = useState<boolean>(false);
  const [lastSessionData, setLastSessionData] = useState<SessionData | null>(null);

  // 1. Setup the Event Listener
  useEffect(() => {
    const handleMessage = (event: MessageEvent) => {
      // In a production environment, we should verify event.origin
      if (event.origin !== new URL(aggregatorUrl).origin) return;

      const message = event.data as MessageEventData;

      // Handle "SESSION_DATA" - The app is sending us the results
      if (message.type === "SESSION_DATA" && message.data) {
        console.log("Parent received session data:", message.data);
        setLastSessionData(message.data);
      }

      // Handle "FINISH_SESSION" - The app is requesting to close
      if (message.type === "FINISH_SESSION") {
        console.log("Parent received finish signal. Closing iframe.");
        setIsSessionActive(false);
      }
    };

    // Attach listener
    window.addEventListener("message", handleMessage);

    // Cleanup listener on unmount
    return () => {
      window.removeEventListener("message", handleMessage);
    };
  }, [aggregatorUrl]);

  const startSession = () => {
    if (!userId) {
      alert("Please enter a User ID");
      return;
    }
    // Clear previous data and open the iframe
    setLastSessionData(null);
    setIsSessionActive(true);
  };

  // Construct URL with userId param safely
  const iframeSrc = useMemo(() => {
    try {
      const url = new URL(aggregatorUrl);
      url.searchParams.set("userId", userId);
      return url.toString();
    } catch (e) {
      return aggregatorUrl;
    }
  }, [aggregatorUrl, userId]);

  return (
    <div className="min-h-screen bg-zinc-100 flex flex-col font-sans text-zinc-900">
      <header className="bg-zinc-800 text-white p-4 shadow-md">
        <h1 className="text-xl font-bold">Impact Health - Parent Demo</h1>
      </header>

      <main className="flex-1 flex justify-center items-center p-8">
        {!isSessionActive ? (
          /* STATE 1: ID Entry Screen */
          <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-md border border-zinc-200">
            <h2 className="text-2xl font-bold mb-6 text-zinc-800">Start New Session</h2>
            
            <div className="mb-4">
              <label className="block mb-2 font-bold text-zinc-700">Camera App URL:</label>
              <input 
                className="w-full p-2 border border-zinc-300 rounded focus:ring-2 focus:ring-blue-500 outline-none"
                type="text" 
                value={aggregatorUrl} 
                onChange={(e) => setAggregatorUrl(e.target.value)}
              />
            </div>

            <div className="mb-6">
              <label className="block mb-2 font-bold text-zinc-700">Patient / User ID:</label>
              <input 
                className="w-full p-2 border border-zinc-300 rounded focus:ring-2 focus:ring-blue-500 outline-none"
                type="text" 
                value={userId} 
                onChange={(e) => setUserId(e.target.value)} 
                placeholder="Enter ID..."
              />
            </div>

            <button 
              className="w-full p-3 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors font-bold shadow-sm"
              onClick={startSession}
            >
              Launch Camera Aggregator
            </button>

            {lastSessionData && (
              <div className="mt-8 p-4 bg-green-50 text-green-900 rounded border border-green-200 animate-in fade-in slide-in-from-bottom-4">
                <h3 className="font-bold mb-2 text-lg">Last Session Complete</h3>
                <div className="text-sm space-y-1">
                  <p><strong>User:</strong> {lastSessionData.userId}</p>
                  <p><strong>Files:</strong> {lastSessionData.files?.length || 0} captured</p>
                  <p><strong>Time:</strong> {new Date(lastSessionData.timestamp).toLocaleString()}</p>
                </div>
              </div>
            )}
          </div>
        ) : (
          /* STATE 2: Active Session (Iframe) */
          <div className="w-full h-[85vh] bg-white shadow-2xl rounded-lg overflow-hidden flex flex-col border border-zinc-300 animate-in zoom-in-95 duration-300">
            <div className="p-3 bg-zinc-100 border-b border-zinc-300 flex justify-between items-center">
              <span className="text-zinc-700">Session Active for User: <strong>{userId}</strong></span>
              <button 
                className="px-4 py-1.5 bg-red-500 text-white rounded hover:bg-red-600 text-sm font-medium transition-colors"
                onClick={() => setIsSessionActive(false)}
              >
                Close
              </button>
            </div>
            <iframe
              src={iframeSrc}
              title="Camera Aggregator"
              className="flex-1 w-full border-none bg-black"
              allow="camera; microphone; fullscreen; display-capture"
            />
          </div>
        )}
      </main>
    </div>
  );
};