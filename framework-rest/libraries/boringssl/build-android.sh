mkdir lib build-android
cd build-android

BUILD_TYPE=Release
NATIVE_API_LEVEL=21

build() {
  arch="$1"
  toolchain="$2"
  echo "building for ${arch} ..."
  rm CMakeCache.txt
  rm -r CMakeFiles
  rm -r crypto

  cmake -DANDROID_ABI="${arch}" \
        -DCMAKE_BUILD_TYPE=${BUILD_TYPE} \
        -DCMAKE_TOOLCHAIN_FILE=../util/android-cmake/android.toolchain.cmake \
        -DANDROID_NATIVE_API_LEVEL=${NATIVE_API_LEVEL} \
        -DANDROID_TOOLCHAIN_NAME="${toolchain}" \
        -GNinja ..
  cmake -DANDROID_ABI="${arch}" \
        -DCMAKE_BUILD_TYPE=${BUILD_TYPE} \
        -DCMAKE_TOOLCHAIN_FILE=../util/android-cmake/android.toolchain.cmake \
        -DANDROID_NATIVE_API_LEVEL=${NATIVE_API_LEVEL} \
        -DANDROID_TOOLCHAIN_NAME="${toolchain}" \
        -GNinja ..
  cmake --build .
  mv crypto/libcrypto.a ../lib/libcrypto_${arch}.a
}

build "x86" "x86-4.9"
build "x86_64" "x86_64-4.9"

for AR in "armeabi" "armeabi-v7a" "arm64-v8a";
do
  build "$AR"
done
