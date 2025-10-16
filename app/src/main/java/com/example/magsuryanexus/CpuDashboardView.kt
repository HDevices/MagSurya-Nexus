package com.example.magsuryanexus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CpuDashboardView(cpuViewModel: CpuViewModel = viewModel()) {
    val cpuInfo by cpuViewModel.cpuInfo.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }

    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrow")

    Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Dashboard de Estado de la CPU",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expandir/Contraer",
                modifier = Modifier.rotate(arrowRotation)
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cpuInfo) { coreInfo ->
                    CpuCoreCard(coreInfo)
                }
            }
        }
    }
}

@Composable
fun CpuCoreCard(coreInfo: CpuCoreInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("CPU Core ${coreInfo.id}", fontWeight = FontWeight.Bold)
            Text("Governor: ${coreInfo.governor}", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            val progress = if (coreInfo.maxFreq > 0) coreInfo.currentFreq.toFloat() / coreInfo.maxFreq.toFloat() else 0f
            val animatedProgress by animateFloatAsState(targetValue = progress, label = "freq_progress")

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${coreInfo.currentFreq / 1000} MHz", fontSize = 14.sp, modifier = Modifier.weight(1f))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.weight(2f)
                )
            }
            Row {
                Text("Min: ${coreInfo.minFreq / 1000} MHz", fontSize = 10.sp, color = Color.Gray)
                Spacer(modifier = Modifier.weight(1f))
                Text("Max: ${coreInfo.maxFreq / 1000} MHz", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}