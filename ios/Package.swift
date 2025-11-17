// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "InvoiceApp",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(
            name: "InvoiceApp",
            targets: ["InvoiceApp"])
    ],
    dependencies: [
        // Add your dependencies here
    ],
    targets: [
        .target(
            name: "InvoiceApp",
            dependencies: []),
        .testTarget(
            name: "InvoiceAppTests",
            dependencies: ["InvoiceApp"])
    ]
)
