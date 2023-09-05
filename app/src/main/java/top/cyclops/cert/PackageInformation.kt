package top.cyclops.cert

import android.graphics.drawable.Drawable

data class PackageInformation(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    val signatures: List<PackageSignature>,
)

