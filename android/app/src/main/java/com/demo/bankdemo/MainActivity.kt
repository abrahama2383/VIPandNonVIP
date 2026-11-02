package com.demo.bankdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dynatrace.agent.api.SessionPropertyEventData
import com.dynatrace.android.agent.Dynatrace
import com.dynatrace.android.agent.conf.DataCollectionLevel
import com.dynatrace.android.agent.conf.UserPrivacyOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Demo only: a real app must ask the user before opting in.
        Dynatrace.applyUserPrivacyOptions(
            UserPrivacyOptions.builder()
                .withDataCollectionLevel(DataCollectionLevel.USER_BEHAVIOR)
                .withCrashReportingOptedIn(true)
                .withScreenRecordOptedIn(true)
                .build()
        )
        setContent {
            MaterialTheme {
                var user by remember { mutableStateOf<BankUser?>(null) }
                Surface(Modifier.fillMaxSize().safeDrawingPadding()) {
                    val current = user
                    if (current == null) LoginScreen { tagSession(it); user = it }
                    else HomeScreen(current) { Dynatrace.endVisit(); user = null }
                }
            }
        }
    }
}

/** Tags the Dynatrace session with the username and the VIP/Regular category. */
private fun tagSession(user: BankUser) {
    Dynatrace.identifyUser(user.username)
    // New RUM (Grail): session property. The key must start with "session_properties.".
    Dynatrace.sendSessionPropertyEvent(
        SessionPropertyEventData().addSessionProperty("session_properties.category", user.category.label)
    )
    // Classic RUM: action value. Map "category" to a session property in the app settings.
    val action = Dynatrace.enterAction("Login")
    action.reportValue("category", user.category.label)
    action.leaveAction()
}

@Composable
fun LoginScreen(onLogin: (BankUser) -> Unit) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🏦", fontSize = 56.sp)
        Text("Demo Bank", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = username, onValueChange = { username = it },
            label = { Text("Username") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val u = authenticate(username, password)
                if (u != null) onLogin(u) else error = "Invalid username or password"
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
        ) { Text("Login") }
    }
}

@Composable
fun HomeScreen(user: BankUser, onLogout: () -> Unit) {
    val accent = if (user.category == Category.VIP) Color(0xFFBF9A33) else Color(0xFF1565C0)

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Home", fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = onLogout) { Text("Logout") }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Welcome, ${user.username}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    user.category.label, color = accent, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                    modifier = Modifier
                        .background(accent.copy(alpha = 0.2f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        Section("Savings Account", listOf("Account No." to "1234 5678 9012", "Balance" to "Rp 25.000.000"))
        Section(
            "Recent Transactions",
            listOf(
                "Transfer to Budi" to "- Rp 500.000",
                "Salary" to "+ Rp 10.000.000",
                "Electricity bill" to "- Rp 350.000",
            ),
        )
    }
}

@Composable
private fun Section(title: String, rows: List<Pair<String, String>>) {
    Text(title, color = Color.Gray, fontWeight = FontWeight.SemiBold)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            rows.forEach { (k, v) ->
                Row(Modifier.fillMaxWidth()) {
                    Text(k, Modifier.weight(1f))
                    Text(v, color = Color.Gray)
                }
            }
        }
    }
}
