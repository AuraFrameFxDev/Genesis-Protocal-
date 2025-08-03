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
     * Handles client binding requests to the service.
     *
     * Always returns null to indicate that this service does not support binding.
     *
     * @return Null, indicating binding is not permitted.
     */
    override fun onBind(_intent: Intent?): IBinder? {
        // Not designed for binding; implement if needed
        return null
    }

    /**
     * Handles a start request for the service and specifies its restart behavior.
     *
     * Always returns `START_NOT_STICKY`, so the system will not recreate the service if it is killed after this method returns.
     *
     * @return `START_NOT_STICKY` to indicate the service should not be restarted automatically.
     */
    override fun onStartCommand(_intent: Intent?, _flags: Int, _startId: Int): Int {
        // Implement service logic here (e.g., start sync tasks)
        return START_NOT_STICKY
    }
}
