package com.example.baselibrary.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 文件工具类
 *
 * draft目录是用于发布视频的草稿箱，并且做了清理，所有不是draft相关的不要放在此目录
 */
object FileUtils {
    private const val MD5_FILE_BUFFER_LENGHT = 1 * 1024 * 1024 // 1MB
    private val gSyncCode = ByteArray(0)

    /**记录录制voice over 每一段的路径 */
    const val RECORD_VOICE_PATH = "/draft/record/voice/"

    /** 删除录制的每一段视频  */
    const val RECORD_PART_PATH = "/draft/record/part/"

    /** 删除保存的文本图片  */
    const val RECORD_SUBTITLE_PATH = "/draft/record/subtitle/"

    /**发布成功之后将合成的视频保存一份 */
    const val PUSH_VIDEO_PATH = "/draft/push/video/"

    //调用腾讯合成视频
    const val GENERATE_VIDEO_PATH = "/draft/generate/file/"

    /**voice over 合成成功之后音频文件 */
    const val RECORD_VOICE_PATH_TEMP = "/draft/record/temp/"

    //草稿箱存数据的根目录
    const val DRAFT_ROOT_PATH = "/draft/"

    /** 发送语音message消息保存路径 */
    const val RECORD_MESSAGE_VOICE_PATH = "/dm/message/voice/"

    /**  */
    const val DOWNLOAD_MULTI_MEDIA_FILE = "/download/multimedia/"
    const val TEMP_SQL_BACKUP = "/backup/sql/"
    fun getDownloadMultiMediaFile(context: Context): File {
        return makeFilesDir(context, DOWNLOAD_MULTI_MEDIA_FILE)
    }

    fun getDraftRootPath(context: Context): File {
        return makeFilesDir(context, DRAFT_ROOT_PATH)
    }

    fun getRecordSubtitlePath(context: Context): File {
        return makeFilesDir(context, RECORD_SUBTITLE_PATH)
    }

    @JvmStatic
    fun getRecordVoicePathTemp(context: Context): File {
        return makeFilesDir(context, RECORD_VOICE_PATH_TEMP)
    }

    fun getPushVideoPath(context: Context): File {
        return makeFilesDir(context, PUSH_VIDEO_PATH)
    }

    fun generateVideoPath(context: Context): File {
        return makeFilesDir(context, GENERATE_VIDEO_PATH)
    }

    @JvmStatic
    fun getRecordVoicePath(context: Context): File {
        return makeFilesDir(context, RECORD_VOICE_PATH)
    }

    fun getMessageVoicePath(context: Context): File {
        return makeFilesDir(context, RECORD_MESSAGE_VOICE_PATH)
    }

    fun getRecordPartPath(context: Context): File {
        return makeFilesDir(context, RECORD_PART_PATH)
    }

    fun getTempSqlBackup(context: Context): File {
        return makeAppFilesDir(context, TEMP_SQL_BACKUP)
    }

    fun makeFilesDir(context: Context, path: String?): File {
        val file: File
        file = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            File(context.getExternalFilesDir(null), path)
        } else {
            File(context.filesDir, path)
        }
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

