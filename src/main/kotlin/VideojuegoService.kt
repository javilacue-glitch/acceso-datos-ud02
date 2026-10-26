import java.nio.file.Path

fun pedirId(mensaje: String): Int? {
    print(mensaje)
    val id = readlnOrNull()?.trim()?.toIntOrNull()
    if (id == null) println("ID inválido.")
    return id
}

fun pedirNota(mensaje: String): Double {
    while (true) {
        print(mensaje)
        val nota = readlnOrNull()?.trim()?.replace(',', '.')?.toDoubleOrNull()
        if (nota != null && nota in 0.0..10.0) return nota
        println("Nota inválida.")
    }
}

fun listarVideojuegos(lista: List<Videojuego>) {
    println("=== LISTADO DE VIDEOJUEGOS ===")
    if (lista.isEmpty()) {
        println("El catálogo está vacío.")
    } else {
        lista.forEach { v ->
            println("${v.id} | ${v.titulo} | ${v.genero} | Año: ${v.anio} | Nota: ${v.nota}")
        }
    }
}

fun buscarPorId(lista: List<Videojuego>, id: Int) {
    println("=== BÚSQUEDA DE VIDEOJUEGO ===")
    // Uso el .find para que devuelva el primer elemento que cumpla con la condicion que pedimos
    // en caso de que no haya ninguno se devuelve null
    val juego = lista.find { it.id == id }
    if (juego != null) {
        println("Encontrado: [ID: ${juego.id}] ${juego.titulo} | ${juego.genero} | Año: ${juego.anio} | Nota: ${juego.nota}")
    } else {
        println("Error: No existe ningún videojuego con el ID $id.")
    }
}

fun añadirVideojuego(lista: List<Videojuego>): List<Videojuego> {
    println("=== AÑADIR NUEVO VIDEOJUEGO ===")
    print("Título: ")
    //?: asigna un valor por defecto si readlnOrNull() devuelve null
    var titulo = readlnOrNull() ?: ""
    if (titulo.trim().isEmpty()) titulo = "Desconocido"

    print("Género: ")
    var genero = readlnOrNull() ?: ""
    if (genero.trim().isEmpty()) genero = "Desconocido"

    print("Año: ")
    var anio = readlnOrNull()?.toIntOrNull()
    while (anio == null) {
        println("Año inválido. Introduce un número entero.")
        print("Año: ")
        anio = readlnOrNull()?.toIntOrNull()
    }

    val nota = pedirNota("Nota (0.0 - 10.0): ")   // ← sustituye al bloque while de la nota

    // Buscamos el ID más alto actual para asignar el siguiente
    var maxId = 0
    for (j in lista) {
        if (j.id > maxId) {
            maxId = j.id
        }
    }

    val nuevoId = maxId + 1
    val nuevoJuego = Videojuego(nuevoId, titulo, genero, anio, nota)

    println("Videojuego añadido correctamente.")
    // Devuelve una lista nueva cada vez que se añada un juego
    return lista + nuevoJuego
}

fun modificarNota(lista: List<Videojuego>, id: Int, nuevaNota: Double): List<Videojuego> {
    println("=== MODIFICAR NOTA ===")

    var existe = false
    for (juego in lista) {
        if (juego.id == id) {
            existe = true
            break
        }
    }

    if (!existe) {
        println("Error: No existe ningún videojuego con el ID $id.")
        return lista
    }

    println("Nota actualizada correctamente.")
    // Clona el objeto cambiando la nota, y si no coincide se devuelve el objeto tal cual
    return lista.map {
        if (it.id == id) it.copy(nota = nuevaNota) else it
    }
}

fun eliminarVideojuego(lista: List<Videojuego>, id: Int): List<Videojuego> {
    println("=== ELIMINAR VIDEOJUEGO ===")

    val juego = lista.find { it.id == id }
    if (juego == null) {
        println("Error: No existe ningún videojuego con el ID $id.")
        return lista
    }

    print("¿Estás seguro de que quieres eliminar '${juego.titulo}'? (s/n): ")
    val respuesta = readlnOrNull()?.lowercase()

    if (respuesta == "s") {
        println("Videojuego eliminado correctamente.")
        // Devuelve una lista nueva solo con los objetos que cumplan la condición
        return lista.filter { it.id != id }
    } else {
        println("Operación cancelada.")
        return lista
    }
}

fun integrarCatalogo(catalogoActual: List<Videojuego>, nuevos: List<Videojuego>): List<Videojuego> {
    if (nuevos.isEmpty()) {
        println("No se encontraron videojuegos o el archivo está vacío")
        return catalogoActual
    }
    nuevos.forEach { v -> println("${v.id} | ${v.titulo} | ${v.genero} | ${v.anio} | ${v.nota}") }

    val idExistentes = catalogoActual.map { it.id }.toSet()
    val sinDuplicar = nuevos.filter { it.id !in idExistentes }

    val duplicados = nuevos.size - sinDuplicar.size
    if (duplicados > 0) println("Se han ignorado $duplicados videojuegos por tener un ID duplicado")

    if (sinDuplicar.isEmpty()) {
        println("No hay videojuegos nuevos que integrar")
        return catalogoActual
    }
    println("Se han integrado ${sinDuplicar.size} videojuegos nuevos en el catálogo")
    return catalogoActual + sinDuplicar
}