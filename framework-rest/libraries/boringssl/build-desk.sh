mkdir lib build
cd build
cmake -GNinja ..
ninja
mv crypto/libcrypto.a ../lib/libcrypto_desk.a
