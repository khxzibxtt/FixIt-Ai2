package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.viewmodel.FixItViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceCatalogScreen(
    viewModel: FixItViewModel,
    onNavigateBack: () -> Unit
) {
    val services by viewModel.services.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var showRequestDialog by remember { mutableStateOf(false) }
    var serviceNameText by remember { mutableStateOf("") }
    var serviceCategoryText by remember { mutableStateOf("Transport") }
    var serviceDescText by remember { mutableStateOf("") }

    var showSkillProofDialog by remember { mutableStateOf(false) }
    var proofCategoryText by remember { mutableStateOf("HVAC") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dynamic Service Catalog", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showRequestDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Request Service", tint = FixItAmber)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Philosophy Card (Module 02)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = FixItAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Not just trades. An Any-Service App.", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Plumbers, electricians, and transport drivers are all verified providers. If a category doesn't exist, request it—admin review expands the marketplace within 24-48 hours.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { showRequestDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = FixItAmber),
                            modifier = Modifier.fillMaxWidth().testTag("request_service_button")
                        ) {
                            Text("+ Request New Service Category", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Skill Tag Proof Card for Vendors
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = FixItBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vendor Multi-Skill Tagging Proof", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Vendors only see job posts matching skills they've been cleared for. Upload certificate or past work photos per skill to unlock bidding.",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { showSkillProofDialog = true },
                            modifier = Modifier.fillMaxWidth().testTag("submit_skill_proof_button")
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = "Upload", tint = FixItBlue)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Submit Skill Tag Verification Photo", color = FixItBlue)
                        }
                    }
                }
            }

            item {
                Text("Approved Marketplace Categories (${services.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            items(services) { service ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(service.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.weight(1f))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (service.isApproved) FixItEmerald.copy(alpha = 0.2f) else FixItAmber.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (service.isApproved) "Live Category" else "Under Admin Review (24-48h)",
                                    color = if (service.isApproved) FixItEmerald else FixItAmber,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(service.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Fair Market Range: ", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("${service.suggestedPriceFloor.toInt()} - ${service.suggestedPriceCeiling.toInt()} OMR", fontWeight = FontWeight.Bold, color = FixItAmber, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Modal: Request New Service Category
    if (showRequestDialog) {
        AlertDialog(
            onDismissRequest = { showRequestDialog = false },
            title = { Text("Request to Add Service") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Can't find your trade? Submit a request. Once approved by admin, a network-wide push notification alerts providers.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = serviceNameText,
                        onValueChange = { serviceNameText = it },
                        label = { Text("Service Name (e.g. Taxi / AC Duct Cleaning)") },
                        modifier = Modifier.fillMaxWidth().testTag("new_service_name_input")
                    )
                    OutlinedTextField(
                        value = serviceCategoryText,
                        onValueChange = { serviceCategoryText = it },
                        label = { Text("Vertical Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = serviceDescText,
                        onValueChange = { serviceDescText = it },
                        label = { Text("Description & Typical Tools") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (serviceNameText.isNotBlank()) {
                            viewModel.requestCustomService(serviceNameText, serviceCategoryText, serviceDescText)
                            showRequestDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FixItAmber),
                    modifier = Modifier.testTag("submit_new_service_button")
                ) {
                    Text("Submit to Admin Queue", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Modal: Submit Skill Proof Photo
    if (showSkillProofDialog) {
        AlertDialog(
            onDismissRequest = { showSkillProofDialog = false },
            title = { Text("Submit Skill Tag Proof") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Quality Guardrail: Upload proof of past work or certification to receive job alerts for this vertical.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = proofCategoryText,
                        onValueChange = { proofCategoryText = it },
                        label = { Text("Skill Tag (e.g. Electrical / AC Repair)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = "Proof", tint = FixItAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Work photo & certificate attached", color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setNotice("Skill proof for '$proofCategoryText' submitted. Admin clearance in progress.")
                        showSkillProofDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FixItAmber)
                ) {
                    Text("Submit for Verification", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSkillProofDialog = false }) { Text("Cancel") }
            }
        )
    }
}