    fun makeAppFilesDir(context: Context, path: String?): File {
        val file = File(context.filesDir, path)
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

    fun getInputStream(oFile: File): InputStream? {
        var oIn: InputStream? = null
        oIn = try {
            FileInputStream(oFile)
        } catch (ex: FileNotFoundException) {
            null
        }
        return oIn
    }

    var list: MutableList<File> = ArrayList()
    fun allFileList(file: File): MutableList<File> {
        list.clear()
        return allFileListRecursive(file)
    }

    fun allFileListRecursive(file: File): MutableList<File> {
        val fs = file.listFiles()
        for (f in fs) {
            if (f.isDirectory) //若是目录，则递归打印该目录下的文件
                allFileListRecursive(f)
            if (f.isFile) //若是文件，直接打印
                list.add(f)
        }
        return list
    }

    fun getBytes(filePath: String): ByteArray? {
        var buffer: ByteArray? = null
        var fis: FileInputStream? = null
        var bos: ByteArrayOutputStream? = null
        try {
            val file = File(filePath)
            fis = FileInputStream(file)
            bos = ByteArrayOutputStream(1000)
            val b = ByteArray(1000)
            var n: Int
            while (fis.read(b).also { n = it } != -1) {
                bos.write(b, 0, n)
            }
            buffer = bos.toByteArray()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (fis != null) {
                try {
                    fis.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (bos != null) {
                try {
                    bos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return buffer
    }

    fun getFile(bfile: ByteArray, filePath: String, fileName: String) {
        var bos: BufferedOutputStream? = null
        var fos: FileOutputStream? = null
        var file: File? = null
        try {
            val dir = File(filePath)
            if (!dir.exists()) { // 判断文件目录是否存在
                val mkdirsResult = dir.mkdirs()
            }
            file = File(filePath + fileName)
            fos = FileOutputStream(file)
            bos = BufferedOutputStream(fos)
            bos.write(bfile)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (bos != null) {
                try {
                    bos.close()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
            }
            if (fos != null) {
                try {
                    fos.close()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
            }
        }
    }

    /**
     * 保存图片到sdcard中
     */
    fun writeBitmapToFile(context: Context, pBitmap: Bitmap?, strName: String?): String {
        val file = File(getRecordVoicePath(context), strName)
        if (pBitmap == null) {
            return ""
        }
        try {
            val bos = BufferedOutputStream(FileOutputStream(file))
            pBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
            bos.flush()
            bos.close()
            return file.absolutePath
        } catch (e: Exception) {
        }
        return ""
    }

    private const val TAG = "FileUtils"
    const val FILE_EXTENSION_SEPARATOR = "."

    /**
     * read file
     *
     * @param filePath
     * @param charsetName The name of a supported [&lt;/code&gt;charset&lt;code&gt;][java.nio.charset.Charset]
     * @return if file not exist, return null, else return content of file
     * @throws RuntimeException if an error occurs while operator BufferedReader
     */
    fun readFile(filePath: String, charsetName: String): StringBuilder? {
        val file = File(filePath)
        val fileContent = StringBuilder("")
        if (file == null || !file.isFile) {
            return null
        }
        var reader: BufferedReader? = null
        return try {
            val `is` = InputStreamReader(FileInputStream(file), charsetName)
            reader = BufferedReader(`is`)
            var line: String? = null
            while (reader.readLine().also { line = it } != null) {
                if (fileContent.toString() != "") {
                    fileContent.append(System.getProperty("line.separator"))
                }
                fileContent.append(line)
            }
            reader.close()
            fileContent
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                    return null
                }
            }
        }
    }
    /**
     * write file
     *
     * @param filePath
     * @param content
     * @param append is append, if true, write to the end of file, else clear content of file and write into it
     * @return return false if content is empty, true otherwise
     * @throws RuntimeException if an error occurs while operator FileWriter
     */
    /**
     * write file, the string will be written to the begin of the file
     *
     * @param filePath
     * @param content
     * @return
     */
    @JvmOverloads
    fun writeFile(filePath: String, content: String, append: Boolean = false): Boolean {
        if (TextUtils.isEmpty(content)) {
            return false
        }
        var fileWriter: FileWriter? = null
        return try {
            makeDirs(filePath)
            fileWriter = FileWriter(filePath, append)
            fileWriter.write(content)
            fileWriter.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close()
                    return false
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * write file
     *
     * @param filePath
     * @param contentList
     * @param append is append, if true, write to the end of file, else clear content of file and write into it
     * @return return false if contentList is empty, true otherwise
     * @throws RuntimeException if an error occurs while operator FileWriter
     */
    fun writeFile(filePath: String, contentList: List<String?>?, append: Boolean): Boolean {
        if (contentList == null || contentList.isEmpty()) {
            return false
        }
        var fileWriter: FileWriter? = null
        return try {
            makeDirs(filePath)
            fileWriter = FileWriter(filePath, append)
            for (line in contentList) {
                fileWriter.write(line)
                fileWriter.write(System.getProperty("line.separator"))
            }
            fileWriter.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * write file, the string list will be written to the begin of the file
     *
     * @param filePath
     * @param contentList
     * @return
     */
    fun writeFile(filePath: String, contentList: List<String?>?): Boolean {
        return writeFile(filePath, contentList, false)
    }

    /**
     * write file, the bytes will be written to the begin of the file
     *
     * @param filePath
     * @param stream
     * @return
     * @see {@link .writeFile
     */
    fun writeFile(filePath: String?, stream: InputStream?): Boolean {
        return writeFile(filePath, stream, false)
    }

    /**
     * write file
     *
     * @param filePath the file to be opened for writing.
     * @param stream the input stream
     * @param append if `true`, then bytes will be written to the end of the file rather than the beginning
     * @return return true
     * @throws RuntimeException if an error occurs while operator FileOutputStream
     */
    fun writeFile(filePath: String?, stream: InputStream?, append: Boolean): Boolean {
        return writeFile(if (filePath != null) File(filePath) else null, stream, append)
    }

    /**
     * write file, the bytes will be written to the begin of the file
     *
     * @param file
     * @param stream
     * @return
     * @see {@link .writeFile
     */
    fun writeFile(file: File?, stream: InputStream?): Boolean {
        return writeFile(file, stream, false)
    }

    /**
     * write file
     *
     * @param file the file to be opened for writing.
     * @param stream the input stream
     * @param append if `true`, then bytes will be written to the end of the file rather than the beginning
     * @return return true
     * @throws RuntimeException if an error occurs while operator FileOutputStream
     */
    fun writeFile(file: File?, stream: InputStream?, append: Boolean): Boolean {
        if (file == null || stream == null) {
            return false
        }
        var o: OutputStream? = null
        return try {
            makeDirs(file.absolutePath)
            o = FileOutputStream(file, append)
            val data = ByteArray(1024)
            var length = -1
            while (stream.read(data).also { length = it } != -1) {
                o.write(data, 0, length)
            }
            o.flush()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            if (o != null) {
                try {
                    o.close()
                    stream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * copy file
     *
     * @param sourceFilePath
     * @param destFilePath
     * @return
     * @throws RuntimeException if an error occurs while operator FileOutputStream
     */
    fun copyFile(sourceFilePath: String?, destFilePath: String?): Boolean {
        var inputStream: InputStream? = null
        try {
            inputStream = FileInputStream(sourceFilePath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return writeFile(destFilePath, inputStream)
    }

    /**
     * read file to string list, a element of list is a line
     *
     * @param filePath
     * @param charsetName The name of a supported [&lt;/code&gt;charset&lt;code&gt;][java.nio.charset.Charset]
     * @return if file not exist, return null, else return content of file
     * @throws RuntimeException if an error occurs while operator BufferedReader
     */
    fun readFileToList(filePath: String, charsetName: String): List<String?>? {
        val file = File(filePath)
        val fileContent: MutableList<String?> = ArrayList()
        if (file == null || !file.isFile) {
            return null
        }
        var reader: BufferedReader? = null
        return try {
            val `is` = InputStreamReader(FileInputStream(file), charsetName)
            reader = BufferedReader(`is`)
            var line: String? = null
            while (reader.readLine().also { line = it } != null) {
                fileContent.add(line)
            }
            reader.close()
            fileContent
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * get file name from path, not include suffix
     *
     * <pre>
     * getFileNameWithoutExtension(null)               =   null
     * getFileNameWithoutExtension("")                 =   ""
     * getFileNameWithoutExtension("   ")              =   "   "
     * getFileNameWithoutExtension("abc")              =   "abc"
     * getFileNameWithoutExtension("a.mp3")            =   "a"
     * getFileNameWithoutExtension("a.b.rmvb")         =   "a.b"
     * getFileNameWithoutExtension("c:\\")              =   ""
     * getFileNameWithoutExtension("c:\\a")             =   "a"
     * getFileNameWithoutExtension("c:\\a.b")           =   "a"
     * getFileNameWithoutExtension("c:a.txt\\a")        =   "a"
     * getFileNameWithoutExtension("/home/admin")      =   "admin"
     * getFileNameWithoutExtension("/home/admin/a.txt/b.mp3")  =   "b"
    </pre> *
     *
     * @param filePath
     * @return file name from path, not include suffix
     * @see
     */
    fun getFileNameWithoutExtension(filePath: String): String {
        if (TextUtils.isEmpty(filePath)) {
            return filePath
        }
        val extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR)
        val filePosi = filePath.lastIndexOf(File.separator)
        if (filePosi == -1) {
            return if (extenPosi == -1) filePath else filePath.substring(0, extenPosi)
        }
        if (extenPosi == -1) {
            return filePath.substring(filePosi + 1)
        }
        return if (filePosi < extenPosi) filePath.substring(
            filePosi + 1,
            extenPosi
        ) else filePath.substring(filePosi + 1)
    }

    /**
     * get file name from path, include suffix
     *
     * <pre>
     * getFileName(null)               =   null
     * getFileName("")                 =   ""
     * getFileName("   ")              =   "   "
     * getFileName("a.mp3")            =   "a.mp3"
     * getFileName("a.b.rmvb")         =   "a.b.rmvb"
     * getFileName("abc")              =   "abc"
     * getFileName("c:\\")              =   ""
     * getFileName("c:\\a")             =   "a"
     * getFileName("c:\\a.b")           =   "a.b"
     * getFileName("c:a.txt\\a")        =   "a"
     * getFileName("/home/admin")      =   "admin"
     * getFileName("/home/admin/a.txt/b.mp3")  =   "b.mp3"
    </pre> *
     *
     * @param filePath
     * @return file name from path, include suffix
     */
    fun getFileName(filePath: String): String {
        if (TextUtils.isEmpty(filePath)) {
            return filePath
        }
        val filePosi = filePath.lastIndexOf(File.separator)
        return if (filePosi == -1) filePath else filePath.substring(filePosi + 1)
    }

    /**
     * get folder name from path
     *
     * <pre>
     * getFolderName(null)               =   null
     * getFolderName("")                 =   ""
     * getFolderName("   ")              =   ""
     * getFolderName("a.mp3")            =   ""
     * getFolderName("a.b.rmvb")         =   ""
     * getFolderName("abc")              =   ""
     * getFolderName("c:\\")              =   "c:"
     * getFolderName("c:\\a")             =   "c:"
     * getFolderName("c:\\a.b")           =   "c:"
     * getFolderName("c:a.txt\\a")        =   "c:a.txt"
     * getFolderName("c:a\\b\\c\\d.txt")    =   "c:a\\b\\c"
     * getFolderName("/home/admin")      =   "/home"
     * getFolderName("/home/admin/a.txt/b.mp3")  =   "/home/admin/a.txt"
    </pre> *
     *
     * @param filePath
     * @return
     */
    fun getFolderName(filePath: String): String {
        if (TextUtils.isEmpty(filePath)) {
            return filePath
        }
        val filePosi = filePath.lastIndexOf(File.separator)
        return if (filePosi == -1) "" else filePath.substring(0, filePosi)
    }

    /**
     * get suffix of file from path
     *
     * <pre>
     * getFileExtension(null)               =   ""
     * getFileExtension("")                 =   ""
     * getFileExtension("   ")              =   "   "
     * getFileExtension("a.mp3")            =   "mp3"
     * getFileExtension("a.b.rmvb")         =   "rmvb"
     * getFileExtension("abc")              =   ""
     * getFileExtension("c:\\")              =   ""
     * getFileExtension("c:\\a")             =   ""
     * getFileExtension("c:\\a.b")           =   "b"
     * getFileExtension("c:a.txt\\a")        =   ""
     * getFileExtension("/home/admin")      =   ""
     * getFileExtension("/home/admin/a.txt/b")  =   ""
     * getFileExtension("/home/admin/a.txt/b.mp3")  =   "mp3"
    </pre> *
     *
     * @param filePath
     * @return
     */
    fun getFileExtension(filePath: String): String {
        if (TextUtils.isEmpty(filePath)) {
            return filePath
        }
        val extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR)
        val filePosi = filePath.lastIndexOf(File.separator)
        if (extenPosi == -1) {
            return ""
        }
        return if (filePosi >= extenPosi) "" else filePath.substring(extenPosi + 1)
    }

    /**
     * Creates the directory named by the trailing filename of this file, including the complete directory path required
     * to create this directory. <br></br>
     * <br></br>
     *
     * **Attentions:**
     *  * makeDirs("C:\\Users\\Trinea") can only create users folder
     *  * makeFolder("C:\\Users\\Trinea\\") can create Trinea folder
     *
     *
     * @param filePath
     * @return true if the necessary directories have been created or the target directory already exists, false one of
     * the directories can not be created.
     *
     *  * if [FileUtils.getFolderName] return null, return false
     *  * if target directory already exists, return true
     *  * return
     *
     */
    fun makeDirs(filePath: String): Boolean {
        val folderName = getFolderName(filePath)
        if (TextUtils.isEmpty(folderName)) {
            return false
        }
        val folder = File(folderName)
        return if (folder.exists() && folder.isDirectory) true else folder.mkdirs()
    }

    /**
     * @param filePath
     * @return
     * @see .makeDirs
     */
    fun makeFolders(filePath: String): Boolean {
        return makeDirs(filePath)
    }

    /**
     * Indicates if this file represents a file on the underlying file system.
     *
     * @param filePath
     * @return
     */
    fun isFileExist(filePath: String): Boolean {
        if (TextUtils.isEmpty(filePath)) {
            return false
        }
        val file = File(filePath)
        return file.exists() && file.isFile
    }

    fun isFolder(filePath: String): Boolean {
        if (TextUtils.isEmpty(filePath)) {
            return false
        }
        val file = File(filePath)
        return file.isDirectory
    }

    /**
     * Indicates if this file represents a file on the underlying file system.
     *
     * @param filePath
     * @return
     */
    fun isFileExist(filePath: String, size: Long): Boolean {
        if (TextUtils.isEmpty(filePath)) {
            return false
        }
        val file = File(filePath)
        return file.exists() && file.isFile && file.length() == size
    }

    /**
     * Indicates if this file represents a file on the underlying file system.
     *
     * @param filePath
     * @return
     */
    fun isFileExist(filePath: String, size: Long, checkSum: String): Boolean {
        if (TextUtils.isEmpty(filePath)) {
            return false
        }
        if (TextUtils.isEmpty(checkSum)) {
            return false
        }
        val file = File(filePath)
        if (!file.exists() || !file.isFile) {
            return false
        }
        var fileSum: String? = ""
        try {
            fileSum = getMD5FromFile(filePath)
        } catch (e: IOException) {
        }
        return file.length() == size && checkSum == fileSum
    }

    /**
     * Indicates if this file represents a directory on the underlying file system.
     *
     * @param directoryPath
     * @return
     */
    fun isFolderExist(directoryPath: String): Boolean {
        if (TextUtils.isEmpty(directoryPath)) {
            return false
        }
        val dire = File(directoryPath)
        return dire.exists() && dire.isDirectory
    }

    /**
     * delete file or directory
     *
     *  * if path is null or empty, return true
     *  * if path not exist, return true
     *  * if path exist, delete recursion. return true
     *
     *
     * @param path
     * @return
     */
    @JvmStatic
    fun deleteFile(path: String): Boolean {
        synchronized(gSyncCode) {
            if (TextUtils.isEmpty(path)) {
                return true
            }
            val file = File(path)
            if (!file.exists()) {
                return true
            }
            if (file.isFile) {
                return file.delete()
            }
            if (!file.isDirectory) {
                return false
            }
            val filesList = file.listFiles()
            if (filesList != null) {
                for (f in filesList) {
                    if (f.isFile) {
                        f.delete()
                    } else if (f.isDirectory) {
                        deleteFile(f.absolutePath)
                    }
                }
            }
            return file.delete()
        }
    }

    fun deleteFileExceptList(path: String, list: List<String?>): Boolean {
        synchronized(gSyncCode) {
            if (TextUtils.isEmpty(path)) {
                return true
            }
            val file = File(path)
            if (!file.exists()) {
                return true
            }
            if (file.isFile) {
                return file.delete()
            }
            if (!file.isDirectory) {
                return false
            }
            val filesList = file.listFiles()
            if (filesList != null) {
                for (f in filesList) {
                    if (f.isFile) {
                        if (!list.contains(f.absolutePath)) {
                            f.delete()
                        }
                    } else if (f.isDirectory) {
                        deleteFileExceptList(f.absolutePath, list)
                    }
                }
            }
            return file.delete()
        }
    }

    /**
     * @Method: fileRename
     * @Description: 将文件从fromName命名为toName，由于使用的是File自带的renameTo()接口，需要注意：  * 读写存储器权限  *
     * fromName和toName这两个路径在相同的挂载点。如果不在同一挂载点，重命名失败。
     * @param fromName 需要重命名的文件，为文件绝对路径
     * @param toName 要改成的名字，为文件绝对路径
     * @return boolean 成功或失败
     */
    fun fileRename(fromName: String, toName: String): Boolean {
        synchronized(gSyncCode) {

            val fromFile = File(fromName)
            val toFile = File(toName)
            if (!fromFile.exists()) {
                return false
            }
            val result = fromFile.renameTo(toFile)
            if (result) {
            }
            return result
        }
    }

    /**
     * get file size
     *
     *  * if path is null or empty, return -1
     *  * if path exist and it is a file, return file size, else return -1
     *
     *
     * @param path
     * @return returns the length of this file in bytes. returns -1 if the file does not exist.
     */
    fun getFileSize(path: String): Long {
        if (TextUtils.isEmpty(path)) {
            return -1
        }
        val file = File(path)
        return if (file.exists() && file.isFile) file.length() else -1
    }

    @Throws(IOException::class)
    fun getMD5FromFile(filePath: String?): String? {
        if (filePath == null) {
            return null
        }
        var digestString: String? = null
        val file = File(filePath)
        val buffer = ByteArray(1024)
        var inputStream: InputStream? = null
        var digest: MessageDigest? = null
        try {
            digest = MessageDigest.getInstance("MD5")
        } catch (e1: NoSuchAlgorithmException) {
        }
        val fileSize = file.length()
        if (fileSize > 3 * MD5_FILE_BUFFER_LENGHT) {
            var raf: RandomAccessFile? = null
            Log.d(TAG, "fileSize is greater than 3MB")
            // 大于3MB时，分段，分头、中、尾，各1MB
            try {
                raf = RandomAccessFile(file, "r")
                raf.seek(0)
                var bytesRead = 0
                var totalRead = 0
                val bytesToRead = 3 * MD5_FILE_BUFFER_LENGHT

                /*
                 * 计算中、尾部起始位置，头部起始为0，不用计算 ------------------------------------------------- |Head 1MB| 间隔大小 | Mid 1MB| 间隔大小
                 * | Tail 1MB| ------------------------------------------------- 其中间隔大小计算方式为： (fileSize - 3 *
                 * MD5_FILE_BUFFER_LENGHT) / 2 所以中部起始位置为：Head + 一个间隔的大小 尾部起始点为：(fileSize - MD5_FILE_BUFFER_LENGHT)
                 */
                val midStartPosition =
                    MD5_FILE_BUFFER_LENGHT + (fileSize - 3 * MD5_FILE_BUFFER_LENGHT).toInt() / 2
                val tailStartPosition = (fileSize - MD5_FILE_BUFFER_LENGHT).toInt()
//                Log.d(
//                    TAG,
//                    String.format(
//                        "midStartPosition = %d, tailStartPosition = %d",
//                        midStartPosition,
//                        tailStartPosition
//                    )
//                )
                val data = ByteArray(128 * 1024)
                while (totalRead < bytesToRead) {
                    // Log.d(TAG, "count = " + (++count));
                    bytesRead = raf.read(data)
                    totalRead += bytesRead
                    if (totalRead == MD5_FILE_BUFFER_LENGHT) {
                        // 读完头部，开始读取中部
                        Log.d(TAG, "totalRead == MD5_FILE_BUFFER_LENGHT")
                        raf.seek(midStartPosition.toLong())
                    } else if (totalRead == 2 * MD5_FILE_BUFFER_LENGHT) {
                        // 读完中部，开始读取尾部
                        Log.d(TAG, "totalRead == 2 * MD5_FILE_BUFFER_LENGHT")
                        raf.seek(tailStartPosition.toLong())
                    }
                    digest!!.update(data)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (raf != null) {
                    try {
                        raf.close()
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                    }
                }
            }
        } else {
            Log.d(TAG, "fileSize is smaller than 3MB")
            try {
                inputStream = FileInputStream(file)
                var readSize = 0
                while (readSize != -1) {
                    readSize = inputStream.read(buffer, 0, buffer.size)
                    if (readSize > 0) {
                        digest!!.update(buffer, 0, readSize)
                        // Log.d(TAG, "update: " + BytesUtil.byte2hexWithoutSpace(digest.digest()));
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close()
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                    }
                }
            }
        }
        if (digest != null) {
            digestString = byte2hexWithoutSpace(digest.digest())
        }
        return digestString
    }

    fun byte2hexWithoutSpace(buffer: ByteArray): String {
        return buffer.joinToString(""){"%02x".format(it)}
    }

    /**
     * @Method: convertSize
     * @Description: 将字节换算为B、KB、MB、GB显示
     * @param length
     * @return 返回类型：String
     */
    fun convertSize(length: Long): String {
        val df = DecimalFormat("#.00")
        var fileSizeString = ""
        fileSizeString = if (length == 0L) {
            "0 B"
        } else if (length < 1024) {
            df.format(length.toDouble()) + " B"
        } else if (length < 1048576) {
            df.format(length.toDouble() / 1024) + " KB"
        } else if (length < 1073741824) {
            df.format(length.toDouble() / 1048576) + " MB"
        } else {
            df.format(length.toDouble() / 1073741824) + " GB"
        }
        return fileSizeString
    }

    /**
     * @Method: copyFromAssetToSdcard
     * @Description: 执行拷贝任务
     * @param context
     * @param assetFilename 需要拷贝的assets文件路径
     * @param dstPath
     * @return
     * @throws IOException
     * @返回类型：boolean
     */
    @Throws(IOException::class)
    fun copyFromAssetToSdcard(context: Context, assetFilename: String, dstPath: String?): Boolean {
        var source: InputStream? = null
        var destination: OutputStream? = null
        try {
            source = context.assets.open(File(assetFilename).path)
            val destinationFile = File(dstPath, assetFilename)
            destinationFile.parentFile.mkdirs()
            destination = FileOutputStream(destinationFile)
            val buffer = ByteArray(1024)
            var nread: Int
            while (source.read(buffer).also { nread = it } != -1) {
                if (nread == 0) {
                    nread = source.read()
                    if (nread < 0) break
                    destination.write(nread)
                    continue
                }
                destination.write(buffer, 0, nread)
            }
        } finally {
            if (source != null) {
                try {
                    source.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (destination != null) {
                try {
                    destination.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return true
    }

    /**
     * 拷贝assets目录下的 指定文件夹 下的所有文件夹及文件到指定文件夹
     * @param context
     * @param assetsPath assets下指定文件夹
     * @param savePath 目标指定文件夹
     */
    @JvmStatic
    fun copyFilesFromAssets(context: Context, assetsPath: String, savePath: String) {
        try {
            val file = File(savePath)
            val fileNames = context.assets.list(assetsPath) // 获取assets目录下的所有文件及目录名
            if (fileNames!!.size > 0) { // 如果是目录
                file.mkdirs() // 如果文件夹不存在，则递归
                for (fileName in fileNames) {
                    copyFilesFromAssets(
                        context, "$assetsPath/$fileName",
                        "$savePath/$fileName"
                    )
                }
            } else { // 如果是文件
                val `is` = context.assets.open(assetsPath)
                val fos = FileOutputStream(File(savePath))
                val buffer = ByteArray(1024)
                var byteCount = 0
                while (`is`.read(buffer).also { byteCount = it } != -1) { // 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount) // 将读取的输入流写入到输出流
                }
                fos.flush() // 刷新缓冲区
                `is`.close()
                fos.close()
            }
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    /**
     * @Method: deleteLocalFile
     * @Description: 删除当前的文件
     * @param path
     * @return
     * 返回类型：boolean
     */
    fun deleteLocalFile(path: String): Boolean {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
        return true
    }

    /**
     * 根据文件路径获取json配置string
     * @param filePath 配置文件的绝对路径
     * @return
     */
    @JvmStatic
    fun getJsonFromFile(filePath: String): String {
        //获取json数据
        val configJsonStr = StringBuilder()
        var tempString: String?
        var bufferedReader: BufferedReader? = null
        try {
            val fileInputStream = FileInputStream(filePath)
            bufferedReader = BufferedReader(InputStreamReader(fileInputStream))
            while (bufferedReader.readLine().also { tempString = it } != null) {
                configJsonStr.append(tempString)
            }
            bufferedReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return configJsonStr.toString()
    }

    fun getFolderSize(file: File): Long {
        var size: Long = 0
        try {
            val fileList = file.listFiles()
            for (i in fileList.indices) {
                size = if (fileList[i].isDirectory) {
                    size + getFolderSize(fileList[i])
                } else {
                    size + fileList[i].length()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }

    /**
     * get Local video duration
     *
     * @return
     */
    fun getLocalVideoDuration(videoPath: String?): Float {
        //除以 1000 返回是秒
        return try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(videoPath)
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toInt() / 1000.0f
        } catch (e: Exception) {
            0f
        }
    }

    /**
     * create a file path
     */
    fun createImageFile(context: Context, isCrop: Boolean = false): File? {
        return try {
            val rootFolderPath = context.getExternalFilesDir(null)?.absolutePath + File.separator + "cropped"
            var rootFile = File(rootFolderPath + File.separator + "capture")
            if (!rootFile.exists())
                rootFile.mkdirs()

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val fileName = if (isCrop) "IMG_${timeStamp}_CROP.jpg" else "IMG_$timeStamp.jpg"
            File(rootFile.absolutePath + File.separator + fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * get image rotate degree
     *
     */
    fun readPictureDegree(src:String):Int{
        var degree = 0
        try {
            val exifInterface = ExifInterface(src)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                else -> {}
            }
        }catch (e:IOException){

        }
        return degree
    }

    /**
     * rotate the image, make it normal
     */
    fun rotateBitmap(bitmap: Bitmap?, rotate:Int):Bitmap?{
        return bitmap?.let {
            val mtx = Matrix()
            mtx.postRotate(rotate.toFloat())
            Bitmap.createBitmap(it, 0,0, it.width,it.height,mtx,true)
        }
    }


}