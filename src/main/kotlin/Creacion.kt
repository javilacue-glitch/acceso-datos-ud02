import java.nio.file.*
import kotlin.io.path.*

fun organizar(){

    val datosIni = Path.of("datos", "datos_ini")
    val datosFin = Path.of("datos", "datos_fin")

    println("CREACIÓN DE RUTAS PROYECTO")
    println("Creando rutas...")
    try {

        if (Files.notExists(datosIni)){
            println("Creación de ruta para DATOS_INI")
            Files.createDirectories(datosIni)
        }

        if (Files.notExists(datosFin)){
            println("Creación de ruta para DATOS_FIN")
            Files.createDirectories(datosFin)
        }

    } catch (e: Exception) {
        println("HA OCURRIDO UN ERROR DURANTE LA CREACIÓN DE LAS CARPETAS")
        e.printStackTrace()
    }
}

fun listar(){
    println("MOSTRANDO ESTRUCTURA DE DIRECTORIOS Y FICHEROS")

    val carpeta = Path.of("datos")

    println("[DIR] datos")

    try {
        Files.walk(carpeta).use { stream ->
            stream.sorted().forEach { path ->
                val profundidad = path.nameCount - carpeta.nameCount
                val tabulacion = "\t".repeat(profundidad)
                val prefijo = if (Files.isDirectory(path)) "[DIR]" else "[FILE]"

                if (profundidad > 0) {
                    println("$tabulacion$prefijo ${path.fileName}")
                }
            }
        }
    } catch (e: Exception) {
        println("[ERROR]")
        e.printStackTrace()
    }
}