package com.demo.bankdemo

enum class Category(val label: String) { REGULAR("Regular"), VIP("VIP") }

data class BankUser(val username: String, val category: Category)

private val rule = Regex("^(user|vip)([0-9]+)$")

/** Demo login rule: "userN"/"userN" is Regular, "vipN"/"vipN" is VIP (N = integer). Password must equal username. */
fun authenticate(username: String, password: String): BankUser? {
    val name = username.trim()
    if (password != name) return null
    val match = rule.matchEntire(name) ?: return null
    val category = if (match.groupValues[1] == "vip") Category.VIP else Category.REGULAR
    return BankUser(name, category)
}
