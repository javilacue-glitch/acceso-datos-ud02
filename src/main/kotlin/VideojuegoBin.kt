import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path


// Estructura de cada registro del fichero binario. Todos miden lo mismo, así puedo saltar
// a cualquier posición multiplicando: id (Int, 4 bytes), titulo (30 caracteres x 2 bytes = 60),
// genero (20 caracteres = 40), anio (Int, 4) y nota (Double, 8). Total: 116 bytes
const val LONG_TITULO = 30
const val LONG_GENERO = 20
const val TAM_ID = 4
const val TAM_TITULO = LONG_TITULO * 2
const val TAM_GENERO = LONG_GENERO * 2
const val TAM_ANIO = 4
const val TAM_NOTA = 8
const val TAM_REGISTRO = TAM_ID + TAM_TITULO + TAM_GENERO + TAM_ANIO + TAM_NOTA
const val OFFSET_NOTA = TAM_ID + TAM_TITULO + TAM_GENERO + TAM_ANIO

// ruta del fichero binario y del CSV desde el que se importan los datos
val RUTA_BIN: Path = Path.of("datos", "videojuegos.bin")
val RUTA_CSV_IMPORTAR: Path = Path.of("datos", "datos_ini", "videojuegos.csv")


// Escribe un texto con longitud fija
fun escribirTexto(raf: RandomAccessFile, texto: String, longitud: Int) {
    raf.writeChars(texto.take(longitud).padEnd(longitud, ' '))
}

fun leerTexto(raf: RandomAccessFile, longitud: Int): String {
    val sb = StringBuilder()
    repeat(longitud) { sb.append(raf.readChar()) }
    return sb.toString().trimEnd()
}

// Escribe un registro completo en la posición actual del fichero
fun escribirRegistro(raf: RandomAccessFile, v: Videojuego) {
    raf.writeInt(v.id)
    escribirTexto(raf, v.titulo, LONG_TITULO)
    escribirTexto(raf, v.genero, LONG_GENERO)
    raf.writeInt(v.anio)
    raf.writeDouble(v.nota)
}

// Lee un registro en el mismo orden en que se escribió, en caso de cambiar el orden los datos sal error
fun leerRegistro(raf: RandomAccessFile): Videojuego {
    val id = raf.readInt()
    val titulo = leerTexto(raf, LONG_TITULO)
    val genero = leerTexto(raf, LONG_GENERO)
    val anio = raf.readInt()
    val nota = raf.readDouble()
    return Videojuego(id, titulo, genero, anio, nota)
}

// Número de registros = tamaño del fichero entre el tamaño de un registro
fun numRegistros(raf: RandomAccessFile): Int = (raf.length() / TAM_REGISTRO).toInt()

// Recorre el fichero y devuelve la posición del registro con ese ID, o devuelve -1 en caso de que no exista
fun buscarPosicion(raf: RandomAccessFile, id: Int): Int {
    for (i in 0 until numRegistros(raf)) {
        raf.seek(i.toLong() * TAM_REGISTRO)
        if (raf.readInt() == id) return i
    }
    return -1
}

fun pedirIdValido(mensaje: String): Int {
    while (true) {
        val id = pedirId(mensaje)
        if (id != null) return id
    }
}

fun pedirIdNuevo(raf: RandomAccessFile): Int {
    while (true) {
        val id = pedirId("ID del nuevo videojuego: ") ?: continue
        if (buscarPosicion(raf, id) != -1) {
            println("Ya existe un videojuego con el ID $id.")
        } else {
            return id
        }
    }
}

fun pedirAnio(): Int {
    while (true) {
        print("Año: ")
        val anio = readlnOrNull()?.trim()?.toIntOrNull()
        if (anio != null) return anio
        println("Año inválido. Introduce un número entero.")
    }
}

fun gestionBIN() {
    Files.createDirectories(RUTA_BIN.parent)

    while (true) {
        println("===== CRUD fichero BIN =====")
        println("1. Importar información de un fichero de intercambio.")
        println("2. Leer información del fichero binario.")
        println("3. Añadir un registro nuevo")
        println("4. Modificar un registro existente (por ID)")
        println("5. Eliminar un registro existente (por ID)")
        println("0. Volver")
        print("Elige una opción: ")

        val opcion = readlnOrNull()?.trim()
        if (opcion == "0") return

        try {
            when (opcion) {
                "1" -> importarBIN()
                "2" -> leerBIN()
                "3" -> añadirBIN()
                "4" -> modificarBIN()
                "5" -> eliminarBIN()
                else -> println("Opción no válida.")
            }
        } catch (e: Exception) {
            println("Error al trabajar con el fichero binario: ${e.message}")
        }
    }
}

