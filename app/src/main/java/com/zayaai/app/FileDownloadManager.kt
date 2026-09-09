package com.zayaai.app

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

/**
 * Utility to download files via Android DownloadManager and open them.
 * Saves to: Downloads/AI_ZAYA/
 */
object FileDownloadHelper {

    /**
     * Start a download using DownloadManager.
     * @param context   Application or Activity context
     * @param fileUrl   The URL of the .pptx file
     * @param fileName  e.g. "presentation_1710812345678.pptx"
     * @param onComplete Callback invoked when download finishes (with downloadId and success)
     * @return downloadId (or -1 on failure)
     */
    fun downloadFile(
        context: Context,
        fileUrl: String,
        fileName: String,
        onComplete: (downloadId: Long, success: Boolean) -> Unit
    ): Long {
        return try {
            val uri = Uri.parse(fileUrl)
            val request = DownloadManager.Request(uri).apply {
                setTitle("AI ZAYA â€” Downloading PPT")
                setDescription("Downloading $fileName")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "AI_ZAYA/$fileName"
                )
                setMimeType("application/vnd.openxmlformats-officedocument.presentationml.presentation")
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = dm.enqueue(request)

            Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()

            // Register receiver for download complete
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                    if (id == downloadId) {
                        val query = DownloadManager.Query().setFilterById(downloadId)
                        val cursor = dm.query(query)
                        if (cursor != null && cursor.moveToFirst()) {
                            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            val status = cursor.getInt(statusIndex)
                            val success = status == DownloadManager.STATUS_SUCCESSFUL
                            if (success) {
                                Toast.makeText(context, "Download completed", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                            }
                            onComplete(downloadId, success)
                            cursor.close()
                        }
                        try {
                            context.unregisterReceiver(this)
                        } catch (_: Exception) {}
                    }
                }
            }

            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_EXPORTED
            )

            downloadId
        } catch (e: Exception) {
            Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            onComplete(-1, false)
            -1
        }
    }

    /**
     * Open a downloaded .pptx file using an external viewer (e.g. PowerPoint, WPS).
     */
    fun openDownloadedFile(context: Context, fileName: String) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "AI_ZAYA/$fileName")

            if (!file.exists()) {
                Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
                return
            }

            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } else {
                Uri.fromFile(file)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.openxmlformats-officedocument.presentationml.presentation")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Fallback: let the system chooser handle it
                context.startActivity(Intent.createChooser(intent, "Open with"))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}


