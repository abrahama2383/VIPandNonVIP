import SwiftUI

/// Five static demo pages reachable from Home. Buttons only show a message; nothing is sent.
enum BankPage: String, CaseIterable, Identifiable {
    case transfer = "Transfer"
    case bills = "Pay Bills"
    case topUp = "Top Up"
    case history = "Transaction History"
    case profile = "Profile"

    var id: String { rawValue }

    var icon: String {
        switch self {
        case .transfer: "arrow.left.arrow.right"
        case .bills: "doc.text"
        case .topUp: "iphone"
        case .history: "clock"
        case .profile: "person.crop.circle"
        }
    }

    var action: String? {
        switch self {
        case .transfer: "Send Rp 500.000"
        case .bills: "Pay Electricity Rp 350.000"
        case .topUp: "Top Up Rp 100.000"
        case .history, .profile: nil
        }
    }

    func rows(for user: BankUser) -> [(String, String)] {
        switch self {
        case .transfer:
            [("From", "Savings 1234 5678 9012"), ("To", "Budi Santoso"), ("Account", "9876 5432 1098"),
             ("Amount", "Rp 500.000"), ("Fee", "Rp 0")]
        case .bills:
            [("Electricity (PLN)", "Rp 350.000"), ("Water (PDAM)", "Rp 120.000"),
             ("Internet", "Rp 450.000"), ("Due date", "10 Nov 2026")]
        case .topUp:
            [("E-wallet", "GoPay"), ("Phone", "0812 3456 7890"), ("Amount", "Rp 100.000"),
             ("Balance after", "Rp 24.900.000")]
        case .history:
            [("01 Nov - Transfer to Budi", "- Rp 500.000"), ("31 Oct - Salary", "+ Rp 10.000.000"),
             ("30 Oct - Electricity bill", "- Rp 350.000"), ("28 Oct - Top up GoPay", "- Rp 100.000"),
             ("25 Oct - Internet bill", "- Rp 450.000"), ("20 Oct - Transfer from Sari", "+ Rp 750.000")]
        case .profile:
            [("Username", user.username), ("Category", user.category.rawValue),
             ("Email", "\(user.username)@demobank.id"), ("Phone", "0812 3456 7890"), ("Member since", "2021")]
        }
    }
}

struct PageView: View {
    let page: BankPage
    let user: BankUser
    @State private var done = false

    var body: some View {
        List {
            Section {
                ForEach(Array(page.rows(for: user).enumerated()), id: \.offset) { _, row in
                    LabeledContent(row.0, value: row.1)
                }
            }
            if let action = page.action {
                Section {
                    Button(action) { done = true }
                        .accessibilityIdentifier("pageAction")
                    if done {
                        Text("Success (demo only, nothing was sent)").foregroundStyle(.green)
                    }
                }
            }
        }
        .navigationTitle(page.rawValue)
    }
}
