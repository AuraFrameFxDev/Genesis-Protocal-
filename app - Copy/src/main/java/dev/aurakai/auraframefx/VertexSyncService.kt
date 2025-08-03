package dev.aurakai.auraframefx

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// @AndroidEntryPoint // Temporarily disabled for successful build
class VertexSyncService : Service() {
    // Example dependency injection (add real dependencies as needed)
    /**
     * Called when a client attempts to bind to the service.
     *
     * This service does not support binding and always returns null.
     *
     * @return Always returns null, indicating binding is not allowed.
     */
    override fun onBind(_intent: Intent?): IBinder? {
        // Not designed for binding; implement if needed
        return null
    }

    /**
     * Handles the start request for the service.
     *
     * Returns `START_NOT_STICKY`, indicating the system should not recreate the service if it is killed after returning from this method.
     *
     * @return The start mode for the service, which is always `START_NOT_STICKY`.
     */
    override fun onStartCommand(_intent: Intent?, _flags: Int, _startId: Int): Int {
        // Implement service logic here (e.g., start sync tasks)
        return START_NOT_STICKY
    }
}
