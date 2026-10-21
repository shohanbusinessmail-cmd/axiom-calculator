package com.axiom.calculator.feature.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.axiom.calculator.BuildConfig
import com.axiom.calculator.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(topBar = {
        TopAppBar(title = { Text("About") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        })
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(24.dp)) {
            Text(stringResourceCompat(context, R.string.app_name), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text("Version ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Text(
                "Axiom Calculator is a complete, fully offline calculator: standard and scientific " +
                    "arithmetic, formula calculators, equation and statistics solvers, unit and programmer " +
                    "tools, and a local calculation history and library. It performs no network requests, " +
                    "shows no advertising, and requires no account.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(24.dp))
            Text("Developer", style = MaterialTheme.typography.titleLarge)
            Text(stringResourceCompat(context, R.string.developer_name), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val email = context.getString(R.string.developer_email)
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                    putExtra(Intent.EXTRA_SUBJECT, "Axiom Calculator feedback")
                }
                runCatching {
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        android.widget.Toast.makeText(context, "No email app is installed. Contact: $email", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }) { Text("Contact developer") }
            Spacer(Modifier.height(24.dp))
            Text("Privacy", style = MaterialTheme.typography.titleLarge)
            Text(
                "All calculations, history, and settings are stored only in this app's local storage on " +
                    "your device. Axiom Calculator requests no Android permissions, including no internet " +
                    "permission, and contains no analytics, advertising, or crash-reporting SDKs.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun stringResourceCompat(context: android.content.Context, id: Int): String = context.getString(id)
