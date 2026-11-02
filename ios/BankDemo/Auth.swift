import Foundation

enum Category: String {
    case regular = "Regular"
    case vip = "VIP"
}

struct BankUser {
    let username: String
    let category: Category
}

/// Demo login rule: "userN"/"userN" is Regular, "vipN"/"vipN" is VIP (N = integer).
/// Password must equal the username. Anything else fails.
func authenticate(username: String, password: String) -> BankUser? {
    let name = username.trimmingCharacters(in: .whitespaces)
    guard !name.isEmpty, password == name else { return nil }
    for (prefix, category) in [("user", Category.regular), ("vip", Category.vip)] {
        let digits = name.dropFirst(prefix.count)
        if name.hasPrefix(prefix), !digits.isEmpty, digits.allSatisfy(\.isASCII), digits.allSatisfy(\.isNumber) {
            return BankUser(username: name, category: category)
        }
    }
    return nil
}
