package com.example.magsuryanexus

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.magsuryanexus.ui.theme.MagSuryaNexusTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MagSuryaNexusTheme {
                AppScaffold()
            }
        }
    }
}

// Función para cambiar el launcher por defecto
fun setAsDefaultLauncher(context: Context) {
    val intent = Intent(Settings.ACTION_HOME_SETTINGS)
    context.startActivity(intent)
}

// Función para restaurar el launcher original (necesita root)
fun restorePreviousLauncher(context: Context, rootController: RootController) {
    // Este es un placeholder. La implementación real es más compleja.
    // Necesitaríamos encontrar el launcher anterior y habilitarlo.
    // Por ahora, ejecutamos el script de desactivación.
    rootController.executeScripts(context, listOf("deactivate.sh"))

    // Deshabilitamos nuestra propia capacidad de ser launcher
    val componentName = ComponentName(context, MainActivity::class.java)
    context.packageManager.setComponentEnabledSetting(
        componentName,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP
    )
    // Y luego re-habilitamos para futuros usos
    context.packageManager.setComponentEnabledSetting(
        componentName,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold() {
    val rootController = remember { RootController() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Ajustes", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                HorizontalDivider()

                var selinuxPermissive by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SELinux Permissive", modifier = Modifier.weight(1f))
                    Switch(
                        checked = selinuxPermissive,
                        onCheckedChange = { isChecked ->
                            selinuxPermissive = isChecked
                            val script = if (isChecked) "selinux_permissive.sh" else "selinux_enforcing.sh"
                            rootController.executeScripts(context, listOf(script))
                        }
                    )
                }

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                    label = { Text("Configuración General") },
                    selected = false,
                    onClick = { /* TODO: Navegar a pantalla de ajustes */ }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("MagSurya Nexus") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { innerPadding ->
            MainScreen(
                modifier = Modifier.padding(innerPadding),
                rootController = rootController
            )
        }
    }
}


@Composable
fun MainScreen(modifier: Modifier = Modifier, rootController: RootController) {
    var isGameModeActive by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Modo Juego Extremo", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            isGameModeActive = !isGameModeActive
            if (isGameModeActive) {
                // Fase 1: Convertirse en Launcher
                setAsDefaultLauncher(context)
                // La ejecución del script se moverá a la fase de lanzamiento del juego
            } else {
                // Restaurar el launcher anterior (lógica simplificada por ahora)
                restorePreviousLauncher(context, rootController)
            }
        }) {
            Text(if (isGameModeActive) "Desactivar Modo Juego Extremo" else "Activar Modo Juego Extremo")
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 32.dp))

        CpuDashboardView()

        Spacer(modifier = Modifier.weight(1f))

        // Modificamos GameListView para que ejecute el script al lanzar el juego
        GameListView(onGameLaunch = {
            if (isGameModeActive) {
                // Fase 2: Aplicar optimizaciones al lanzar el juego
                rootController.executeScripts(context, listOf("activate.sh"))
            }
        })

        HorizontalDivider(modifier = Modifier.padding(horizontal = 32.dp))
        Spacer(modifier = Modifier.height(16.dp))

        Text("Logs en Tiempo Real (Próximamente)", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MagSuryaNexusTheme {
        AppScaffold()
    }
}
