import AVFoundation
import MediaPlayer

/// Manages the AVAudioSession configuration for background audio playback.
/// Sets up the audio category and activates the session at app launch.
final class AudioSessionManager {

    static let shared = AudioSessionManager()

    private init() {}

    /// Configure audio session for background playback.
    /// Must be called early in app lifecycle (e.g., App.init or AppDelegate).
    func configure() {
        let session = AVAudioSession.sharedInstance()
        do {
            try session.setCategory(
                .playback,
                mode: .default,
                options: []
            )
            try session.setActive(true)
            print("[AudioSession] Configured for playback")
        } catch {
            print("[AudioSession] Failed to configure: \(error.localizedDescription)")
        }

        setupRemoteCommands()
    }

    /// Set up MPRemoteCommandCenter for lock screen / Control Center controls.
    private func setupRemoteCommands() {
        let commandCenter = MPRemoteCommandCenter.shared()

        commandCenter.playCommand.isEnabled = true
        commandCenter.playCommand.addTarget { _ in
            // TODO: Resume playback via PlayerBridge
            return .success
        }

        commandCenter.pauseCommand.isEnabled = true
        commandCenter.pauseCommand.addTarget { _ in
            // TODO: Pause playback via PlayerBridge
            return .success
        }

        commandCenter.nextTrackCommand.isEnabled = true
        commandCenter.nextTrackCommand.addTarget { _ in
            // TODO: Next track via PlayerBridge
            return .success
        }

        commandCenter.previousTrackCommand.isEnabled = true
        commandCenter.previousTrackCommand.addTarget { _ in
            // TODO: Previous track via PlayerBridge
            return .success
        }

        commandCenter.changePlaybackPositionCommand.isEnabled = true
        commandCenter.changePlaybackPositionCommand.addTarget { event in
            guard let positionEvent = event as? MPChangePlaybackPositionCommandEvent else {
                return .commandFailed
            }
            // TODO: Seek via PlayerBridge
            _ = positionEvent.positionTime
            return .success
        }
    }

    /// Update Now Playing info on lock screen.
    func updateNowPlaying(
        title: String,
        artist: String,
        duration: TimeInterval,
        currentTime: TimeInterval,
        isPlaying: Bool
    ) {
        var info = [String: Any]()
        info[MPMediaItemPropertyTitle] = title
        info[MPMediaItemPropertyArtist] = artist
        info[MPMediaItemPropertyPlaybackDuration] = duration
        info[MPNowPlayingInfoPropertyElapsedPlaybackTime] = currentTime
        info[MPNowPlayingInfoPropertyPlaybackRate] = isPlaying ? 1.0 : 0.0
        MPNowPlayingInfoCenter.default().nowPlayingInfo = info
    }
}
