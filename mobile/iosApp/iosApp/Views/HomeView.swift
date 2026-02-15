import SwiftUI

/// Home screen placeholder.
/// Will display personalized playlists, quick picks, and recommendations
/// from the Innertube home feed.
struct HomeView: View {

    let onShowAuth: () -> Void
    let onShowPlayer: () -> Void

    var body: some View {
        VStack {
            Spacer()

            VStack(spacing: 8) {
                Text("Home Feed")
                    .font(.title)
                    .fontWeight(.bold)

                Text("Playlists and recommendations will appear here")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }

            Spacer()
        }
        .navigationTitle("YT Music")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                HStack {
                    Button(action: { /* TODO: Search */ }) {
                        Image(systemName: "magnifyingglass")
                    }
                    Button(action: onShowAuth) {
                        Image(systemName: "person.circle")
                    }
                }
            }
        }
    }
}

#Preview {
    NavigationStack {
        HomeView(onShowAuth: {}, onShowPlayer: {})
    }
}
