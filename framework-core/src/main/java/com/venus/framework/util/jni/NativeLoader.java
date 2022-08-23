package com.venus.framework.util.jni;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;

import com.venus.framework.util.L;
import com.venus.framework.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Native dynamic library loader
 * <p/>
 * Created by ywu on 16/5/1.
 */
public class NativeLoader {

    public static synchronized void loadLibrary(Context context, String libName) {
        try {
            // try normal so loading method
            System.loadLibrary(libName);
            return;
        } catch (Error e) {
            L.e("System.loadLibrary failed: " + libName, e);
        }

        // try to load it from apk
        loadLibraryFromApk(context, libName);
    }

    @SuppressLint("UnsafeDynamicallyLoadedCode")
    private static void loadLibraryFromApk(Context context, String libName) {
        final String libFolder = findLibFolderName();
        final String libFileName = System.mapLibraryName(libName);
        final File localLibsDir = prepareLocalLibsDir(context);

        // extract so from apk to local dir
        JarFile apk = null;
        InputStream stream = null;

        try {
            apk = new JarFile(context.getApplicationInfo().publicSourceDir);
            String path = "lib/" + libFolder + "/" + libFileName;
            ZipEntry soEntry = apk.getEntry(path);
            if (soEntry == null) {
                throw new UnsatisfiedLinkError("library not found in apk: " + path);
            }

            // remove old local so file
            File destFile = new File(localLibsDir, libFileName);
            if (!destFile.exists()) {
                stream = apk.getInputStream(soEntry);
                OutputStream out = new FileOutputStream(destFile);
                byte[] buf = new byte[4096];
                int len;
                while ((len = stream.read(buf)) > 0) {
                    Thread.yield();
                    out.write(buf, 0, len);
                }
                out.close();

                // 取消设置权限 FIX lint alert: SetWorldReadable
//                // 参考Telegram,设置文件权限
//                if (Build.VERSION.SDK_INT >= 9) {
//                    destFile.setReadable(true, false);
//                    destFile.setExecutable(true, false);
//                    destFile.setWritable(true);
//                }
            }

            System.load(destFile.getAbsolutePath());
        } catch (IOException e) {
            throw new UnsatisfiedLinkError("load library from apk failed: " + libFileName);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    L.e(e);
                }
            }
            if (apk != null) {
                try {
                    apk.close();
                } catch (Exception e) {
                    L.e(e);
                }
            }
        }
    }

    private static synchronized File prepareLocalLibsDir(Context context) {
        Context appCtx = context.getApplicationContext();
        File dir = new File(appCtx.getApplicationInfo().dataDir, "app_libs");
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new RuntimeException("could not create libs directory");
        }

        try {
            PackageInfo pkg = appCtx.getPackageManager().getPackageInfo(appCtx.getPackageName(), 0);
            int currAppVersion = pkg.versionCode;

            // load previous libs version from the dir
            File versionFlag = new File(dir, "v");
            int libsVersion;

            try {
                libsVersion = Integer.valueOf(Utils.readFileContent(versionFlag));
            } catch (Exception e) {
                L.d("load libs version failed, use 0");
                libsVersion = 0;
            }

            if (currAppVersion != libsVersion) {
                // clear the old libs
                for (File file : dir.listFiles()) {
                    if (!file.isDirectory()) {
                        file.delete();
                    }
                }

                // mark the current libs version
                Utils.writeFileContent(versionFlag, "" + currAppVersion);
            }

            return dir;
        } catch (Exception e) {
            throw new RuntimeException("prepare libs directory failed", e);
        }
    }

    private static String findLibFolderName() {
        String abi;  // preferred ABI
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            abi = Build.CPU_ABI;
        } else {
            abi = Build.SUPPORTED_ABIS[0];
        }

        String folder;
        if (abi.equalsIgnoreCase("arm64-v8a")) {
            folder = "armeabi-v7a";  // TODO build against arm64-v8a?
        } else if (abi.equalsIgnoreCase("armeabi-v7a")) {
            folder = "armeabi-v7a";
        } else if (abi.equalsIgnoreCase("armeabi")) {
            folder = "armeabi";
        } else if (abi.equalsIgnoreCase("x86")) {
            folder = "x86";
        } else if (abi.equalsIgnoreCase("mips")) {
            folder = "mips";
        } else {
            folder = "armeabi";
            L.e("Unsupported arch: " + abi);
        }

        String javaArch = System.getProperty("os.arch");
        if (javaArch != null && javaArch.contains("686")) {
            folder = "x86";
        }

        return folder;
    }
}
