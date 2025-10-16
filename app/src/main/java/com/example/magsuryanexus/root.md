# Estrategia de Implementación: Modo Juego Atómico y Modular

Este documento detalla la arquitectura actual del proyecto, que ha evolucionado hacia un sistema de **scripts atómicos y configurables** para un control más granular y flexible del "Modo Juego Absoluto".

## Resumen del Enfoque

Los objetivos generales de control del sistema permanecen, pero la implementación ha cambiado para favorecer la modularidad:

1.  **Congelar Procesos No Esenciales.**
2.  **Convertirse en un Launcher Temporal.**
3.  **Aislamiento de Recursos (cgroups).**
4.  **Prioridad Máxima del Planificador (Scheduler).**

---

## Arquitectura de Scripts Atómicos

Se abandona la idea de scripts monolíticos (`activate.sh`) y la construcción dinámica de scripts en Kotlin. En su lugar, adoptamos un enfoque de "Scripts Atómicos".

**Principio:** Cada optimización específica se encapsula en su propio script, pequeño y de propósito único, ubicado en la carpeta `assets/`. 

### Ventajas de este enfoque:

*   **Configurabilidad:** En el futuro, desde el panel de Ajustes, el usuario podrá habilitar o deshabilitar cada script individualmente.
*   **Mantenimiento:** Es mucho más fácil depurar, modificar o añadir optimizaciones sin afectar al resto.
*   **Claridad:** El propósito de cada archivo es evidente por su nombre.

### Ejemplos de Scripts Atómicos:

*   `cpu_performance.sh`: Cambia el gobernador de todos los núcleos de la CPU a `performance`.
*   `cpu_schedutil.sh`: Restaura el gobernador a `schedutil`.
*   `connectivity_disable.sh`: Desactiva Bluetooth, NFC y GPS.
*   `connectivity_enable.sh`: Reactiva los servicios de conectividad.
*   `game_priority.sh`: Acepta un PID como argumento para elevar la prioridad del juego con `chrt`.
*   `freeze_user_apps.sh`: Congela una lista predefinida de aplicaciones de usuario.
*   `unfreeze_user_apps.sh`: Descongela las aplicaciones de usuario.

---

## `RootController.kt` (Versión Modular)

El `RootController` se ha simplificado drásticamente. Su única responsabilidad es ejecutar una lista de scripts que se le proporciona, haciendo el sistema predecible y desacoplado.

```kotlin
class RootController {

    /**
     * Ejecuta una lista de scripts desde la carpeta de assets con privilegios de superusuario.
     */
    fun executeScripts(context: Context, scriptNames: List<String>, gamePid: String? = null) {
        // ... implementación que itera sobre la lista `scriptNames` ...
        // ... copia cada script a un directorio ejecutable ...
        // ... y lo ejecuta con `su` pasándole el gamePid si existe ...
    }
}
```

---

## Flujo de Ejecución Actualizado

La secuencia de eventos ahora sigue un modelo más flexible:

1.  **(Futuro) Configuración:** El usuario navega al panel de Ajustes (en el `Drawer`) y selecciona qué optimizaciones (scripts) quiere aplicar cuando se active el modo juego.

2.  **Activación del Modo Juego:**
    *   El usuario pulsa el botón "Activar Modo Juego Extremo".
    *   La app pide convertirse en el **launcher por defecto**.

3.  **Lanzamiento del Juego:**
    *   El usuario pulsa en un juego de la `GameListView`.
    *   La lógica de la aplicación compila una lista de los **nombres de los scripts que están habilitados** en la configuración.
    *   (Futuro) Se obtiene el PID del juego recién lanzado.
    *   Se invoca a `rootController.executeScripts(context, listaDeScripts, pidDelJuego)`.

4.  **Salida del Juego:**
    *   Se detecta que el proceso del juego ha finalizado.
    *   Se compila una lista de los scripts de "restauración" (ej. `cpu_schedutil.sh`, `connectivity_enable.sh`).
    *   Se invoca a `rootController.executeScripts(context, listaDeScriptsDeRestauracion)`.
    *   Se restaura el launcher original del sistema.

Esta arquitectura modular nos da una base sólida para construir un sistema de optimización potente y personalizable, tal como se planeó.
