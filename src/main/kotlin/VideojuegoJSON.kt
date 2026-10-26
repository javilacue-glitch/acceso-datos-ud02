import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideojuegoJSON(
    @SerialName("id") val id: Int,
    @SerialName("titulo") val titulo: String,
    @SerialName("genero") val genero: String,
    @SerialName("anio") val anio: Int,
    @SerialName("nota") val nota: Double

)
