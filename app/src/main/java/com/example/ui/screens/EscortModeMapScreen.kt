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
import com.example.ui.viewmodel.FixItViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscortModeMapScreen(
    viewModel: FixItViewModel,
    onNavigateBack: () -> Unit
) {
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val mapsStoresResult by viewModel.mapsStoresResult.collectAsState()

    var neighborhoodLocation by remember { mutableStateOf("Ruwi & Al Khuwair, Muscat") }
    var partsCategory by remember { mutableStateOf("Sanitary & Plumbing Hardware") }

    LaunchedEffect(Unit) {
        if (mapsStoresResult == null) {
            viewModel.searchNearbySupplyStores(neighborhoodLocation, partsCategory)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escort Mode • Supply Locator", fontWeight = FontWeight.Bold) },
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
            // Escort Mode Explainer Card (Module 05)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = "Store", tint = FixItBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Escort Mode: Zero Markup Shopping", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "• The tradesperson inspects the problem and writes a specific shopping list.\n• The vendor physically accompanies you to the local merchant.\n• You pay the shop directly. No parts cash ever touches the platform or vendor wallet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Maps Grounding Search Form
            Text("Find Verified Supply Stores Nearby", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = neighborhoodLocation,
                onValueChange = { neighborhoodLocation = it },
                label = { Text("Location / District") },
                modifier = Modifier.fillMaxWidth().testTag("location_search_input"),
                leadingIcon = { Icon(Icons.Default.PinDrop, contentDescription = "Pin") }
            )

            OutlinedTextField(
                value = partsCategory,
                onValueChange = { partsCategory = it },
                label = { Text("Material Specialty") },
                modifier = Modifier.fillMaxWidth().testTag("category_search_input"),
                leadingIcon = { Icon(Icons.Default.Category, contentDescription = "Category") }
            )

            Button(
                onClick = { viewModel.searchNearbySupplyStores(neighborhoodLocation, partsCategory) },
                enabled = !isAiLoading,
                colors = ButtonDefaults.buttonColors(containerColor = FixItBlue),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("search_maps_grounding_button")
            ) {
                if (isAiLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Searching Google Maps...", color = Color.White)
                } else {
                    Icon(Icons.Default.Explore, contentDescription = "Explore", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search Local Merchants (Google Maps Grounded)", color = Color.White)
                }
            }

            // Results Card
            if (mapsStoresResult != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("maps_results_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = "Stores", tint = FixItEmerald)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recommended Supply Centers", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = mapsStoresResult ?: "",
                            color = Color(0xFFE2E8F0),
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FixItEmerald.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsWalk, contentDescription = "Walk", tint = FixItEmerald)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Vendor will meet you at the shop or accompany from residence.", color = FixItEmerald, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
