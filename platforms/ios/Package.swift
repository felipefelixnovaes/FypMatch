// swift-tools-version: 5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "FypMatch-iOS",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        .library(
            name: "FypMatch-iOS",
            targets: ["FypMatch-iOS"]
        )
    ],
    dependencies: [
        // Firebase iOS SDK
        .package(url: "https://github.com/firebase/firebase-ios-sdk", from: "10.20.0"),
        
        // SwiftUI Navigation
        .package(url: "https://github.com/pointfreeco/swift-composable-architecture", from: "1.7.0"),
        
        // Async Image Loading
        .package(url: "https://github.com/onevcat/Kingfisher", from: "7.10.0"),
        
        // Network Layer
        .package(url: "https://github.com/Alamofire/Alamofire", from: "5.8.0")
    ],
    targets: [
        .target(
            name: "FypMatch-iOS",
            dependencies: [
                .product(name: "FirebaseAuth", package: "firebase-ios-sdk"),
                .product(name: "FirebaseFirestore", package: "firebase-ios-sdk"),
                .product(name: "FirebaseStorage", package: "firebase-ios-sdk"),
                .product(name: "FirebaseMessaging", package: "firebase-ios-sdk"),
                .product(name: "ComposableArchitecture", package: "swift-composable-architecture"),
                .product(name: "Kingfisher", package: "Kingfisher"),
                .product(name: "Alamofire", package: "Alamofire")
            ]
        ),
        .testTarget(
            name: "FypMatch-iOSTests",
            dependencies: ["FypMatch-iOS"]
        )
    ]
)