package com.example.app.ipc

// Explicitly import the AIDL interface
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class OracleDriveServiceConnector(private val context: Context) {
    private var auraDriveService: IAuraDriveService? = null
    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        /****
         * Called when the AuraDrive service has been connected.
         *
         * Sets the remote `IAuraDriveService` interface reference and marks the connection as active.
         *
         * @param name The component name of the connected service.
         * @param service The binder interface to the connected service.
         */
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            auraDriveService = IAuraDriveService.Companion.Stub.asInterface(service)
            _isServiceConnected.value = true
        }

        /**
         * Handles disconnection from the AuraDrive service.
         *
         * Clears the remote service reference and updates the connection state to indicate disconnection.
         */
        override fun onServiceDisconnected(name: ComponentName?) {
            auraDriveService = null
            _isServiceConnected.value = false
        }
    }

    /**
     * Binds to the remote AuraDrive service using an explicit intent.
     *
     * Sets the connection state to false if a security exception occurs during binding.
     */
    fun bindService() {
        val intent = Intent().apply {
            component = ComponentName(
                "com.genesis.ai.app",
                "com.genesis.ai.app.service.AuraDriveServiceImpl"
            )
        }
        try {
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        } catch (e: SecurityException) {
            _isServiceConnected.value = false
        }
    }

    /**
     * Unbinds from the AuraDrive service and updates the connection state to disconnected.
     *
     * Any exceptions during unbinding are silently ignored.
     */
    fun unbindService() {
        try {
            context.unbindService(serviceConnection)
        } catch (_: Exception) {
        }
        auraDriveService = null
        _isServiceConnected.value = false
    }

    /**
     * Retrieves the current status from the remote AuraDrive service.
     *
     * @return The status string reported by the remote service, or null if the service is unavailable or a RemoteException occurs.
     */
    suspend fun getStatusFromOracleDrive(): String? = withContext(Dispatchers.IO) {
        try {
            auraDriveService?.getOracleDriveStatus()
        } catch (e: RemoteException) {
            null
        }
    }

    /**
         * Requests the remote AuraDrive service to toggle its LSPosed module.
         *
         * The `packageName` and `enable` parameters are not used by the remote service.
         *
         * @return "Success" if the module was toggled successfully, "Failed" if the operation did not succeed, or null if the service is unavailable or a remote exception occurs.
         */
    suspend fun toggleModuleOnOracleDrive(packageName: String, enable: Boolean): String? =
        withContext(Dispatchers.IO) {
            try {
                val result = auraDriveService?.toggleLSPosedModule()
                if (result == true) "Success" else "Failed"
            } catch (e: RemoteException) {
                null
            }
        }

    /**
     * Retrieves a detailed internal status report from the remote AuraDrive service.
     *
     * @return The detailed status string, or null if the service is unavailable or a RemoteException occurs.
     */
    suspend fun getDetailedInternalStatus(): String? = withContext(Dispatchers.IO) {
        try {
            auraDriveService?.getDetailedInternalStatus()
        } catch (e: RemoteException) {
            null
        }
    }

    /**
     * Retrieves the internal diagnostics log from the remote AuraDrive service as a single string.
     *
     * The log entries are joined with newline characters. Returns null if the service is unavailable or a remote exception occurs.
     *
     * @return The diagnostics log as a newline-separated string, or null if not available.
     */
    suspend fun getInternalDiagnosticsLog(): String? = withContext(Dispatchers.IO) {
        try {
            val logs = auraDriveService?.getInternalDiagnosticsLog()
            logs?.joinToString("\n")
        } catch (e: RemoteException) {
            null
        }
    }
}
