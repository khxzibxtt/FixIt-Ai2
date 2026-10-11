package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FixItAmber
import com.example.ui.theme.FixItBlue
import com.example.ui.theme.FixItEmerald
import com.example.ui.theme.FixItError
import com.example.ui.viewmodel.FixItViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisputeArbitrationScreen(
    viewModel: FixItViewModel,
    onNavigateBack: () -> Unit
) {
    val job by viewModel.selectedJob.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val disputeRuling by viewModel.disputeRuling.collectAsState()

    var consumerClaimText by remember {
        mutableStateOf("Vendor replaced valve but joint started leaking after 4 hours. Water damage to cabinet. Original defect not fully resolved.")
    }
    var vendorDefenseText by remember {
        mutableStateOf("Installed high-grade brass valve tested under 3-bar pressure with consumer present. Pipe fracture is further down the riser, unrelated to the valve joint.")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("High Thinking AI Tribunal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = "Thinking Mode", tint = FixItAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "High Thinking Adjudication Engine",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Powered by gemini-3.1-pro-preview with thinkingLevel = HIGH. Evaluates photographic evidence, timestamps, and contract terms to deliver binding escrow resolutions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Evidence inputs
            Text("Consumer Statement & Complaint", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = consumerClaimText,
                onValueChange = { consumerClaimText = it },
                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("consumer_claim_input"),
                shape = RoundedCornerShape(10.dp)
            )

            Text("Vendor Statement & Technical Defense", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = vendorDefenseText,
                onValueChange = { vendorDefenseText = it },
                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("vendor_defense_input"),
                shape = RoundedCornerShape(10.dp)
            )

            Button(
                onClick = {
                    val currentJob = job ?: return@Button
                    viewModel.requestArbitration(currentJob, consumerClaimText, vendorDefenseText)
                },
                enabled = !isAiLoading,
                colors = ButtonDefaults.buttonColors(containerColor = FixItAmber),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("run_tribunal_button")
            ) {
                if (isAiLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Deep Thinking Arbitration in progress...", color = Color.Black)
                } else {
                    Icon(Icons.Default.Balance, contentDescription = "Arbitrate", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Execute Deep Thinking Arbitration", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            // Results Card
            if (disputeRuling != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("tribunal_ruling_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = "Ruling", tint = FixItEmerald)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Tribunal Adjudication Ruling",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = disputeRuling ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    viewModel.setNotice("Dispatched replacement certified technician. 0 OMR cost to consumer. Escrow re-allocated.")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FixItBlue),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Dispatch Re-Fix", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.setNotice("Escrow full refund returned to consumer wallet.")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Full Refund", fontSize = 12.sp, color = FixItEmerald)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
