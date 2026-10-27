# Catálogo de Videojuegos

Programa de consola en Kotlin para gestionar un catálogo de videojuegos.
Permite leer y guardar los datos en un fichero CSV, importarlos desde XML y JSON, convertir entre los tres formatos y gestionar un fichero binario de acceso aleatorio con un CRUD completo.

## 1. Estructura de datos

### **Data Class:**

```kotlin
@Serializable
data class Videojuego(
    val id: Int,
    val titulo: String,
    val genero: String,
    val anio: Int,
    val nota: Double
)
```

La misma clase se usa para los cuatro formatos (CSV, XML, JSON y binario).

### **Ficheros de intercambio:**

- **CSV** (`videojuegos.csv`): una fila por videojuego, campos separados por `;` y en este orden: `id;titulo;genero;anio;nota`.
- **JSON** (`videojuegos.json`): una lista de objetos con las claves `id`, `titulo`, `genero`, `anio` y `nota`.
- **XML** (`videojuegos.xml`): etiqueta raíz `<videojuegos>` con una etiqueta `<videojuego>` por cada juego y dentro una etiqueta por campo.

Ejemplo de una fila del CSV:

```
1;The Legend of Zelda: Breath of the Wild;Aventura;2017;9.5
```

### **Estructura del registro binario:**

- **id**: Int - 4 bytes
- **titulo**: String - 40 caracteres de longitud fija (2 bytes cada uno) = 80 bytes
- **genero**: String - 20 caracteres de longitud fija (2 bytes cada uno) = 40 bytes
- **anio**: Int - 4 bytes
- **nota**: Double - 8 bytes
- **Tamaño total del registro**: 4 + 80 + 40 + 4 + 8 = **136 bytes**

Posición de cada campo dentro de un registro:

| Campo  | Desde el byte | Tamaño   |
|--------|---------------|----------|
| id     | 0             | 4 bytes  |
| titulo | 4             | 80 bytes |
| genero | 84            | 40 bytes |
| anio   | 124           | 4 bytes  |
| nota   | 128           | 8 bytes  |

Los textos se rellenan con espacios hasta completar la longitud fija (si son más largos se cortan) y al leerlos se quitan los espacios sobrantes del final.

### **Carpetas y ficheros:**

```
datos/
├── datos_ini/        <- ficheros de entrada: videojuegos.csv, videojuegos.xml, videojuegos.json
├── datos_fin/        <- ficheros que genera el menú de conversiones
└── videojuegos.bin   <- fichero binario de acceso aleatorio
```

## 2. Instrucciones de ejecución

- **Requisitos previos**: JDK 23 instalado (el proyecto usa `jvmToolchain(23)`).
- **Compilación**: abre el proyecto en IntelliJ IDEA y deja que Gradle sincronice las dependencias (`build.gradle.kts`). Se usan kotlin-csv, Jackson XML (con el módulo de Kotlin) y kotlinx.serialization para JSON.
- **Ficheros de datos iniciales**: coloca `videojuegos.csv` en la carpeta `datos/datos_ini` antes de ejecutar. Para las opciones de XML y JSON también hacen falta `videojuegos.xml` y `videojuegos.json` en esa misma carpeta. Las carpetas `datos_ini` y `datos_fin` se crean solas al arrancar si no existen.
- **Ejecución**: ejecuta la función `main` de `Main.kt`. Se muestra este menú:

```
===== CATÁLOGO DE VIDEOJUEGOS =====
1. Gestión CSV
2. Leer catálogo desde XML
3. Leer catálogo desde JSON
4. Conversiones entre formatos
5. Gestión fichero BIN
0. Salir
====================================
```

- **1. Gestión CSV**: listar, buscar por ID, añadir, modificar la nota y eliminar. Cada cambio se guarda en el CSV.
- **2 y 3. Leer desde XML / JSON**: añaden al catálogo los videojuegos del fichero que no estén ya (se ignoran los ID repetidos) y guardan el resultado en el CSV.
- **4. Conversiones**: convierte entre CSV, JSON y XML. Lee de `datos_ini` y escribe en `datos_fin`.
- **5. Gestión fichero BIN**: importar desde el CSV (vacía el `.bin` y lo vuelve a crear), leer, añadir un registro nuevo, modificar la nota de un registro por ID y eliminar un registro por ID.

### Organización del código

- `Main.kt`: menú principal y submenú del CSV.
- `Videojuego.kt`: la data class.
- `VideojuegoRepository.kt`: lectura y escritura de CSV, JSON y XML, y las conversiones.
- `VideojuegoService.kt`: operaciones sobre la lista (listar, buscar, añadir, modificar, eliminar e integrar).
- `VideojuegoBin.kt`: CRUD del fichero binario.
- `Creacion.kt`: creación de las carpetas del proyecto.

## 3. Decisiones de diseño

- Elegí el CSV como formato principal porque es sencillo de crear y editar con cualquier hoja de cálculo, y uso `;` como separador porque es el que usa Excel en España y evita problemas con las comas.
- Todos los formatos pasan por la lista de `Videojuego` en memoria. Así la función `convertir` sirve para las seis conversiones: solo hay que decirle qué función lee y cuál escribe.
- Las funciones que modifican el catálogo no cambian la lista original, devuelven una lista nueva. Después se guarda en el CSV.
- Para el fichero binario uso `RandomAccessFile` porque hace falta saltar a la posición de un registro (`seek`) y con `DataOutputStream` solo se puede leer y escribir en orden.
- Reservé 40 caracteres para el título porque el más largo del catálogo, "The Legend of Zelda: Breath of the Wild", tiene 39. Reservé 20 para el género porque el más largo ("Plataformas") tiene 11 y así hay margen.
- Cada carácter ocupa 2 bytes porque los textos se escriben con `writeChars`, de ahí que el registro mida 136 bytes.
- Todos los registros miden lo mismo, así que la posición de uno es su número por 136 y se puede saltar directamente a él.
- Al modificar un registro solo se sobrescribe el campo `nota` (8 bytes, a partir del byte 128 del registro), sin reescribir el resto.
- Al eliminar un registro, los siguientes suben una posición y se recorta el fichero. Así no quedan huecos.
- El fichero se llama `videojuegos.bin` porque es un fichero binario con los datos de los videojuegos.
- Al importar al binario, si el CSV no existe o está vacío no se toca el `.bin`, para no perder datos por error.
- En el CSV el ID nuevo es el mayor existente más uno. En el binario es el usuario quien lo elige y se comprueba que sea un número y que no exista ya.
- Al pedir una nota se repite la pregunta hasta que sea un número entre 0 y 10 (se acepta coma o punto), y lo mismo con el ID y el año.
