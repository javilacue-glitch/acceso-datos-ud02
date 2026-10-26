import java.nio.file.Path
import kotlin.io.path.Path

fun main() {
    organizar()
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
        println("0. Salir")
        println("====================================")
        print("Elige una opción: ")

        when (readlnOrNull()?.trim()?.lowercase()) {
            "1" -> {
                catalogo = gestionCSV(catalogo, rutaCSV)
            }

            "2" -> {
                catalogo = integrarCatalogo(catalogo, leerVideojuegosXML(rutaXML))
                escribirCSV(rutaCSV, catalogo)
            }

            "3" -> {
                catalogo = integrarCatalogo(catalogo, leerVideojuegosJSON(rutaJSON))
                escribirCSV(rutaCSV, catalogo)
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
        println("--- Gestión CSV ---")
        println("1. Listar todos los videojuegos")
        println("2. Buscar videojuego por ID")
        println("3. Añadir nuevo videojuego")
        println("4. Modificar nota de un videojuego")
        println("5. Eliminar videojuego")
        println("0. Volver al menú principal")

        when (readlnOrNull()?.lowercase()?.trim()) {
            "1" -> listarVideojuegos(lista)

            "2" -> {
                val id = pedirId("Introduce el ID a buscar: ")
                if (id != null) buscarPorId(lista, id)
            }

            "3" -> {
                lista = añadirVideojuego(lista)
                escribirCSV(ruta, lista)
            }

            "4" -> {
                val id = pedirId("Introduce el ID del videojuego: ")
                if (id != null) {
                    if (lista.none { it.id == id }) {
                        println("Error: No existe ningún videojuego con el ID $id.")
                    } else {
                        val nuevaNota = pedirNota("Introduce la nueva nota (0.0 - 10.0): ")
                        lista = modificarNota(lista, id, nuevaNota)
                        escribirCSV(ruta, lista)
                    }
                }
            }

            "5" -> {
                val id = pedirId("Introduce el ID del videojuego a eliminar: ")
                if (id != null) {
                    lista = eliminarVideojuego(lista, id)
                    escribirCSV(ruta, lista)
                }
            }

            "0" -> return lista

            else -> println("Opción no válida.")
        }
    }
}