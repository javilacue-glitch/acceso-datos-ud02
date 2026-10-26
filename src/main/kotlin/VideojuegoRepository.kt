import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.w3c.dom.Node
import kotlin.io.path.exists
import kotlin.io.path.readText

fun leerDatosCSV(ruta: Path): List<Videojuego> {
    var juegos: List<Videojuego> = emptyList()

    if (!Files.isReadable(ruta)) {
        println("Error: No se puede leer el fichero en la ruta: $ruta")
    } else {
        val reader = csvReader {
            delimiter = ';'
        }

        val filas: List<List<String>> = reader.readAll(ruta.toFile())

        juegos = filas.mapNotNull { columnas ->
            if (columnas.size >= 5) {
                try {
                    val id = columnas[0].toInt()
                    val titulo = columnas[1]
                    val genero = columnas[2]
                    val anio = columnas[3].toInt()
                    val nota = columnas[4].toDouble()
                    Videojuego(id, titulo, genero, anio, nota)
                } catch (e: Exception) {
                    println("Fila inválida ignorada: $columnas -> Error: ${e.message}")
                    null
                }
            } else {
                println("Fila con formato incompleto ignorada: $columnas")
                null
            }
        }
    }
    println("---Información leída con éxito de: $ruta")
    return juegos
}

fun escribirCSV(ruta: Path, juegos: List<Videojuego>) {
    try {
        val fichero: File = ruta.toFile()
        csvWriter {
            delimiter = ';'
        }.writeAll(
            juegos.map { videojuego ->
                listOf(
                    videojuego.id.toString(),
                    videojuego.titulo,
                    videojuego.genero,
                    videojuego.anio.toString(),
                    videojuego.nota.toString()
                )
            },
            fichero
        )
        println("--- Información guardada con éxito en $fichero")
    } catch (e: Exception) {
        println("Error al escribir el fichero CSV: ${e.message}")
    }
}

@JacksonXmlRootElement(localName = "Videojuego")
data class VideojuegosXML(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "videojuego")
    val listaVideojuego: List<Videojuego> = emptyList()
)
fun leerVideojuegosJSON(rutaJSON: Path): List<Videojuego> {
    if (!rutaJSON.exists()){
        println("Error: No existe el fichero JSON en la ruta: $rutaJSON")
        return emptyList()
    }

    return try {
        val textoJSON = rutaJSON.readText()
        // Decodificamos utilizando la clase VideojuegoJSON
        val jsonDecodificado = Json.decodeFromString<List<VideojuegoJSON>>(textoJSON)

        // Mapeamos a la clase principal Videojuego
        jsonDecodificado.map {
            Videojuego(it.id, it.titulo, it.genero, it.anio, it.nota)
        }
    } catch (e: Exception){
        println("Error al parsear el JSON: ${e.message}")
        emptyList()
    }
}

fun leerVideojuegosXML(rutaXML: Path): List<Videojuego> {
    if (!rutaXML.exists()) {
        println("Error: No existe el fichero XML en la ruta: $rutaXML")
        return emptyList()
    }

    val listaXML = mutableListOf<Videojuego>()

    return try {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(rutaXML.toFile())
        document.documentElement.normalize()

        val nodeList = document.getElementsByTagName("videojuego")

        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                val element = node as Element
                val id = element.getElementsByTagName("id").item(0).textContent.toInt()
                val titulo = element.getElementsByTagName("titulo").item(0).textContent
                val genero = element.getElementsByTagName("genero").item(0).textContent
                val anio = element.getElementsByTagName("anio").item(0).textContent.toInt()
                val nota = element.getElementsByTagName("nota").item(0).textContent.toDouble()

                listaXML.add(Videojuego(id, titulo, genero, anio, nota))
            }
        }
        listaXML
    } catch (e: Exception) {
        println("Error al parsear el XML: ${e.message}")
        emptyList()
    }
}

fun conversionFormatos(rutaConversor: Path): List<Videojuego> {
    while (true) {
        println("--- Conversiones ---")
        println("1. CSV  → JSON")
        println("2. CSV  → XML")
        println("3. JSON → CSV")
        println("4. JSON → XML")
        println("5. XML  → CSV")
        println("6. XML  → JSON")
        println("0. Volver")
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

            }

            "5" -> {

            }

            "6" -> {

            }

            "0" -> {
                println("Saliendo del programa...")
                break
            }

            else -> println("No existe esa opción, selecciona otra")
        }
    }
}