package com.drkings.artify.data.datasource.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * ## Estrategia de dos niveles
 *
 * ### Nivel 1 — Throttle proactivo (pre-request)
 * Cuando `Remaining ≤ THROTTLE_THRESHOLD`, se aplica un retardo ANTES de enviar
 * la siguiente solicitud para evitar llegar al límite:
 * - Intervalo mínimo calculado como `WINDOW_DURATION_MS / limit` (≈ 1 000 ms con 60 req/min).
 * - Si el tiempo transcurrido desde la última solicitud ya supera el intervalo,
 *   el retardo es 0 (no se penaliza el flujo normal).
 *
 * ### Nivel 2 — Retry reactivo (post-429)
 * Si el servidor responde con **429 Too Many Requests**:
 * 1. Usa `WINDOW_DURATION_MS / limit` × factor de backoff.
 * 3. Cierra el body de la respuesta, espera y reintenta hasta [MAX_RETRIES] veces.
 * 4. Si tras [MAX_RETRIES] sigue siendo 429, propaga el error hacia el UseCase.
 */
class RateLimitInterceptor : Interceptor {

    private val remaining = AtomicInteger(REQUESTS_PER_MINUTE)
    private val limit = AtomicInteger(REQUESTS_PER_MINUTE)
    private val lastRequestAt = AtomicLong(0L)

    override fun intercept(chain: Interceptor.Chain): Response {
        applyPreemptiveThrottle()

        val request = chain.request()
        val response = chain.proceed(request)
        lastRequestAt.set(System.currentTimeMillis())

        updateStateFromHeaders(response)

        return if (response.code == HTTP_429) {
            retryOnRateLimit(chain, request, response)
        } else {
            response
        }
    }

    // ── Nivel 1: throttle proactivo ───────────────────────────────────────────
    private fun applyPreemptiveThrottle() {
        val currentRemaining = remaining.get()
        if (currentRemaining > THROTTLE_THRESHOLD) return

        val currentLimit = limit.get().coerceAtLeast(1)
        val minIntervalMs = WINDOW_DURATION_MS / currentLimit
        val elapsed = System.currentTimeMillis() - lastRequestAt.get()
        val delayMs = (minIntervalMs - elapsed).coerceAtLeast(0L)

        if (delayMs > 0) {
            sleepSafe(delayMs)
        }
    }

    // ── Lectura de headers ────────────────────────────────────────────────────

    /**
     * Actualiza el estado interno con los valores devueltos por Discogs en cada respuesta.
     * Los tres headers son opcionales; si alguno falta se conserva el valor anterior.
     */
    private fun updateStateFromHeaders(response: Response) {
        val newLimit = response.header(HEADER_RATELIMIT)?.toIntOrNull()
        val newRemaining = response.header(HEADER_RATELIMIT_REMAINING)?.toIntOrNull()

        if (newLimit != null) limit.set(newLimit)
        if (newRemaining != null) remaining.set(newRemaining)

        Log.d(TAG, "Headers: límite=${newLimit ?: "–"}, restantes=${newRemaining ?: "–"}")
    }

    // ── Nivel 2: retry reactivo al 429 ───────────────────────────────────────

    private fun retryOnRateLimit(
        chain: Interceptor.Chain,
        request: Request,
        initialResponse: Response
    ): Response {
        var response = initialResponse
        var retries = 0

        while (response.code == HTTP_429 && retries < MAX_RETRIES) {
            val waitMs = calculateReactiveWaitMs(retries)

            response.close()

            Log.w(TAG, "429 recibido. Reintento ${retries + 1}/$MAX_RETRIES en ${waitMs}ms")

            sleepSafe(waitMs)

            response = chain.proceed(request)
            lastRequestAt.set(System.currentTimeMillis())
            updateStateFromHeaders(response)
            retries++
        }

        if (response.code == HTTP_429) {
            Log.e(TAG, "Rate limit persistente tras $MAX_RETRIES reintentos. Propagando 429.")
        }

        return response
    }

    /**
     * Calcula el tiempo de espera reactivo.
     *
     * Usa un backoff creciente basado en el intervalo de la ventana:
     * - Reintento 0: 1 intervalo (~1 000 ms con 60 req/min)
     * - Reintento 1: 3 intervalos (~3 000 ms)
     * - Reintento 2: ventana completa (60 000 ms) → garantiza el reset del límite
     */
    private fun calculateReactiveWaitMs(retryIndex: Int): Long {
        val currentLimit = limit.get().coerceAtLeast(1)
        val intervalMs = WINDOW_DURATION_MS / currentLimit
        return when (retryIndex) {
            0 -> intervalMs
            1 -> intervalMs * RETRY_FACTOR_MEDIUM
            else -> WINDOW_DURATION_MS // Espera completa: la ventana seguro se resetea
        }
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    /**
     * Suspende el thread de OkHttp. Si el thread es interrumpido (ej. cancelación
     * de coroutine que cerró el socket), relanza como [IOException] para que
     * la cadena de OkHttp lo propague correctamente.
     */
    private fun sleepSafe(ms: Long) {
        try {
            Thread.sleep(ms)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IOException("Espera de rate limit interrumpida", e)
        }
    }

    private companion object {
        const val TAG = "RateLimitInterceptor"

        /** Límite de Discogs: 60 solicitudes por minuto. */
        const val REQUESTS_PER_MINUTE = 60

        /** Duración de la ventana en milisegundos: 60 000 ms = 1 minuto. */
        const val WINDOW_DURATION_MS = 60_000L

        /** Por debajo de este umbral se activa el throttle proactivo. */
        const val THROTTLE_THRESHOLD = 10

        /** Número máximo de reintentos ante un 429. */
        const val MAX_RETRIES = 3

        /** Factor de espera para el reintento intermedio (1× → 3× → ventana completa). */
        const val RETRY_FACTOR_MEDIUM = 3L

        const val HTTP_429 = 429

        /** Total de solicitudes permitidas en la ventana. */
        const val HEADER_RATELIMIT = "X-Discogs-Ratelimit"

        /** Solicitudes restantes en la ventana actual. */
        const val HEADER_RATELIMIT_REMAINING = "X-Discogs-Ratelimit-Remaining"
    }
}
