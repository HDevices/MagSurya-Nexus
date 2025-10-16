package com.example.magsuryanexus

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Game(
    val name: String,
    val packageName: String,
    val icon: Drawable?
)

class GamesViewModel : ViewModel() {
    private val _games = MutableStateFlow<List<Game>>(emptyList())
    val games: StateFlow<List<Game>> = _games

    // Por ahora, la lista de juegos se guarda en memoria. Más adelante se puede persistir.
    private val gamePackages = mutableSetOf("com.activision.callofduty.shooter")

    fun loadGames(context: Context) {
        viewModelScope.launch {
            val gameList = withContext(Dispatchers.IO) {
                val packageManager = context.packageManager
                gamePackages.mapNotNull { pkgName ->
                    try {
                        val appInfo = packageManager.getApplicationInfo(pkgName, 0)
                        val name = packageManager.getApplicationLabel(appInfo).toString()
                        val icon = packageManager.getApplicationIcon(appInfo)
                        Game(name, pkgName, icon)
                    } catch (e: PackageManager.NameNotFoundException) {
                        Game("$pkgName (No inst.)", pkgName, null)
                    }
                }
            }
            _games.value = gameList.sortedBy { it.name }
        }
    }

    fun addGame(packageName: String, context: Context) {
        if (packageName.isNotBlank()) {
            gamePackages.add(packageName)
            loadGames(context) // Recargar la lista
        }
    }
}

@Composable
fun GameListView(gamesViewModel: GamesViewModel = viewModel(), onGameLaunch: () -> Unit = {}) {
    val context = LocalContext.current
    val games by gamesViewModel.games.collectAsState()
    var showAddGameDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        gamesViewModel.loadGames(context)
    }

    if (showAddGameDialog) {
        AddGameDialog(
            onDismiss = { showAddGameDialog = false },
            onAdd = { packageName ->
                gamesViewModel.addGame(packageName, context)
                showAddGameDialog = false
            }
        )
    }

    Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                "Juegos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { showAddGameDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir juego")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(150.dp)) {
            items(games) { game ->
                GameListItem(game = game) {
                    onGameLaunch()
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(game.packageName)
                    if (launchIntent != null) {
                        context.startActivity(launchIntent)
                    }
                }
            }
        }
    }
}

@Composable
fun AddGameDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var packageName by rememberSaveable { mutableStateOf("")}

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Juego") },
        text = {
            Column {
                Text("Introduce el nombre del paquete del juego.")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("ej: com.activision.callofduty.shooter") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(packageName) },
                enabled = packageName.isNotBlank() && packageName.contains(".")
            ) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}



@Composable
fun GameListItem(game: Game, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, enabled = game.icon != null)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (game.icon != null) {
            Image(
                bitmap = game.icon.toBitmap().asImageBitmap(),
                contentDescription = game.name,
                modifier = Modifier.size(40.dp)
            )
        } else {
            Image(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = game.name,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
        Text(text = game.name, fontSize = 16.sp)
    }
}