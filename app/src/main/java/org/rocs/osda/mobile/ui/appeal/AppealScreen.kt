package org.rocs.osda.mobile.ui.appeal

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.io.File
import org.rocs.osda.mobile.data.model.Appeal
import org.rocs.osda.mobile.ui.common.OsdaCard
import org.rocs.osda.mobile.ui.common.PrimaryButton
import org.rocs.osda.mobile.ui.common.FilterPill
import org.rocs.osda.mobile.ui.common.StatCard
import org.rocs.osda.mobile.ui.common.StatusColors
import org.rocs.osda.mobile.ui.common.RefreshWhileVisible
import org.rocs.osda.mobile.ui.common.StatusPill
import org.rocs.osda.mobile.ui.common.toDisplayStatus
import org.rocs.osda.mobile.ui.theme.OsdaTokens
import org.rocs.osda.mobile.util.AppealCaptureFile
import org.rocs.osda.mobile.util.formatDateTime
import org.rocs.osda.mobile.util.resolvePickedFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppealScreen(viewModel: AppealViewModel, onSubmitted: () -> Unit = {}) {
    val state by viewModel.uiState.collectAsState()

    if (viewModel.isFilingMode) {
        FileAppealContent(viewModel, onSubmitted)
    } else {
        MyAppealsContent(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileAppealContent(viewModel: AppealViewModel, onSubmitted: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val offenseRecord = state.records.firstOrNull { it.recordId == state.selectedRecordId }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var pendingCaptureFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val file = pendingCaptureFile
        if (success && file != null) {
            viewModel.uploadAttachmentFromFile(file, file.name, "image/jpeg")
        }
    }

    fun startCameraCapture() {
        val (file, uri) = AppealCaptureFile.create(context)
        pendingCaptureFile = file
        viewModel.onAttachmentSelected(file.name)
        cameraLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startCameraCapture()
    }

    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val picked = withContext(Dispatchers.IO) { resolvePickedFile(context, uri) }
                    viewModel.onAttachmentSelected(picked.fileName)
                    viewModel.uploadAttachmentFromBytes(picked.bytes, picked.fileName, picked.contentType)
                } catch (e: Exception) {
                    viewModel.onAttachmentReadError()
                }
            }
        }
    }

    LaunchedEffect(state.submitSuccess) {
        if (state.submitSuccess) {
            delay(900)
            onSubmitted()
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = viewModel::load,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Column(modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)) {
                Text("File a New Appeal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Request a review of an offense on your record",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OsdaCard(modifier = Modifier.padding(bottom = 20.dp)) {
                Text("Offense", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    if (offenseRecord != null) "${offenseRecord.offense.offense} • ${offenseRecord.dateOfViolation}" else "Offense not found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (state.submitSuccess) {
                    Text(
                        "Appeal submitted successfully. Returning to the offense...",
                        color = OsdaTokens.green,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                } else {
                    Text("Message *", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                    OutlinedTextField(
                        value = state.message,
                        onValueChange = viewModel::onMessageChange,
                        placeholder = { Text("Explain why you're appealing this offense...") },
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )

                    Text("Attach Appeal Letter (optional)", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))

                    if (state.attachmentFileName == null) {
                        state.attachmentError?.let {
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                                            PackageManager.PERMISSION_GRANTED
                                    if (granted) startCameraCapture() else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Take Photo")
                            }
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    filePickerLauncher.launch(
                                        arrayOf(
                                            "application/pdf",
                                            "application/msword",
                                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                            "image/*"
                                        )
                                    )
                                }
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Choose File")
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                Text(state.attachmentFileName ?: "", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                when {
                                    state.isUploadingAttachment -> Text(
                                        "Uploading...", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    state.attachmentError != null -> Text(
                                        state.attachmentError ?: "", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    state.attachmentDocumentId != null -> Text(
                                        "Uploaded", style = MaterialTheme.typography.labelSmall, color = OsdaTokens.green
                                    )
                                }
                            }
                            TextButton(onClick = { viewModel.clearAttachment() }) {
                                Text("Remove")
                            }
                        }

                        if (state.attachmentLooksUnreadable && state.attachmentDocumentId != null) {
                            Text(
                                "We couldn't clearly read this file. You can still submit, but consider retaking the photo or choosing a clearer copy.",
                                color = OsdaTokens.amber,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }

                    state.submitError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                    }

                    Spacer(Modifier.height(16.dp))
                    PrimaryButton(
                        text = if (state.isSubmitting) "Submitting..." else "Submit Appeal",
                        enabled = !state.isSubmitting && !state.isUploadingAttachment,
                        onClick = {
                            if (viewModel.validateBeforeConfirm()) {
                                showConfirmDialog = true
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Submit this appeal?") },
            text = { Text("Once submitted, this appeal will be sent to the Prefect for review and can't be edited.") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    viewModel.submit()
                }) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyAppealsContent(viewModel: AppealViewModel) {
    val state by viewModel.uiState.collectAsState()
    RefreshWhileVisible(onRefresh = viewModel::refresh)

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = viewModel::load,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Column(modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)) {
                Text("My Appeals", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Create and file offense appeals",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                StatCard(state.totalCount.toString(), "Total Filed", MaterialTheme.colorScheme.onBackground, Modifier.weight(1f))
                StatCard(state.pendingCount.toString(), "Pending", OsdaTokens.amber, Modifier.weight(1f))
                StatCard(state.approvedCount.toString(), "Approved", OsdaTokens.green, Modifier.weight(1f))
                StatCard(state.deniedCount.toString(), "Denied", OsdaTokens.red, Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                FilterPill("All", state.filter == AppealFilter.ALL) { viewModel.setFilter(AppealFilter.ALL) }
                FilterPill("Pending", state.filter == AppealFilter.PENDING) { viewModel.setFilter(AppealFilter.PENDING) }
                FilterPill("Approved", state.filter == AppealFilter.APPROVED) { viewModel.setFilter(AppealFilter.APPROVED) }
                FilterPill("Denied", state.filter == AppealFilter.DENIED) { viewModel.setFilter(AppealFilter.DENIED) }
            }

            when {
                state.isLoading && state.appeals.isEmpty() -> Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = MaterialTheme.colorScheme.primary)
                }
                state.error != null -> Text(state.error ?: "Something went wrong.", color = MaterialTheme.colorScheme.error)
                state.filteredAppeals.isEmpty() -> Text(
                    "No appeals to show.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.filteredAppeals) { appeal -> AppealHistoryCard(appeal) }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AppealHistoryCard(appeal: Appeal) {
    val (fg, bg) = StatusColors.forAppeal(appeal.status)
    OsdaCard {
        Text(
            "APPEAL ID: AP-${appeal.appealId.toString().padStart(4, '0')}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                appeal.record?.offense?.offense ?: "Offense",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            StatusPill(appeal.status.toDisplayStatus(), fg, bg)
        }
        Text(
            "Reason:",
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            appeal.message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            "Submitted ${formatDateTime(appeal.dateFiled)}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            "Date of Resolution: ${if (appeal.dateProcessed == null) "Not yet resolved" else formatDateTime(appeal.dateProcessed)}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 2.dp)
        )
        appeal.remarks?.let {
            Text(
                "Remarks: $it",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}