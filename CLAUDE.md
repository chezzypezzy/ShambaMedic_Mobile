# ShambaMedic

Android crop disease detection app for Kenyan smallholder farmers.

## Stack
- Kotlin, Jetpack Compose, Material3
- Hilt for DI, Room for local DB, Retrofit for API
- TensorFlow Lite for on-device ML inference
- CameraX for camera, DataStore for preferences
- MVVM + Repository + Clean Architecture

## Package structure
com.example.shambamedic
- data (local, remote, repository)
- domain (model, usecase)
- presentation (auth, home, camera, results, history, navigation)
- ml (InferenceEngine, ModelLabels)
- di (AppModule, DatabaseModule, NetworkModule, RepositoryModule)
- util (Constants, NetworkMonitor, SyncManager)

## Key facts
- Min SDK API 28, Target SDK 37
- TFLite model at app/src/main/assets/shambamedic_model.tflite
- Model input: 200x200, normalized to [-1, 1]
- 39 output classes (PlantVillage subset)
- Backend URL: https://shambamedic-api.onrender.com/
- Room DB name: shambamedic_db

## Build
./gradlew assembleDebug
./gradlew installDebug
