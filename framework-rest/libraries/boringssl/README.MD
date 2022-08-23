## Build Boring SSL
参考 https://boringssl.googlesource.com/boringssl/+/2490/BUILDING

```
brew install go ninja
```

### Android

    mkdir build-android
    cd build-android
    cmake -DANDROID_ABI=armeabi-v7a \
          -DCMAKE_BUILD_TYPE=Release \
          -DCMAKE_TOOLCHAIN_FILE=../util/android-cmake/android.toolchain.cmake \
          -DANDROID_NATIVE_API_LEVEL=15 \
          -GNinja ..
    cmake --build .

### Desktop build for debugging (OS X)

    mkdir build
    cd build
    cmake -GNinja ..
    ninja

find `build-android.sh`, `build-desk.sh` for details.
