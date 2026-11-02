import SwiftUI
import Dynatrace

@main
struct BankDemoApp: App {
    @State private var user: BankUser?

    init() {
        // Demo only: a real app must ask the user before opting in.
        let privacy = Dynatrace.userPrivacyOptions()
        privacy.dataCollectionLevel = .userBehavior
        privacy.crashReportingOptedIn = true
        Dynatrace.applyUserPrivacyOptions(privacy) { _ in }
    }

    var body: some Scene {
        WindowGroup {
            if let user {
                HomeView(user: user) { Dynatrace.endVisit(); self.user = nil }
            } else {
                LoginView { tagSession($0); self.user = $0 }
            }
        }
    }
}

/// Tags the Dynatrace session with the username and the VIP/Regular category.
private func tagSession(_ user: BankUser) {
    Dynatrace.identifyUser(user.username)
    // Classic RUM: action value. Define "category" as a session property in the app settings.
    let action = DTXAction.enter(withName: "Login")
    action?.reportValue(withName: "category", stringValue: user.category.rawValue)
    action?.leave()
}

struct LoginView: View {
    let onLogin: (BankUser) -> Void
    @State private var username = ""
    @State private var password = ""
    @State private var error: String?

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "building.columns.fill")
                .font(.system(size: 56))
                .foregroundStyle(.blue)
            Text("Demo Bank").font(.largeTitle.bold())
            TextField("Username", text: $username)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
                .textFieldStyle(.roundedBorder)
                .accessibilityIdentifier("username")
            SecureField("Password", text: $password)
                .textFieldStyle(.roundedBorder)
                .accessibilityIdentifier("password")
            if let error {
                Text(error).foregroundStyle(.red).font(.footnote)
            }
            Button {
                if let user = authenticate(username: username, password: password) {
                    onLogin(user)
                } else {
                    error = "Invalid username or password"
                }
            } label: {
                Text("Login").frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            .accessibilityIdentifier("login")
        }
        .padding(32)
    }
}

struct HomeView: View {
    let user: BankUser
    let onLogout: () -> Void

    private var isVIP: Bool { user.category == .vip }
    private var accent: Color { isVIP ? Color(red: 0.75, green: 0.6, blue: 0.2) : .blue }

    var body: some View {
        NavigationStack {
            List {
                Section {
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Welcome, \(user.username)").font(.title2.bold())
                        Text(user.category.rawValue)
                            .font(.caption.bold())
                            .padding(.horizontal, 10).padding(.vertical, 4)
                            .background(accent.opacity(0.2), in: Capsule())
                            .foregroundStyle(accent)
                            .accessibilityIdentifier("category")
                    }
                    .padding(.vertical, 4)
                }
                Section("Savings Account") {
                    LabeledContent("Account No.", value: "1234 5678 9012")
                    LabeledContent("Balance", value: "Rp 25.000.000")
                }
                Section("Recent Transactions") {
                    LabeledContent("Transfer to Budi", value: "- Rp 500.000")
                    LabeledContent("Salary", value: "+ Rp 10.000.000")
                    LabeledContent("Electricity bill", value: "- Rp 350.000")
                }
            }
            .navigationTitle("Home")
            .toolbar {
                Button("Logout", action: onLogout)
            }
        }
    }
}
