import ProjectDescription

// MARK: - Shared KMP Framework

let sharedFrameworkPath = "../shared/build/xcode-frameworks"

let sharedFrameworkScript = TargetScript.pre(
    script: """
    if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
        echo "Skipping Gradle build — IDE mode"
        exit 0
    fi
    cd "$SRCROOT/.."
    export JAVA_HOME="/Users/nik/Applications/Android Studio.app/Contents/jbr/Contents/Home"
    ./gradlew :shared:embedAndSignAppleFrameworkForXcode
    """,
    name: "Compile Shared KMP Framework",
    basedOnDependencyAnalysis: false
)

// MARK: - Project

let project = Project(
    name: "iosApp",
    options: .options(
        defaultKnownRegions: ["en"],
        developmentRegion: "en"
    ),
    settings: .settings(
        base: [
            "DEVELOPMENT_TEAM": "",
        ],
        configurations: [
            .debug(name: "Debug"),
            .release(name: "Release"),
        ]
    ),
    targets: [
        .target(
            name: "iosApp",
            destinations: .iOS,
            product: .app,
            bundleId: "com.ytmd.mobile.ios",
            deploymentTargets: .iOS("16.0"),
            infoPlist: .file(path: "iosApp/Info.plist"),
            sources: ["iosApp/**/*.swift"],
            scripts: [sharedFrameworkScript],
            dependencies: [],
            settings: .settings(
                base: [
                    "PRODUCT_NAME": "YT Music",
                    "MARKETING_VERSION": "0.1.0",
                    "CURRENT_PROJECT_VERSION": "1",
                    "ENABLE_USER_SCRIPT_SANDBOXING": "NO",
                    "FRAMEWORK_SEARCH_PATHS": [
                        "$(inherited)",
                        "$(SRCROOT)/../shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)",
                        "$(SRCROOT)/../shared/build/bin/iosSimulatorArm64/debugFramework",
                        "$(SRCROOT)/../shared/build/bin/iosArm64/releaseFramework",
                    ],
                    "OTHER_LDFLAGS": "$(inherited) -lsqlite3 -framework shared",
                ]
            )
        ),
    ]
)
