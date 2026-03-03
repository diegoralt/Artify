package com.drkings.artify.data.datasource.api

import android.content.Context
import com.drkings.artify.R
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

fun buildTrustManager(context: Context): X509TrustManager {
    // Cadena real que envía el servidor Discogs:
    // discogs.com  →  E7 (cross-firmado por ISRG Root X1)  →  ISRG Root X1
    //
    // Android API 24 (7.0) NO incluye ISRG Root X1 en su trust store.
    // Fue agregado desde Android 7.1.1 (API 25).
    // Por eso se bundlea aquí junto con ISRG Root X2 para cubrir ambas cadenas.
    val customCerts = listOf(
        loadCertificate(context, R.raw.isrg_root_x1), // Requerido: firma E7-cross → discogs.com
        loadCertificate(context, R.raw.isrg_root_x2) // Respaldo: firma E7 directo
    )

    val androidCAStore = KeyStore.getInstance("AndroidCAStore").apply {
        load(null)
    }

    val combinedKeyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
        load(null, null)

        // Copia los certificados del sistema Android
        androidCAStore.aliases().asSequence().forEach { alias ->
            try {
                val cert = androidCAStore.getCertificate(alias)
                if (cert != null) setCertificateEntry(alias, cert)
            } catch (e: Exception) {
                // Ignorar certificados que no se puedan leer
            }
        }

        // Agrega los certificados Let's Encrypt que faltan en API 24
        customCerts.forEachIndexed { index, cert ->
            setCertificateEntry("custom_cert_$index", cert)
        }
    }

    val trustManagerFactory = TrustManagerFactory
        .getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(combinedKeyStore)
        }

    return trustManagerFactory.trustManagers
        .filterIsInstance<X509TrustManager>()
        .first()
}

private fun loadCertificate(context: Context, resId: Int): X509Certificate {
    return context.resources.openRawResource(resId).use { inputStream ->
        CertificateFactory.getInstance("X.509")
            .generateCertificate(inputStream) as X509Certificate
    }
}
