import java.nio.file.Path

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

fun buscarPorId(lista: List<Videojuego>) {
    println("=== BÚSQUEDA DE VIDEOJUEGO ===")
    print("Introduce el ID a buscar: ")
    // toIntOrNull() intenta convertir a Int si meten letras devuelve null en lugar de petar el programa
    val id = readlnOrNull()?.toIntOrNull()

    if (id == null) {
        println("ID inválido")
        return
    }

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

    var nota: Double? = null
    while (nota == null || nota < 0.0 || nota > 10.0) {
        print("Nota (0.0 - 10.0): ")
        nota = readlnOrNull()?.toDoubleOrNull()
        if (nota == null || nota < 0.0 || nota > 10.0) {
            println("Nota inválida.")
        }
    }

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

fun modificarNota(lista: List<Videojuego>): List<Videojuego> {
    println("=== MODIFICAR NOTA ===")
    print("Introduce el ID del videojuego: ")
    val id = readlnOrNull()?.toIntOrNull()

    if (id == null) {
        println("ID inválido.")
        return lista
    }

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

    var nuevaNota: Double? = null
    while (nuevaNota == null || nuevaNota < 0.0 || nuevaNota > 10.0) {
        print("Introduce la nueva nota (0.0 - 10.0): ")
        nuevaNota = readlnOrNull()?.toDoubleOrNull()
        if (nuevaNota == null || nuevaNota < 0.0 || nuevaNota > 10.0) {
            println("Nota inválida.")
        }
    }

    println("Nota actualizada correctamente.")
    // Clona el objeto cambiando la nota, y si no coincide se devuelve el objeto tal cual
    return lista.map {
        if (it.id == id) it.copy(nota = nuevaNota) else it
    }
}

fun eliminarVideojuego(lista: List<Videojuego>): List<Videojuego> {
    println("=== ELIMINAR VIDEOJUEGO ===")
    print("Introduce el ID del videojuego a eliminar: ")
    val id = readlnOrNull()?.toIntOrNull()

    if (id == null) {
        println("ID inválido.")
        return lista
    }

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

fun integrarJSON(catalogoActual: List<Videojuego>, rutaJSON: Path): List<Videojuego> {
    println("=== INTEGRAR DESDE JSON ===")

    val juegosJSON = leerVideojuegosJSON(rutaJSON)

    if (juegosJSON.isEmpty()) {
        println("No se encontraron videojuegos o el archivo está vacio")
        return catalogoActual
    }

    println("Videojuegos encontrados en el JSON:")
    juegosJSON.forEach { v ->
        println("${v.id} | ${v.titulo} | ${v.genero} | ${v.anio} | ${v.nota} ")
    }

    val idExistentes = catalogoActual.map { it.id }.toSet()
    val juegosSinDuplicar = juegosJSON.filter { it.id !in idExistentes }

    val duplicadosIgnorados = juegosJSON.size - juegosSinDuplicar.size
    if (duplicadosIgnorados > 0) {
        println("Se han ignorado $duplicadosIgnorados videojuegos por tener un ID duplicado")
    }

    if (juegosSinDuplicar.isEmpty()) {
        println("No hay videojuegos nuevos para añadir")
        return catalogoActual
    }

    println("Se han añadido ${juegosSinDuplicar.size} videojuegos nuevos al catálogo")
    return catalogoActual + juegosSinDuplicar
}

fun integrarXML(catalogoActual: List<Videojuego>, rutaXML: Path): List<Videojuego> {
    println("=== INTEGRAR DESDE XML ===")

    val juegosXML = leerVideojuegosXML(rutaXML)

    if (juegosXML.isEmpty()) {
        println("No se encontraron videojuegos en el archivo XML")
        return catalogoActual
    }

    println("Videojuegos leídos desde XML:")
    juegosXML.forEach { v ->
        println("${v.id} | ${v.titulo} | ${v.genero} | ${v.anio} | ${v.nota} ")
    }

    val idExistentes = catalogoActual.map { it.id }.toSet()
    val juegosSinDuplicar = juegosXML.filter { it.id !in idExistentes }

    val duplicadosIgnorados = juegosXML.size - juegosSinDuplicar.size
    if (duplicadosIgnorados > 0) {
        println("Se han ignorado $duplicadosIgnorados videojuegos por tener un ID duplicado")
    }

    if (juegosSinDuplicar.isEmpty()) {
        println("Todos los videojuegos del XML ya se encuentran en el catálogo actual")
        return catalogoActual
    }

    println("Se han integrado ${juegosSinDuplicar.size} videojuegos nuevos")
    return catalogoActual + juegosSinDuplicar
}
