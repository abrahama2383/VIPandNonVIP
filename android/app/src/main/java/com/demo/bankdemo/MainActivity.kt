package com.demo.bankdemo

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.dynatrace.android.agent.Dynatrace
import com.dynatrace.android.agent.conf.DataCollectionLevel
import com.dynatrace.android.agent.conf.DynatraceConfigurationBuilder
import com.dynatrace.android.agent.conf.UserPrivacyOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var user by remember { mutableStateOf<BankUser?>(null) }
                Surface(Modifier.fillMaxSize().safeDrawingPadding()) {
                    val current = user
                    if (current == null) LoginScreen { Monitoring.login(this, it); user = it }
                    else HomeScreen(current) { Monitoring.logout(); user = null }
                }
            }
        }
    }
}

/**
 * v2: only VIP users are monitored, to save license cost. A Regular user never starts
 * the Dynatrace agent, so the agent sends nothing and no session is created.
 */
private object Monitoring {
    private var started = false

    fun login(activity: Activity, user: BankUser) {
        if (user.category != Category.VIP) return
        if (!started) {
            Dynatrace.startup(
                activity,
                DynatraceConfigurationBuilder(
                    "0723d7db-6587-4356-ba64-a3082d0452db",
                    "https://bf97052cfu.bf.dynatrace.com/mbeacon",
                ).withUserOptIn(true).withStartupLoadBalancing(true).buildConfiguration(),
            )
            started = true
        }
        // Demo only: a real app must ask the user before opting in.
        // The choice is saved, so only the first VIP login on a device starts at OFF.
        Dynatrace.applyUserPrivacyOptions(
            UserPrivacyOptions.builder()
                .withDataCollectionLevel(DataCollectionLevel.USER_BEHAVIOR)
                .withCrashReportingOptedIn(true)
                .withScreenRecordOptedIn(true)
                .build()
        )
        Dynatrace.identifyUser(user.username)
        // Classic RUM: action value. Define "category" as a session property in the app settings.
        val action = Dynatrace.enterAction("Login")
        action.reportValue("category", user.category.label)
        action.leaveAction()
    }

    fun logout() {
        if (!started) return
        // Stop the agent: this ends the session, and a Regular user who logs in next sends nothing.
        // Don't switch to OFF instead: an OFF agent still sends a "Loading" event that shows up as a session.
        Dynatrace.shutdown()
        started = false
    }
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
    var page by remember { mutableStateOf<BankPage?>(null) }
    page?.let { PageScreen(it, user) { page = null }; return }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
        ServicesSection { page = it }
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
