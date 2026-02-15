import SwiftUI
import shared

@main
struct iOSApp: App {

    init() {
        // Initialize Koin DI
        KoinInitializerKt.doInitKoin()
        // Set up audio session for background playback
        AudioSessionManager.shared.configure()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
