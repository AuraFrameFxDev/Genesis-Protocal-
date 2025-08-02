package dev.aurakai.auraframefx.oracledrive.storage

import dev.aurakai.auraframefx.oracledrive.DriveFile
import dev.aurakai.auraframefx.oracledrive.FileMetadata
import dev.aurakai.auraframefx.oracledrive.FileResult
import dev.aurakai.auraframefx.oracledrive.StorageOptimization
import dev.aurakai.auraframefx.oracledrive.SyncConfiguration

/**
* Cloud storage provider interface for Oracle Drive
* Handles AI-optimized storage operations with consciousness integration
*/
interface CloudStorageProvider {

    /**
     * Performs intelligent storage optimization using AI-driven algorithms and compression techniques.
     *
     * @return A [StorageOptimization] object containing metrics and results of the optimization process.
     */
    suspend fun optimizeStorage(): StorageOptimization

    /**
     * Applies AI-driven compression to optimize a file for upload.
     *
     * @param file The file to be optimized.
     * @return The optimized file ready for upload.
     */
    suspend fun optimizeForUpload(file: DriveFile): DriveFile

    /****
     * Uploads an optimized file to cloud storage with associated metadata and access controls.
     *
     * @param file The file to be uploaded, expected to be optimized for cloud storage.
     * @param metadata Metadata and access control information to associate with the uploaded file.
     * @return The result of the upload operation, including status and any relevant details.
     */
    suspend fun uploadFile(file: DriveFile, metadata: FileMetadata): FileResult

    /**
     * Downloads a file from cloud storage using its identifier.
     *
     * @param fileId The unique identifier of the file to download.
     * @return A [FileResult] containing the status and details of the download operation.
     */
    suspend fun downloadFile(fileId: String): FileResult

    /**
     * Deletes a file from cloud storage by its identifier.
     *
     * @param fileId The unique identifier of the file to delete.
     * @return The result of the deletion operation.
     */
    suspend fun deleteFile(fileId: String): FileResult

    /****
     * Performs intelligent file synchronization using AI-driven optimization based on the provided configuration.
     *
     * @param config The synchronization configuration specifying sync parameters and rules.
     * @return The result of the synchronization operation, including status and details.
     */
    suspend fun intelligentSync(config: SyncConfiguration): FileResult
}
