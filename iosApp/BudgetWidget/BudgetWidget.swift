import SwiftUI
import WidgetKit

private let appGroup = "group.com.shoptourr"
private let jsonKey = "budget_widget_json"

struct BudgetSnapshot: Codable {
    var tripId: String?
    var city: String
    var remainingLine: String
    var overBudget: Bool
}

struct BudgetEntry: TimelineEntry {
    let date: Date
    let snapshot: BudgetSnapshot
}

struct BudgetProvider: TimelineProvider {
    func placeholder(in context: Context) -> BudgetEntry {
        BudgetEntry(date: Date(), snapshot: BudgetSnapshot(city: "ShopTourr", remainingLine: "", overBudget: false))
    }

    func getSnapshot(in context: Context, completion: @escaping (BudgetEntry) -> Void) {
        completion(BudgetEntry(date: Date(), snapshot: load() ?? placeholder(in: context).snapshot))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<BudgetEntry>) -> Void) {
        let snapshot = load() ?? BudgetSnapshot(city: "ShopTourr", remainingLine: "", overBudget: false)
        let next = Date().addingTimeInterval(30 * 60)
        completion(Timeline(entries: [BudgetEntry(date: Date(), snapshot: snapshot)], policy: .after(next)))
    }

    private func load() -> BudgetSnapshot? {
        guard let raw = UserDefaults(suiteName: appGroup)?.string(forKey: jsonKey),
              let data = raw.data(using: .utf8) else {
            return nil
        }
        return try? JSONDecoder().decode(BudgetSnapshot.self, from: data)
    }
}

struct BudgetWidgetView: View {
    var entry: BudgetEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(entry.snapshot.city.uppercased())
                .font(.system(size: 11, weight: .medium))
                .foregroundStyle(Color(red: 0.54, green: 0.51, blue: 0.47))
            if !entry.snapshot.remainingLine.isEmpty {
                Text(entry.snapshot.remainingLine)
                    .font(.system(size: 20, weight: .medium))
                    .foregroundStyle(
                        entry.snapshot.overBudget
                            ? Color(red: 0.61, green: 0.23, blue: 0.16)
                            : Color(red: 0.11, green: 0.10, blue: 0.09)
                    )
                    .lineLimit(2)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
        .containerBackground(Color(red: 0.98, green: 0.98, blue: 0.96), for: .widget)
        .widgetURL(deepLink)
    }

    private var deepLink: URL {
        if let id = entry.snapshot.tripId, !id.isEmpty {
            return URL(string: "voyage://trips/\(id)")!
        }
        return URL(string: "voyage://home")!
    }
}

struct BudgetWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "BudgetWidget", provider: BudgetProvider()) { entry in
            BudgetWidgetView(entry: entry)
        }
        .configurationDisplayName("Budget")
        .description("Remaining budget on the current trip")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

@main
struct BudgetWidgetBundle: WidgetBundle {
    var body: some Widget {
        BudgetWidget()
    }
}
