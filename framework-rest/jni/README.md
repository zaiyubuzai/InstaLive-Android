# 构建JNI模块
安装NDK组件，参考[文档](https://developer.android.com/ndk/guides/)

## [Google Boring SSL](https://github.com/google/boringssl)

Hash算法使用了[Boring SSL](https://github.com/google/boringssl)的`crypto`模块，方便起见，目前采取直接引用预先编译的静态库的方式

### 代码来源
代码取自[chromium-stable分支](https://github.com/google/boringssl/tree/chromium-stable) [commit 907ae62](https://github.com/google/boringssl/commit/907ae62b9d81121cb86b604f83e6b811a43f7a87)

Forked repo: [3rdStone/boringssl](https://github.com/xinthink/boringssl) [fm-chromium-stable分支](https://github.com/xinthink/boringssl/commits/fm-chromium-stable)

### 预先构建BoringSSL静态库
参考[building-for-android](https://github.com/google/boringssl/blob/master/BUILDING.md#building-for-android)

修改了`CMakeLists.txt`文件，Android平台只build`crypto`及相关模块，以便减少lib的size，变更见commit

详见[README.md文档](../libraries/boringssl/README.MD)

    cd boringssl
    build-android.sh
    # or build-desk.sh

## 构建动态库
直接随工程编译打包即可
