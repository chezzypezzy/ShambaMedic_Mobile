# Camera and ML Integration Implementation Plan - ShambaMedic

I will implement the `CameraScreen` feature, which uses CameraX to capture plant leaf images and the `InferenceEngine` to classify diseases in real-time.

## Proposed Changes

### Configuration & Permissions

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/Peter/AndroidStudioProjects/ShambaMedic/app/src/main/AndroidManifest.xml)
- Add `CAMERA`, `INTERNET`, and `ACCESS_NETWORK_STATE` permissions.
- Declare the `android.hardware.camera` feature as required.

---

### Presentation Layer (`com.example.shambamedic.presentation.camera`)

#### [MODIFY] [CameraViewModel.kt](file:///C:/Users/Peter/AndroidStudioProjects/ShambaMedic/app/src/main/java/com/example/shambamedic/presentation/camera/CameraViewModel.kt)
- Implement `CameraUiState` to track capture status, classification results, and loading states.
- Implement `onImageCaptured(bitmap: Bitmap)`:
    - Triggers `ClassifyDiseaseUseCase`.
    - Automatically saves the scan result using `SaveScanUseCase`.
- Implement `saveScan(result: ClassificationResult)` to persist the scan in the local database.
- Implement `retake()` logic to reset the UI for another scan.

#### [MODIFY] [CameraScreen.kt](file:///C:/Users/Peter/AndroidStudioProjects/ShambaMedic/app/src/main/java/com/example/shambamedic/presentation/camera/CameraScreen.kt)
- Integrate **Accompanist Permissions** for a robust camera permission flow.
- Implement `CameraPreview` using `AndroidView` and **CameraX**.
- **Live Camera View**:
    - Includes a back button and crop type badge.
    - Features a **leaf framing guide** (dashed border) to assist the user.
    - Shutter button for image capture.
- **Review Screen**:
    - Displays the captured image.
    - Shows a result card with the disease name, confidence bar, and severity indicator.
    - "Retake" and "View Results" actions.

---

### Navigation Layer (`com.example.shambamedic.presentation.navigation`)

#### [MODIFY] [NavGraph.kt](file:///C:/Users/Peter/AndroidStudioProjects/ShambaMedic/app/src/main/java/com/example/shambamedic/presentation/navigation/NavGraph.kt)
- Replace the Camera placeholder with the actual `CameraScreen` composable.

## Verification Plan

### Automated Verification
- I will run `./gradlew assembleDebug` to ensure that all CameraX dependencies are correctly linked and that Hilt can provide the necessary use cases to the `CameraViewModel`.

### Manual Verification
- I will verify the permission request flow.
- I will check the camera preview binding to ensure it lifecycle-aware.
- I will verify that the review screen correctly displays results from the inference engine.
- I will ensure all user-facing strings are marked for Swahili translation.
