import java.nio.file.DirectoryStream
import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.extension
import kotlin.io.path.notExists

fun main(){
    organizar()
    listar()
}


fun organizar(){

    val datosIni = Path.of("datos", "datos_ini")
    val datosFin = Path.of("datos", "datos_fin")

    println("CREACION DE RUTAS PROYECTO")
    println("Creando rutas...")
    try {

        if (Files.notExists(datosIni)){
            println("Creacion de ruta para DATOS_INI")
            Files.createDirectories(datosIni)
        }

        if (Files.notExists(datosFin)){
            println("Creacion de ruta para DATOS_FIN")
            Files.createDirectories(datosFin)
        }

    } catch (e: Exception) {
        println("HA OCURRIDO UN ERROR DURANTE LA CREACION DE LAS CARPETAS")
        e.printStackTrace()
    }
}

//Lista la estructura de arbol que hay para comprobar que todo esta bien
fun listar(){
    println("MOSTRANDO ESTRUCTURA DE DIRECTORIOS Y FICHEROS")

    val carpeta = Path.of("datos")

    println("[DIR] datos")

    try {
        Files.walk(carpeta).use { stream ->
            stream.sorted().forEach { path ->
                //Calculo de tabulaciones para saber la posicion del arbol concreta
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