import SwiftUI
import VisionKit

struct DocumentScannerView: UIViewControllerRepresentable {
    @ObservedObject var viewModel: InvoiceViewModel
    @Environment(\.dismiss) var dismiss

    func makeUIViewController(context: Context) -> VNDocumentCameraViewController {
        let scanner = VNDocumentCameraViewController()
        scanner.delegate = context.coordinator
        return scanner
    }

    func updateUIViewController(_ uiViewController: VNDocumentCameraViewController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, VNDocumentCameraViewControllerDelegate {
        let parent: DocumentScannerView

        init(_ parent: DocumentScannerView) {
            self.parent = parent
        }

        func documentCameraViewController(_ controller: VNDocumentCameraViewController, didFinishWith scan: VNDocumentCameraScan) {
            guard scan.pageCount > 0 else {
                parent.dismiss()
                return
            }

            // Get the first scanned page
            let image = scan.imageOfPage(at: 0)
            guard let imageData = image.jpegData(compressionQuality: 0.8) else {
                parent.dismiss()
                return
            }

            // Upload the scanned invoice
            Task {
                await parent.viewModel.uploadInvoice(imageData: imageData)
                await MainActor.run {
                    parent.dismiss()
                }
            }
        }

        func documentCameraViewControllerDidCancel(_ controller: VNDocumentCameraViewController) {
            parent.dismiss()
        }

        func documentCameraViewController(_ controller: VNDocumentCameraViewController, didFailWithError error: Error) {
            print("Document scanner failed: \(error.localizedDescription)")
            parent.dismiss()
        }
    }
}
