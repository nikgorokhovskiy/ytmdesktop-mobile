import SwiftUI
import WebKit

/// Authentication screen with WKWebView for Google login.
/// After successful login, cookies are extracted and saved.
struct AuthView: View {

    let onComplete: () -> Void

    var body: some View {
        NavigationStack {
            AuthWebView(onCookiesExtracted: { cookies in
                // TODO: Save cookies to CookieStore via shared module
                print("[Auth] Extracted \(cookies.count) cookies")
                onComplete()
            })
            .navigationTitle("Sign In")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        onComplete()
                    }
                }
            }
        }
    }
}

/// WKWebView wrapper for Google authentication.
struct AuthWebView: UIViewRepresentable {

    let onCookiesExtracted: ([String: String]) -> Void

    func makeCoordinator() -> Coordinator {
        Coordinator(onCookiesExtracted: onCookiesExtracted)
    }

    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        config.websiteDataStore = .default()
        let webView = WKWebView(frame: .zero, configuration: config)
        webView.navigationDelegate = context.coordinator

        if let url = URL(string: "https://music.youtube.com") {
            webView.load(URLRequest(url: url))
        }
        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {}

    class Coordinator: NSObject, WKNavigationDelegate {
        let onCookiesExtracted: ([String: String]) -> Void

        init(onCookiesExtracted: @escaping ([String: String]) -> Void) {
            self.onCookiesExtracted = onCookiesExtracted
        }

        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            guard let url = webView.url,
                  url.host?.contains("music.youtube.com") == true else { return }

            // Extract cookies from WKWebsiteDataStore
            let store = webView.configuration.websiteDataStore.httpCookieStore
            store.getAllCookies { [weak self] cookies in
                var cookieMap: [String: String] = [:]
                for cookie in cookies {
                    if cookie.domain.contains("youtube.com") || cookie.domain.contains("google.com") {
                        cookieMap[cookie.name] = cookie.value
                    }
                }

                // Check for essential auth cookies
                if cookieMap["SAPISID"] != nil && cookieMap["SID"] != nil {
                    DispatchQueue.main.async {
                        self?.onCookiesExtracted(cookieMap)
                    }
                }
            }
        }
    }
}

#Preview {
    AuthView(onComplete: {})
}
