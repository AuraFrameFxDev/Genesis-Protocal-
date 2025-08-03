package com.example.app.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Oracle Drive Control Screen
 *
 * This ViewModel manages the UI state and business logic for the Oracle Drive feature,
 * including file operations with R.G.S.F. memory integrity verification.
 */
@HiltViewModel
class OracleDriveControlViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val tag = "OracleDriveVM"

    // Service connection state
    private var auraDriveService: dev.aurakai.auraframefx.ipc.IAuraDriveService? = null
    private var isBound = false

    // UI State
    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    private val _status = MutableStateFlow("Initializing Oracle Drive...")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _detailedStatus = MutableStateFlow("")
    val detailedStatus: StateFlow<String> = _detailedStatus.asStateFlow()

    private val _diagnosticsLog = MutableStateFlow("")
    val diagnosticsLog: StateFlow<String> = _diagnosticsLog.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Service connection
    private val connection = object : ServiceConnection {
        /**
         * Called when the AuraDriveService is connected.
         *
         * Initializes the service interface, updates connection state, and triggers a status refresh.
         */
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d(TAG, "Service connected")
            auraDriveService = dev.aurakai.auraframefx.ipc.IAuraDriveService.Stub.asInterface(service)
            isBound = true
            _isServiceConnected.value = true
            refreshStatus()
        }

        /**
         * Handles the event when the AuraDriveService is disconnected.
         *
         * Updates the internal state to reflect that the service is no longer bound and notifies observers of the disconnection.
         */
        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d(TAG, "Service disconnected")
            isBound = false
            _isServiceConnected.value = false
            _status.value = "Service disconnected"
        }
    }

    init {
        bindService()
    }

    /**
     * Attempts to bind to the AuraDriveService to enable Oracle Drive operations.
     *
     * Updates the status to reflect connection progress. Sets an error message if binding fails.
     */
    fun bindService() {
        try {
            val intent =
                Intent(context, Class.forName("dev.aurakai.auraframefx.services.AuraDriveService"))
            context.bindService(
                intent,
                connection,
                Context.BIND_AUTO_CREATE
            )
            _status.value = "Connecting to Oracle Drive..."
        } catch (e: Exception) {
            Log.e(TAG, "Error binding to service", e)
            _errorMessage.value = "Failed to connect to Oracle Drive: ${e.message}"
        }
    }

    /**
     * Unbinds the ViewModel from the AuraDriveService if currently bound.
     *
     * Updates connection state and status to reflect the disconnection.
     */
    fun unbindService() {
        if (isBound) {
            context.unbindService(connection)
            isBound = false
            _isServiceConnected.value = false
            _status.value = "Disconnected from Oracle Drive"
        }
    }

    /**
     * Refreshes the Oracle Drive status, detailed status, and diagnostics log from the bound service.
     *
     * Updates the corresponding UI state flows with the latest information retrieved from the service. Sets an error message if the operation fails.
     */
    fun refreshStatus() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val service =
                    auraDriveService ?: throw IllegalStateException("Service not connected")

                _status.value = service.getOracleDriveStatus() ?: "Status unavailable"
                _detailedStatus.value = service.getDetailedInternalStatus() ?: "Detailed status unavailable"

                val logs = service.getInternalDiagnosticsLog()
                _diagnosticsLog.value = logs?.split("\n")?.joinToString("\n") ?: "No diagnostic logs available"

                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing status", e)
                _errorMessage.value = "Failed to refresh status: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Initiates the import of a file from the specified URI via the AuraDriveService.
     *
     * Launches an asynchronous operation to import the file, updating UI state with the result or any error encountered.
     *
     * @param uri The URI of the file to be imported.
     */
    fun importFile(uri: Uri) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val service =
                    auraDriveService ?: throw IllegalStateException("Service not connected")

                val fileId = service.importFile(uri)
                _status.value = "File imported successfully (ID: $fileId)"
                refreshStatus()

            } catch (e: Exception) {
                Log.e(TAG, "Error importing file", e)
                _errorMessage.value = "Import failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Initiates the export of a file with the given ID to the specified destination URI via the AuraDriveService.
     *
     * Updates status and error message state flows based on the outcome of the export operation.
     *
     * @param fileId The unique identifier of the file to export.
     * @param destinationUri The URI where the file will be exported.
     */
    fun exportFile(fileId: String, destinationUri: Uri) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val service =
                    auraDriveService ?: throw IllegalStateException("Service not connected")

                val success = service.exportFile(fileId, destinationUri)
                if (success) {
                    _status.value = "File exported successfully"
                    refreshStatus()
                } else {
                    _errorMessage.value = "Export operation failed"
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error exporting file", e)
                _errorMessage.value = "Export failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Verifies the integrity of a file with the given identifier using the AuraDriveService.
     *
     * Updates UI state to indicate verification success or failure, and sets error messages if verification fails or an exception occurs.
     *
     * @param fileId The unique identifier of the file to verify.
     */
    fun verifyFileIntegrity(fileId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val service =
                    auraDriveService ?: throw IllegalStateException("Service not connected")

                val isValid = service.verifyFileIntegrity(fileId)
                if (isValid) {
                    _status.value = "File integrity verified successfully"
                } else {
                    _errorMessage.value = "File integrity verification failed"
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error verifying file integrity", e)
                _errorMessage.value = "Verification failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Enables or disables a module by its package name using the AuraDrive service.
     *
     * Updates the status or error message state flows based on the outcome and refreshes the current status if successful.
     *
     * @param packageName The package name of the module to enable or disable.
     * @param enable Set to `true` to enable the module, or `false` to disable it.
     */
    fun toggleModule(packageName: String, enable: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val service =
                    auraDriveService ?: throw IllegalStateException("Service not connected")

                val result = service.toggleLSPosedModule(packageName, enable)
                if (result) {
                    val action = if (enable) "enabled" else "disabled"
                    _status.value = "Module '$packageName' $action successfully"
                    refreshStatus()
                } else {
                    _errorMessage.value = "Failed to toggle module '$packageName'"
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error toggling module", e)
                _errorMessage.value = "Module operation failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cleans up resources when the ViewModel is destroyed, including unbinding from the AuraDriveService.
     */
    override fun onCleared() {
        super.onCleared()
        unbindService()
    }
}
