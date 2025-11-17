package com.invoiceapp.ui.query

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.invoiceapp.data.api.McpQueryResponse
import com.invoiceapp.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueryScreen(
    viewModel: QueryViewModel = hiltViewModel()
) {
    var queryText by remember { mutableStateOf("") }
    val queryState by viewModel.queryState.collectAsState()
    val examplesState by viewModel.examplesState.collectAsState()
    val queryHistory by viewModel.queryHistory.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadExamples()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ask About Your Spending") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask a question...") },
                        singleLine = false,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (queryText.isNotBlank()) {
                                viewModel.submitQuery(queryText)
                                queryText = ""
                            }
                        },
                        enabled = queryText.isNotBlank() && queryState !is Resource.Loading
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (queryText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Examples section
            if (queryHistory.isEmpty()) {
                item {
                    Text(
                        text = "Try asking:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                when (val examples = examplesState) {
                    is Resource.Success -> {
                        items(examples.data ?: emptyList()) { example ->
                            SuggestionChip(
                                onClick = {
                                    viewModel.submitQuery(example)
                                },
                                label = { Text(example) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    is Resource.Loading -> {
                        item {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                    else -> {
                        item {
                            listOf(
                                "How much did I spend on sweets?",
                                "How much are house basic costs?",
                                "How much was spent for the kids?",
                                "What was my total spending this month?",
                                "Show me my top expenses"
                            ).forEach { example ->
                                SuggestionChip(
                                    onClick = {
                                        viewModel.submitQuery(example)
                                    },
                                    label = { Text(example) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // Query history
            items(queryHistory.reversed()) { (query, response) ->
                QueryItem(query = query, response = response)
            }

            // Loading indicator
            if (queryState is Resource.Loading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Analyzing your spending...")
                        }
                    }
                }
            }

            // Error state
            if (queryState is Resource.Error) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (queryState as Resource.Error).message ?: "Failed to process query",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QueryItem(
    query: String,
    response: McpQueryResponse
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // User query
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .align(Alignment.End),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
        ) {
            Text(
                text = query,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // AI response
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .align(Alignment.Start),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = response.answer,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium
                )

                // Suggestions
                if (response.suggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You might also want to ask:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    response.suggestions.take(3).forEach { suggestion ->
                        Text(
                            text = "• $suggestion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
