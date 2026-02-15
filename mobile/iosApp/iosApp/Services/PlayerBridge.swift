import AVFoundation
import shared

/// Bridges the shared Kotlin PlayerController to AVPlayer for iOS.
/// Manages AVPlayer lifecycle, observes playback status, and updates
/// the shared PlayerController state.
final class PlayerBridge: ObservableObject {

    private var player: AVPlayer?
    private var playerController: PlayerController?
    private var timeObserver: Any?

    init() {
        // PlayerController is provided by Koin from the shared module
        // In full implementation, resolve from Koin
    }

    /// Start playing a stream URL.
    func play(url: URL, trackId: String) {
        stop()

        let playerItem = AVPlayerItem(url: url)
        player = AVPlayer(playerItem: playerItem)
        player?.play()

        setupTimeObserver()
        setupNotifications(for: playerItem)
    }

    /// Pause playback.
    func pause() {
        player?.pause()
        playerController?.updatePlaybackState(isPlaying: false)
    }

    /// Resume playback.
    func resume() {
        player?.play()
        playerController?.updatePlaybackState(isPlaying: true)
    }

    /// Seek to position in milliseconds.
    func seek(to positionMs: Int64) {
        let time = CMTime(value: positionMs, timescale: 1000)
        player?.seek(to: time)
    }

    /// Stop and clean up.
    func stop() {
        if let observer = timeObserver {
            player?.removeTimeObserver(observer)
            timeObserver = nil
        }
        player?.pause()
        player = nil
    }

    // MARK: - Private

    private func setupTimeObserver() {
        let interval = CMTime(seconds: 0.5, preferredTimescale: 600)
        timeObserver = player?.addPeriodicTimeObserver(
            forInterval: interval,
            queue: .main
        ) { [weak self] time in
            guard let self = self,
                  let duration = self.player?.currentItem?.duration else { return }

            let positionMs = Int64(time.seconds * 1000)
            let durationMs = Int64(duration.seconds * 1000)

            self.playerController?.updateProgress(
                positionMs: positionMs,
                durationMs: durationMs
            )

            // Update Now Playing info
            AudioSessionManager.shared.updateNowPlaying(
                title: "Current Track", // TODO: Get from PlayerController state
                artist: "Artist",
                duration: duration.seconds,
                currentTime: time.seconds,
                isPlaying: self.player?.rate ?? 0 > 0
            )
        }
    }

    private func setupNotifications(for item: AVPlayerItem) {
        NotificationCenter.default.addObserver(
            forName: .AVPlayerItemDidPlayToEndTime,
            object: item,
            queue: .main
        ) { [weak self] _ in
            self?.playerController?.next()
        }
    }

    deinit {
        stop()
        NotificationCenter.default.removeObserver(self)
    }
}
