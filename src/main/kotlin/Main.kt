import java.nio.file.Path
import kotlin.io.path.Path

fun main() {
    val rutaCSV = Path("datos/datos_ini/videojuegos.csv")
    val rutaXML = Path("datos/datos_ini/videojuegos.xml")
    val rutaJSON = Path("datos/datos_ini/videojuegos.json")
    var catalogo = leerDatosCSV(rutaCSV)

    while (true) {
        println("===== CATÁLOGO DE VIDEOJUEGOS =====")
        println("1. Gestion CSV")
        println("2. Leer catálogo desde XML")
        println("3. Leer catálogo desde JSON")
        println("4. Conversiones entre formatos")
        println("0. Salir del programa")
        println("====================================")
        print("Elige una opción: ")

        when (readlnOrNull()?.trim()?.lowercase()) {
            "1" -> {
                catalogo = gestionCSV(catalogo, rutaCSV)
            }

            "2" -> {
                catalogo = integrarXML(catalogo, rutaXML)
                escribirCSV(rutaCSV, catalogo)
            }

            "3" -> {
                catalogo = integrarJSON(catalogo, rutaJSON)
            }

            "4" -> {
                conversionFormatos()
            }

            "0" -> {
                println("Saliendo del programa...")
                break
            }

            else -> println("No existe esa opción, selecciona otra")
        }
    }
}

fun gestionCSV(listaInicial: List<Videojuego>, ruta: Path): List<Videojuego> {
    var lista = listaInicial

    while (true) {
        println("=== PANEL DE GESTIÓN CSV ===")
        println("1. Listar el catálogo")
        println("2. Buscar un videojuego")
        println("3. Añadir un videojuego")
        println("4. Modificar una nota")
        println("5. Eliminar un videojuego")
        println("0. Volver al inicio")
        print("Elige una opción: ")

        when (readlnOrNull()?.lowercase()?.trim()) {
            "1" -> listarVideojuegos(lista)

            "2" -> buscarPorId(lista)

            "3" -> {
                lista = añadirVideojuego(lista)
                escribirCSV(ruta, lista)
            }

            "4" -> {
                lista = modificarNota(lista)
                escribirCSV(ruta, lista)
            }

            "5" -> {
                lista = eliminarVideojuego(lista)
                escribirCSV(ruta, lista)
            }

            "0" -> return lista

            else -> println("Opción no válida.")
        }
    }
}