package com.example.llmlearningassistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.llmlearningassistant.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestsScreen(
    viewModel: MainViewModel,
    onContinue: () -> Unit
) {
    val interests by viewModel.interests.collectAsState()
    val selectedCount by viewModel.selectedInterests.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Select Interests") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Select up to 10 topics you want to learn about:",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(interests) { interest ->
                    FilterChip(
                        selected = interest.isSelected,
                        onClick = { viewModel.toggleInterest(interest.id) },
                        label = { Text(interest.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedCount.isNotEmpty()
            ) {
                Text("Continue to Home")
            }
        }
    }
}
