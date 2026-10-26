import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.*
import com.fasterxml.jackson.module.kotlin.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.*
import java.nio.file.*
import kotlin.io.path.*


@JacksonXmlRootElement(localName = "videojuegos")
data class VideojuegosXML(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "videojuego")
    val listaVideojuego: List<Videojuego> = emptyList()
)

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

fun leerVideojuegosJSON(rutaJSON: Path): List<Videojuego> {
    if (!rutaJSON.exists()) {
        println("Error: No existe el fichero JSON en la ruta: $rutaJSON")
        return emptyList()
    }
    return try {
        val textoJSON = rutaJSON.readText()
        Json.decodeFromString<List<Videojuego>>(textoJSON)
    } catch (e: Exception) {
        println("Error al parsear el JSON: ${e.message}")
        emptyList()
    }
}

fun escribirJSON(ruta: Path, juegos: List<Videojuego>) {
    try {
        val format = Json { prettyPrint = true }
        val textoJSON = format.encodeToString(juegos)
        ruta.toFile().writeText(textoJSON)
    } catch (e: Exception) {
        println("Error al escribir el JSON: ${e.message}")
    }
}

fun leerVideojuegosXML(rutaXML: Path): List<Videojuego> {
    if (!rutaXML.exists()) {
        println("Error: No existe el fichero XML en la ruta: $rutaXML")
        return emptyList()
    }
    return try {
        val mapper = XmlMapper().registerKotlinModule()
        val contenedor = mapper.readValue(rutaXML.toFile(), VideojuegosXML::class.java)
        contenedor.listaVideojuego
    } catch (e: Exception) {
        println("Error al parsear el XML con Jackson: ${e.message}")
        emptyList()
    }
}

fun escribirXML(ruta: Path, juegos: List<Videojuego>) {
    try {
        val mapper = XmlMapper().registerKotlinModule()
        val contenedor = VideojuegosXML(juegos)
        mapper.writerWithDefaultPrettyPrinter().writeValue(ruta.toFile(), contenedor)
    } catch (e: Exception) {
        println("Error al escribir el XML: ${e.message}")
    }
}

fun convertir(
    origen: Path,
    destino: Path,
    leer: (Path) -> List<Videojuego>,
    escribir: (Path, List<Videojuego>) -> Unit
) {
    if (!origen.exists()){
        println("Error: El fichero de origen no existe ($origen)")
        return
    }

    val datos = leer(origen)
    if (datos.isEmpty()) {
        println("No se encuentró ningun dato en el fichero de origen")
        return
    }

    escribir(destino, datos)
    println("La conversión se ha completado con exito. ${datos.size} registros guardados en $destino")
}

fun conversionFormatos() {
    val dirIni = Path.of("datos/datos_ini")
    val dirFin = Path.of("datos/datos_fin")

    // Si no existe el directorio final, lo creamos
    if (!dirFin.exists()) Files.createDirectories(dirFin)

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

        when (readlnOrNull()?.trim()) {
            "1" -> convertir(dirIni.resolve("videojuegos.csv"), dirFin.resolve("videojuegos.json"), ::leerDatosCSV, ::escribirJSON)
            "2" -> convertir(dirIni.resolve("videojuegos.csv"), dirFin.resolve("videojuegos.xml"), ::leerDatosCSV, ::escribirXML)
            "3" -> convertir(dirIni.resolve("videojuegos.json"), dirFin.resolve("videojuegos.csv"), ::leerVideojuegosJSON, ::escribirCSV)
            "4" -> convertir(dirIni.resolve("videojuegos.json"), dirFin.resolve("videojuegos.xml"), ::leerVideojuegosJSON, ::escribirXML)
            "5" -> convertir(dirIni.resolve("videojuegos.xml"), dirFin.resolve("videojuegos.csv"), ::leerVideojuegosXML, ::escribirCSV)
            "6" -> convertir(dirIni.resolve("videojuegos.xml"), dirFin.resolve("videojuegos.json"), ::leerVideojuegosXML, ::escribirJSON)
            "0" -> break
            else -> println("Opción no válida.")
        }
    }
}