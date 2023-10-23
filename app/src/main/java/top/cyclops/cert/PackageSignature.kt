package top.cyclops.cert

import java.security.interfaces.RSAPublicKey

data class PackageSignature(
    val signature: ByteArray,
    val publicKey: RSAPublicKey?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PackageSignature

        if (!signature.contentEquals(other.signature)) return false
        return publicKey == other.publicKey
    }

    override fun hashCode(): Int {
        var result = signature.contentHashCode()
        result = 31 * result + publicKey.hashCode()
        return result
    }
}