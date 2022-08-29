package com.example.baselibrary.utils

import android.graphics.Bitmap
import android.media.ExifInterface
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipUtils {
    const val TAG = "ZIP"

    /**
     * 解压zip到指定的路径
     *
     * @param zipFileString ZIP的名称
     * @param outPathString 要解压缩路径
     * @throws Exception
     */
    @Throws(Exception::class)
    fun UnZipFolder(zipFileString: String?, outPathString: String) {
        val inZip = ZipInputStream(FileInputStream(zipFileString))
        var zipEntry: ZipEntry
        var szName = ""
        while (inZip.nextEntry.also { zipEntry = it } != null) {
            szName = zipEntry.name
            if (zipEntry.isDirectory) {
                //获取部件的文件夹名
                szName = szName.substring(0, szName.length - 1)
                val folder = File(outPathString + File.separator + szName)
                folder.mkdirs()
            } else {
//                Log.d(TAG, outPathString + File.separator + szName)
                val file = File(outPathString + File.separator + szName)
                if (!file.exists()) {
//                    Log.d(TAG, "Create the file:" + outPathString + File.separator + szName)
                    file.parentFile.mkdirs()
                    file.createNewFile()
                }
                // 获取文件的输出流
                val out = FileOutputStream(file)
                var len: Int
                val buffer = ByteArray(1024)
                // 读取（字节）字节到缓冲区
                while (inZip.read(buffer).also { len = it } != -1) {
                    // 从缓冲区（0）位置写入（字节）字节
                    out.write(buffer, 0, len)
                    out.flush()
                }
                out.close()
            }
        }
        inZip.close()
    }

    fun pictureDegree(path: String): Int {
        try {
            val exifInterface = ExifInterface(path)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    90
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    270
                }
                else -> {
                    0
                }
            }
        } catch (e: Exception) {
            return 0
        }
    }

    @Throws(Exception::class)
    fun UnZipFolder(zipFileString: String?, outPathString: String, szName: String) {
        var szName = szName
        val inZip = ZipInputStream(FileInputStream(zipFileString))
        var zipEntry: ZipEntry
        while (inZip.nextEntry.also { zipEntry = it } != null) {
            //szName = zipEntry.getName();
            if (zipEntry.isDirectory) {
                //获取部件的文件夹名
                szName = szName.substring(0, szName.length - 1)
                val folder = File(outPathString + File.separator + szName)
                folder.mkdirs()
            } else {
//                Log.d(TAG, outPathString + File.separator + szName)
                val file = File(outPathString + File.separator + szName)
                if (!file.exists()) {
//                    Log.d(TAG, "Create the file:" + outPathString + File.separator + szName)
                    file.parentFile.mkdirs()
                    file.createNewFile()
                }
                // 获取文件的输出流
                val out = FileOutputStream(file)
                var len: Int
                val buffer = ByteArray(1024)
                // 读取（字节）字节到缓冲区
                while (inZip.read(buffer).also { len = it } != -1) {
                    // 从缓冲区（0）位置写入（字节）字节
                    out.write(buffer, 0, len)
                    out.flush()
                }
                out.close()
            }
        }
        inZip.close()
    }

    /**
     * 压缩文件list
     *
     * @param fileList 要压缩的文件list
     * @param zipFileString 完成的Zip路径
     * @throws Exception
     */
    @Throws(Exception::class)
    fun ZipFileList(fileList: List<String>, zipFileString: String?) {
        //创建ZIP
        val outZip = ZipOutputStream(FileOutputStream(zipFileString))

        //压缩
        for (i in fileList.indices) {
            if (fileList[i].isEmpty()) {
                continue
            }
            val file = File(fileList[i])
            if (file.exists() && file.isFile) {
                val zipEntry = ZipEntry(file.name)
                val inputStream = FileInputStream(file)
                outZip.putNextEntry(zipEntry)
                var len: Int
                val buffer = ByteArray(4096)
                while (inputStream.read(buffer).also { len = it } != -1) {
                    outZip.write(buffer, 0, len)
                }
                outZip.closeEntry()
            }
        }

        //完成和关闭
        outZip.finish()
        outZip.close()
    }

    /**
     * 压缩文件和文件夹
     *
     * @param srcFileString 要压缩的文件或文件夹
     * @param zipFileString 解压完成的Zip路径
     * @throws Exception
     */
    @Throws(Exception::class)
    fun ZipFolder(srcFileString: String?, zipFileString: String?) {
        //创建ZIP
        val outZip = ZipOutputStream(FileOutputStream(zipFileString))
        //创建文件
        val file = File(srcFileString)
        //压缩
        ZipFiles(file.parent + File.separator, file.name, outZip)
        //完成和关闭
        outZip.finish()
        outZip.close()
    }

    /**
     * 压缩文件
     *
     * @param folderString
     * @param fileString
     * @param zipOutputSteam
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun ZipFiles(
        folderString: String,
        fileString: String,
        zipOutputSteam: ZipOutputStream?
    ) {
        if (zipOutputSteam == null) return
        val file = File(folderString + fileString)
        if (file.isFile) {
            val zipEntry = ZipEntry(fileString)
            val inputStream = FileInputStream(file)
            zipOutputSteam.putNextEntry(zipEntry)
            var len: Int
            val buffer = ByteArray(4096)
            while (inputStream.read(buffer).also { len = it } != -1) {
                zipOutputSteam.write(buffer, 0, len)
            }
            zipOutputSteam.closeEntry()
        } else {
            //文件夹
            val fileList = file.list()
            //没有子文件和压缩
            if (fileList.size <= 0) {
                val zipEntry = ZipEntry(fileString + File.separator)
                zipOutputSteam.putNextEntry(zipEntry)
                zipOutputSteam.closeEntry()
            }
            //子文件和递归
            for (i in fileList.indices) {
                ZipFiles(folderString, fileString + File.separator + fileList[i], zipOutputSteam)
            }
        }
    }

    /**
     * 返回zip的文件输入流
     *
     * @param zipFileString zip的名称
     * @param fileString    ZIP的文件名
     * @return InputStream
     * @throws Exception
     */
    @Throws(Exception::class)
    fun UpZip(zipFileString: String?, fileString: String?): InputStream {
        val zipFile = ZipFile(zipFileString)
        val zipEntry = zipFile.getEntry(fileString)
        return zipFile.getInputStream(zipEntry)
    }

    /**
     * 返回ZIP中的文件列表（文件和文件夹）
     *
     * @param zipFileString  ZIP的名称
     * @param bContainFolder 是否包含文件夹
     * @param bContainFile   是否包含文件
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun GetFileList(
        zipFileString: String?,
        bContainFolder: Boolean,
        bContainFile: Boolean
    ): List<File> {
        val fileList: MutableList<File> = ArrayList()
        val inZip = ZipInputStream(FileInputStream(zipFileString))
        var zipEntry: ZipEntry
        var szName = ""
        while (inZip.nextEntry.also { zipEntry = it } != null) {
            szName = zipEntry.name
            if (zipEntry.isDirectory) {
                // 获取部件的文件夹名
                szName = szName.substring(0, szName.length - 1)
                val folder = File(szName)
                if (bContainFolder) {
                    fileList.add(folder)
                }
            } else {
                val file = File(szName)
                if (bContainFile) {
                    fileList.add(file)
                }
            }
        }
        inZip.close()
        return fileList
    }

    /**
     * 质量压缩
     * 设置bitmap options属性，降低图片的质量，像素不会减少
     * 第一个参数为需要压缩的bitmap图片对象，第二个参数为压缩后图片保存的位置
     * 设置options 属性0-100，来实现压缩（因为png是无损压缩，所以该属性对png是无效的）
     *
     * @param bmp
     * @param file
     */
    fun qualityCompress(bmp: Bitmap, file: File?) {
        // 0-100 100为不压缩
        val quality = 40
        val baos = ByteArrayOutputStream()
        // 把压缩后的数据存放到baos中
        bmp.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        try {
            val fos = FileOutputStream(file)
            fos.write(baos.toByteArray())
            fos.flush()
            fos.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

}