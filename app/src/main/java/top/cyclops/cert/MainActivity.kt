@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package top.cyclops.cert

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageItemInfo
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.PermissionChecker
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.launch
import top.cyclops.cert.ui.theme.CertificatesTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (PermissionChecker.checkSelfPermission(
                    this,
                    Manifest.permission.QUERY_ALL_PACKAGES
                ) != PermissionChecker.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "权限不足", Toast.LENGTH_LONG).show()
            }
        }
        lifecycleScope.launch {
            viewModel.refreshPackage(this@MainActivity)
        }
        setContent {
            CertificatesTheme {
                MainContent(viewModel)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(vm: MainViewModel) {
    val packages by vm.packagesStream.collectAsState()
    Scaffold(topBar = {
        MediumTopAppBar(title = {
            Text(text = "Certificates")
        })
    }) { padding ->
        var showCopyContent by remember {
            mutableStateOf<PackageInformation?>(null)
        }
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(packages, key = { it.packageName }) { info ->
                PackageItem(info, modifier = Modifier.fillParentMaxWidth(), onCopyClick = {
                    showCopyContent = it
                })
            }
        }
        if (showCopyContent != null) {
            CopyDialog({ showCopyContent = null }, showCopyContent)
        }
    }
}

@Composable
fun PackageItem(
    info: PackageInformation,
    modifier: Modifier = Modifier,
    onCopyClick: (PackageInformation) -> Unit,
) {

    Card(
        modifier = modifier
            .padding(4.dp)
            .padding(horizontal = 4.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            val icon = rememberDrawablePainter(info.icon)
            Image(
                painter = icon,
                modifier = Modifier.size(48.dp),
                contentDescription = ""
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, top = 4.dp)
            ) {
                Text(
                    text = info.name,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = info.packageName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                TextButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = {
                        onCopyClick(info)
                    }
                ) {
                    Text(text = "Copy")
                }
            }
        }
    }

}


@Composable
fun CopyDialog(onDismissRequest: () -> Unit, information: PackageInformation?) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val copy = { text: String ->
        val cm: ClipboardManager =
            context
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val mClipData = ClipData.newPlainText("", text)
        cm.setPrimaryClip(mClipData)
        Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show()
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismissRequest()
            }
        }
    }
    if (information != null) {
        var showSeparator by remember {
            mutableStateOf(false)
        }
        var uppercase by remember {
            mutableStateOf(false)
        }
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            dragHandle = null,
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 24.dp),
                text = information.name,
                style = MaterialTheme.typography.headlineSmall
            )
            Row(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = uppercase, onCheckedChange = { uppercase = it })
                Text(text = "Uppercase", style = MaterialTheme.typography.labelSmall)
                Checkbox(
                    modifier = Modifier.padding(start = 16.dp),
                    checked = showSeparator,
                    onCheckedChange = { showSeparator = it })
                Text(text = "Separator", style = MaterialTheme.typography.labelSmall)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        copy(
                            information.signatures.first().signature.md5(
                                showSeparator,
                                uppercase
                            )
                        )
                    }) {
                    Text(text = "MD5")
                }
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        copy(
                            information.signatures.first().signature.sha1(
                                showSeparator,
                                uppercase
                            )
                        )
                    }) {
                    Text(text = "SHA1")
                }
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        copy(
                            information.signatures.first().signature.sha256(
                                showSeparator,
                                uppercase
                            )
                        )
                    }) {
                    Text(text = "SHA256")
                }
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        copy(
                            information.signatures.first().publicKey.modulus.toByteArray()
                                .toHexString(showSeparator, uppercase)
                                .removePrefix("00:")
                        )

                    }) {
                    Text(text = "PUBLIC KEY")
                }
            }

        }
    }
}