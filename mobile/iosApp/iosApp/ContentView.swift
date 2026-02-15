import SwiftUI

struct ContentView: View {

    @State private var selectedTab = 0
    @State private var showPlayer = false
    @State private var showAuth = false

    var body: some View {
        ZStack(alignment: .bottom) {
            NavigationStack {
                HomeView(
                    onShowAuth: { showAuth = true },
                    onShowPlayer: { showPlayer = true }
                )
            }

            // Mini player overlay at the bottom
            MiniPlayerView(onExpand: { showPlayer = true })
        }
        .sheet(isPresented: $showPlayer) {
            PlayerView()
        }
        .sheet(isPresented: $showAuth) {
            AuthView(onComplete: { showAuth = false })
        }
    }
}

/// Mini player bar at the bottom of the screen
struct MiniPlayerView: View {

    let onExpand: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            // Progress bar
            GeometryReader { geometry in
                Rectangle()
                    .fill(Color.red)
                    .frame(width: geometry.size.width * 0.3, height: 2)
            }
            .frame(height: 2)

            HStack(spacing: 12) {
                // Thumbnail placeholder
                RoundedRectangle(cornerRadius: 4)
                    .fill(Color.gray.opacity(0.3))
                    .frame(width: 48, height: 48)

                VStack(alignment: .leading, spacing: 2) {
                    Text("No track playing")
                        .font(.subheadline)
                        .lineLimit(1)
                    Text("---")
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }

                Spacer()

                Button(action: { /* TODO: Play/Pause */ }) {
                    Image(systemName: "play.fill")
                        .font(.title2)
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(.ultraThinMaterial)
        }
        .onTapGesture(perform: onExpand)
    }
}

#Preview {
    ContentView()
}
