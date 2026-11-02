package com.demo.bankdemo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Five static demo pages reachable from Home. Buttons only show a message; nothing is sent. */
enum class BankPage(val title: String, val action: String?) {
    TRANSFER("Transfer", "Send Rp 500.000"),
    BILLS("Pay Bills", "Pay Electricity Rp 350.000"),
    TOP_UP("Top Up", "Top Up Rp 100.000"),
    HISTORY("Transaction History", null),
    PROFILE("Profile", null);

    fun rows(user: BankUser): List<Pair<String, String>> = when (this) {
        TRANSFER -> listOf(
            "From" to "Savings 1234 5678 9012", "To" to "Budi Santoso", "Account" to "9876 5432 1098",
            "Amount" to "Rp 500.000", "Fee" to "Rp 0",
        )
        BILLS -> listOf(
            "Electricity (PLN)" to "Rp 350.000", "Water (PDAM)" to "Rp 120.000",
            "Internet" to "Rp 450.000", "Due date" to "10 Nov 2026",
        )
        TOP_UP -> listOf(
            "E-wallet" to "GoPay", "Phone" to "0812 3456 7890", "Amount" to "Rp 100.000",
            "Balance after" to "Rp 24.900.000",
        )
        HISTORY -> listOf(
            "01 Nov - Transfer to Budi" to "- Rp 500.000", "31 Oct - Salary" to "+ Rp 10.000.000",
            "30 Oct - Electricity bill" to "- Rp 350.000", "28 Oct - Top up GoPay" to "- Rp 100.000",
            "25 Oct - Internet bill" to "- Rp 450.000", "20 Oct - Transfer from Sari" to "+ Rp 750.000",
        )
        PROFILE -> listOf(
            "Username" to user.username, "Category" to user.category.label,
            "Email" to "${user.username}@demobank.id", "Phone" to "0812 3456 7890", "Member since" to "2021",
        )
    }
}

/** The "Services" list on Home: one row per page. */
@Composable
fun ServicesSection(onOpen: (BankPage) -> Unit) {
    Text("Services", color = Color.Gray, fontWeight = FontWeight.SemiBold)
    Card(Modifier.fillMaxWidth()) {
        Column {
            BankPage.entries.forEach { page ->
                Row(
                    Modifier.fillMaxWidth().clickable { onOpen(page) }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(page.title, Modifier.weight(1f))
                    Text("›", color = Color.Gray, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
fun PageScreen(page: BankPage, user: BankUser, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var done by remember(page) { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onBack) { Text("‹ Back") }
        Text(page.title, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                page.rows(user).forEach { (k, v) ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(k, Modifier.weight(1f))
                        Text(v, color = Color.Gray)
                    }
                }
            }
        }
        page.action?.let { action ->
            Button(onClick = { done = true }, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text(action) }
            if (done) Text("Success (demo only, nothing was sent)", color = Color(0xFF2E7D32))
        }
    }
}
