import SwiftUI

/// Full-screen player view skeleton.
/// Displays album art, track info, progress bar, and playback controls.
struct PlayerView: View {

    @Environment(\.dismiss) private var dismiss

    @State private var progress: Double = 0.3 // placeholder
    @State private var isPlaying: Bool = false

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                Spacer().frame(height: 32)

                // Album art placeholder
                RoundedRectangle(cornerRadius: 12)
                    .fill(Color.gray.opacity(0.2))
                    .aspectRatio(1, contentMode: .fit)
                    .padding(.horizontal, 24)

                Spacer().frame(height: 32)

                // Track info
                VStack(alignment: .leading, spacing: 4) {
                    Text("No track selected")
                        .font(.title2)
                        .fontWeight(.bold)
                        .lineLimit(1)

                    Text("---")
                        .font(.body)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 24)

                Spacer().frame(height: 24)

                // Progress bar
                VStack(spacing: 4) {
                    ProgressView(value: progress)
                        .tint(.red)
                        .padding(.horizontal, 24)

                    HStack {
                        Text("0:00")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Spacer()
                        Text("0:00")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    .padding(.horizontal, 24)
                }

                Spacer().frame(height: 24)

                // Playback controls
                HStack(spacing: 0) {
                    Spacer()

                    Button(action: { /* TODO: Shuffle */ }) {
                        Image(systemName: "shuffle")
                            .font(.title3)
                            .foregroundColor(.secondary)
                    }

                    Spacer()

                    Button(action: { /* TODO: Previous */ }) {
                        Image(systemName: "backward.fill")
                            .font(.title)
                    }

                    Spacer()

                    Button(action: { isPlaying.toggle() }) {
                        Image(systemName: isPlaying ? "pause.circle.fill" : "play.circle.fill")
                            .font(.system(size: 64))
                    }

                    Spacer()

                    Button(action: { /* TODO: Next */ }) {
                        Image(systemName: "forward.fill")
                            .font(.title)
                    }

                    Spacer()

                    Button(action: { /* TODO: Repeat */ }) {
                        Image(systemName: "repeat")
                            .font(.title3)
                            .foregroundColor(.secondary)
                    }

                    Spacer()
                }

                Spacer()
            }
            .navigationTitle("Now Playing")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Close") {
                        dismiss()
                    }
                }
            }
        }
    }
}

#Preview {
    PlayerView()
}
