@file:Suppress("unused")
@file:OptIn(ExperimentalStdlibApi::class)

package top.cyclops.cert

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

fun ByteArray.sha1(separator: Boolean = false, uppercase: Boolean = false):String{
    val digest = MessageDigest.getInstance("SHA1")
    return digest.digest(this).toHexString(separator,uppercase)
}


fun ByteArray.sha256(separator: Boolean = false, uppercase: Boolean = false):String{
    val digest = MessageDigest.getInstance("SHA256")
    return digest.digest(this).toHexString(separator,uppercase)
}


fun ByteArray.md5(separator: Boolean = false, uppercase: Boolean = false): String {
    val digest = MessageDigest.getInstance("MD5")
    return digest.digest(this).toHexString(separator,uppercase)
}

fun String.md5(separator: Boolean = false, uppercase: Boolean = false): String {
    return this.toByteArray().md5(separator,uppercase)
}
suspend fun File.md5(separator: Boolean = false, uppercase: Boolean = false): String =
    withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("MD5")
        inputStream().use {
            val data = ByteArray(10240)
            var count = 0
            do {
                if (count > 0) {
                    digest.update(data, 0, count)
                }
                count = it.read(data)
            } while (count > 0)
        }
        digest.digest().toHexString(separator, uppercase)
    }

fun ByteArray.toHexString(separator: Boolean = false, uppercase: Boolean = false): String {
    return this.toHexString(HexFormat {
        upperCase = uppercase
        bytes {
            if (separator) {
                byteSeparator = ":"
            }
        }
    })
}