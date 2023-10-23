package top.cyclops.cert

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey

class MainViewModel : ViewModel() {
    private val _packagesStream = MutableStateFlow(emptyList<PackageInformation>())
    val packagesStream = _packagesStream.asStateFlow()
    fun refreshPackage(context: Context) {
        val pm = context.packageManager
        _packagesStream.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pm.getInstalledPackages(PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(PackageManager.GET_SIGNATURES)
        }.filter { it.applicationInfo.sourceDir.startsWith("/data/app/") }
            .map { it.convert(pm) }
            .sortedBy { it.name }
    }


    private fun PackageInfo.convert(packageManager: PackageManager): PackageInformation {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            signingInfo.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            signatures
        }.map { it.convert() }
        return PackageInformation(
            name = applicationInfo.loadLabel(packageManager).toString(),
            packageName = packageName,
            icon = applicationInfo.loadIcon(packageManager),
            signatures = signatures
        )
    }


    private fun Signature.convert(): PackageSignature {
        val signature = this.toByteArray()
        return PackageSignature(
            signature = signature,
            publicKey = signature.inputStream()
                .use { cert ->
                    (CertificateFactory.getInstance("X509")
                        .generateCertificate(cert) as X509Certificate)
                        .publicKey as? RSAPublicKey
                }
        )
    }

}