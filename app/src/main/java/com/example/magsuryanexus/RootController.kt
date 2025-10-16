package com.example.magsuryanexus

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class RootController {

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Ejecuta una lista de scripts de forma asíncrona desde la carpeta de assets con privilegios de superusuario.
     *
     * @param context El contexto de la aplicación para acceder a los assets.
     * @param scriptNames La lista de nombres de archivo de los scripts a ejecutar (ej. "set_cpu_performance.sh").
     * @param gamePid (Opcional) El PID del juego para pasarlo como argumento a los scripts.
     */
    fun executeScripts(context: Context, scriptNames: List<String>, gamePid: String? = null) {
        scope.launch {
            try {
                val process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process.outputStream)

                for (scriptName in scriptNames) {
                    val scriptFile = prepareScript(context, scriptName)
                    val command = if (gamePid != null) {
                        "sh ${scriptFile.absolutePath} $gamePid\n"
                    } else {
                        "sh ${scriptFile.absolutePath}\n"
                    }
                    os.writeBytes(command)
                    os.flush()
                }

                os.writeBytes("exit\n")
                os.flush()
                os.close()
                process.waitFor()
            } catch (e: IOException) {
                e.printStackTrace() // Manejar el error
            } catch (e: InterruptedException) {
                e.printStackTrace() // Manejar el error
            }
        }
    }

    /**
     * Copia un script desde assets al almacenamiento interno y lo hace ejecutable.
     */
    private fun prepareScript(context: Context, scriptName: String): File {
        val scriptFile = File(context.filesDir, scriptName)
        // No es necesario recrear el script si ya existe y tiene el mismo contenido.
        if (scriptFile.exists()) {
            // (Opcional) Se podría añadir una comprobación de hash si los scripts cambian a menudo
            return scriptFile
        }

        context.assets.open(scriptName).use { input ->
            FileOutputStream(scriptFile).use { output ->
                input.copyTo(output)
            }
        }
        
        // Hacemos el script ejecutable
        Runtime.getRuntime().exec("chmod +x ${scriptFile.absolutePath}").waitFor()
        return scriptFile
    }
}
