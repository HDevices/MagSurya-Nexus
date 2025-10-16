package com.example.magsuryanexus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class CpuCoreInfo(
    val id: Int,
    val currentFreq: Long,
    val minFreq: Long,
    val maxFreq: Long,
    val governor: String
)

class CpuViewModel : ViewModel() {

    private val _cpuInfo = MutableStateFlow<List<CpuCoreInfo>>(emptyList())
    val cpuInfo: StateFlow<List<CpuCoreInfo>> = _cpuInfo

    init {
        startCpuMonitoring()
    }

    private fun startCpuMonitoring() {
        viewModelScope.launch {
            while (isActive) {
                _cpuInfo.value = fetchCpuInfo()
                delay(1000) // Actualizar cada segundo
            }
        }
    }

    private suspend fun fetchCpuInfo(): List<CpuCoreInfo> = withContext(Dispatchers.IO) {
        val cpuCores = mutableListOf<CpuCoreInfo>()
        try {
            File("/sys/devices/system/cpu/").listFiles { file ->
                file.isDirectory && file.name.matches(Regex("cpu[0-9]+"))
            }?.forEach { coreDir ->
                val coreId = coreDir.name.substring(3).toInt()
                val freqDir = File(coreDir, "cpufreq")

                val currentFreq = freqDir.resolve("scaling_cur_freq").takeIf { it.exists() }?.readText()?.trim()?.toLongOrNull() ?: 0L
                val minFreq = freqDir.resolve("scaling_min_freq").takeIf { it.exists() }?.readText()?.trim()?.toLongOrNull() ?: 0L
                val maxFreq = freqDir.resolve("scaling_max_freq").takeIf { it.exists() }?.readText()?.trim()?.toLongOrNull() ?: 0L
                val governor = freqDir.resolve("scaling_governor").takeIf { it.exists() }?.readText()?.trim() ?: "N/A"
                
                cpuCores.add(CpuCoreInfo(coreId, currentFreq, minFreq, maxFreq, governor))
            }
        } catch (e: Exception) {
            // En caso de error, no hacer nada por ahora
        }
        cpuCores.sortedBy { it.id }
    }
}