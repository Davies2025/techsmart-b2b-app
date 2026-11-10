package com.example.techsmart.data.repository

import com.example.techsmart.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Un mensaje tal como lo espera el backend de la web: sender = "user" | "bot". */
data class MensajeApi(val sender: String, val text: String)

/**
 * Cliente del Asesor IA. Usa el mismo endpoint que la web (POST /api/chat):
 *   request:  { "messages": [ { "sender": "user" | "bot", "text": "..." } ] }
 *   response: { "reply": "..." }
 *
 * La URL sale de BuildConfig.CHAT_API_URL (se configura en local.properties con chat.api.url).
 * Por defecto apunta a 10.0.2.2, que es la máquina anfitriona vista desde el emulador.
 */
class ChatRepository(
    private val url: String = BuildConfig.CHAT_API_URL
) {
    suspend fun enviar(historial: List<MensajeApi>): Result<String> = withContext(Dispatchers.IO) {
        var conexion: HttpURLConnection? = null
        try {
            val cuerpo = JSONObject().put(
                "messages",
                JSONArray().apply {
                    historial.forEach { put(JSONObject().put("sender", it.sender).put("text", it.text)) }
                }
            ).toString()

            conexion = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 45_000 // la IA puede tardar en responder
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }
            conexion.outputStream.use { it.write(cuerpo.toByteArray(Charsets.UTF_8)) }

            val codigo = conexion.responseCode
            if (codigo !in 200..299) {
                return@withContext Result.failure(IllegalStateException("Error en el servidor: $codigo"))
            }
            val texto = conexion.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val reply = JSONObject(texto).optString("reply").trim()
            if (reply.isEmpty()) Result.failure(IllegalStateException("Respuesta vacía del asesor"))
            else Result.success(reply)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            conexion?.disconnect()
        }
    }
}