// Vacía o crea el fichero binario e importa los datos del CSV
fun importarBIN() {
    println("=== IMPORTAR INFORMACIÓN ===")
    val juegos = leerDatosCSV(RUTA_CSV_IMPORTAR)
    if (juegos.isEmpty()) {
        println("No hay datos en el CSV que importar. El fichero binario no se modifica.")
        return
    }

    RandomAccessFile(RUTA_BIN.toFile(), "rw").use { raf ->
        raf.setLength(0)
        juegos.forEach { escribirRegistro(raf, it) }
    }
    println("Importación completada: ${juegos.size} registros guardados en $RUTA_BIN")
}

fun leerBIN() {
    println("=== CONTENIDO DEL FICHERO BINARIO ===")
    if (!Files.exists(RUTA_BIN) || Files.size(RUTA_BIN) == 0L) {
        println("El fichero binario está vacío o no existe.")
        return
    }

    RandomAccessFile(RUTA_BIN.toFile(), "r").use { raf ->
        repeat(numRegistros(raf)) {
            val v = leerRegistro(raf)
            println("${v.id} | ${v.titulo} | ${v.genero} | Año: ${v.anio} | Nota: ${v.nota}")
        }
    }
}

fun añadirBIN() {
    println("=== AÑADIR REGISTRO ===")
    RandomAccessFile(RUTA_BIN.toFile(), "rw").use { raf ->
        val id = pedirIdNuevo(raf)

        print("Título: ")
        var titulo = readlnOrNull()?.trim() ?: ""
        if (titulo.isEmpty()) titulo = "Desconocido"

        print("Género: ")
        var genero = readlnOrNull()?.trim() ?: ""
        if (genero.isEmpty()) genero = "Desconocido"

        val anio = pedirAnio()
        val nota = pedirNota("Nota (0.0 - 10.0): ")

        // me coloco al final del fichero para que el registro nuevo quede detrás de los demás
        raf.seek(raf.length())
        escribirRegistro(raf, Videojuego(id, titulo, genero, anio, nota))
    }
    println("Registro añadido correctamente.")
}

fun modificarBIN() {
    println("=== MODIFICAR REGISTRO ===")
    val id = pedirIdValido("Introduce el ID del videojuego: ")

    RandomAccessFile(RUTA_BIN.toFile(), "rw").use { raf ->
        val pos = buscarPosicion(raf, id)
        if (pos == -1) {
            println("Error: No existe ningún videojuego con el ID $id. No se ha realizado ningún cambio.")
            return
        }

        raf.seek(pos.toLong() * TAM_REGISTRO)
        val actual = leerRegistro(raf)
        println("Videojuego encontrado: ${actual.titulo} (nota actual: ${actual.nota})")

        val nuevaNota = pedirNota("Introduce la nueva nota (0.0 - 10.0): ")

        // Nos colocamos justo en el campo nota de ese registro y solo se sobrescribe ese campo
        raf.seek(pos.toLong() * TAM_REGISTRO + OFFSET_NOTA)
        raf.writeDouble(nuevaNota)
    }
    println("Nota actualizada correctamente.")
}

fun eliminarBIN() {
    println("=== ELIMINAR REGISTRO ===")
    val id = pedirIdValido("Introduce el ID del videojuego a eliminar: ")

    RandomAccessFile(RUTA_BIN.toFile(), "rw").use { raf ->
        val pos = buscarPosicion(raf, id)
        if (pos == -1) {
            println("Error: No existe ningún videojuego con el ID $id.")
            return
        }

        raf.seek(pos.toLong() * TAM_REGISTRO)
        val actual = leerRegistro(raf)
        println("Videojuego encontrado: ${actual.titulo}")
        print("¿Estás seguro de que quieres eliminar '${actual.titulo}'? (s/n): ")
        val respuesta = readlnOrNull()?.trim()?.lowercase()

        if (respuesta != "s") {
            println("Operación cancelada. No se ha eliminado nada.")
            return
        }

        // Subimos un hueco todos los registros posteriores y recortamos el fichero
        val total = numRegistros(raf)
        val buffer = ByteArray(TAM_REGISTRO)
        for (i in pos until total - 1) {
            raf.seek((i + 1).toLong() * TAM_REGISTRO)
            raf.readFully(buffer)
            raf.seek(i.toLong() * TAM_REGISTRO)
            raf.write(buffer)
        }
        raf.setLength((total - 1).toLong() * TAM_REGISTRO)
    }
    println("Videojuego eliminado correctamente.")
}