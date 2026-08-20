import AuthenticationServices
import Shared
import UIKit

final class IosSocialAuthCoordinator: NSObject, ASAuthorizationControllerDelegate, ASAuthorizationControllerPresentationContextProviding, ASWebAuthenticationPresentationContextProviding {
    static let shared = IosSocialAuthCoordinator()

    private var appleCallback: ((String?, String?, String?) -> Void)?
    private var browserCallback: ((String?, String?) -> Void)?
    private var webSession: ASWebAuthenticationSession?

    func configure() {
        IosSocialAuthClientKt.registerIosSocialAuth(
            apple: IosAppleSignIn { nonce, callback in
                IosSocialAuthCoordinator.shared.startApple(nonce: nonce, callback: callback)
            },
            browser: IosBrowserAuth { url, scheme, callback in
                IosSocialAuthCoordinator.shared.startBrowser(url: url, scheme: scheme, callback: callback)
            }
        )
    }

    func startApple(nonce: String, callback: @escaping (String?, String?, String?) -> Void) {
        appleCallback = callback
        let request = ASAuthorizationAppleIDProvider().createRequest()
        request.requestedScopes = [.fullName, .email]
        request.nonce = nonce
        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate = self
        controller.presentationContextProvider = self
        controller.performRequests()
    }

    func startBrowser(url: String, scheme: String, callback: @escaping (String?, String?) -> Void) {
        guard let nsUrl = URL(string: url) else {
            callback(nil, "Invalid authorization URL")
            return
        }
        browserCallback = callback
        let session = ASWebAuthenticationSession(url: nsUrl, callbackURLScheme: scheme) { callbackURL, error in
            if let error = error as? ASWebAuthenticationSessionError, error.code == .canceledLogin {
                callback(nil, "cancelled")
                return
            }
            if let error {
                callback(nil, error.localizedDescription)
                return
            }
            callback(callbackURL?.absoluteString, nil)
        }
        session.presentationContextProvider = self
        session.prefersEphemeralWebBrowserSession = true
        webSession = session
        session.start()
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        guard let credential = authorization.credential as? ASAuthorizationAppleIDCredential,
              let tokenData = credential.identityToken,
              let idToken = String(data: tokenData, encoding: .utf8) else {
            appleCallback?(nil, nil, "Apple did not return an identity token.")
            appleCallback = nil
            return
        }
        let fullName = [credential.fullName?.givenName, credential.fullName?.familyName]
            .compactMap { $0 }
            .joined(separator: " ")
            .trimmingCharacters(in: .whitespaces)
        appleCallback?(idToken, fullName.isEmpty ? nil : fullName, nil)
        appleCallback = nil
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        if let authError = error as? ASAuthorizationError, authError.code == .canceled {
            appleCallback?(nil, nil, "cancelled")
        } else {
            appleCallback?(nil, nil, error.localizedDescription)
        }
        appleCallback = nil
    }

    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        keyWindow()
    }

    func presentationAnchor(for session: ASWebAuthenticationSession) -> ASPresentationAnchor {
        keyWindow()
    }

    private func keyWindow() -> ASPresentationAnchor {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
            .first { $0.isKeyWindow } ?? UIWindow()
    }
}
