package com.future.xlink.utils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 文件工具类
 *
 * @author sands
 * @date 2018-02-01
 */
public class ZFileUtils {
    private final static int BUFFER_LENGTH = 524288;


    /**
     * 获取文件夹
     */
    public static String getParentDirectory(String filePath) {
        return getParentDirectory(new File(filePath));
    }

    /**
     * 获取文件的文件夹
     */
    public static String getParentDirectory(File file) {
        return file.getParentFile().getAbsolutePath();
    }

    /**
     * 创建文件夹
     */
    public static boolean createDirectory(String filePath) {
        return createDirectory(new File(filePath));
    }

    /**
     * 创建文件夹，如果不存在
     */
    public static boolean createDirectory(File file) {
        if (!file.exists()) {
            return file.mkdirs();
        }
        return true;
    }

    /**
     * 创建文件，如果不存在
     */
    public static boolean createFile(String filePath) throws IOException {
        return createFile(new File(filePath));
    }

    /**
     * 创建文件，如果不存在
     */
    public static boolean createFile(File file) throws IOException {
        if (!file.exists()) {
            return file.createNewFile();
        }
        return true;
    }

    /**
     * 根据资源所在目录获取文件路径
     */
    public static URL getResource(String filePath) {
        return Thread.currentThread().getContextClassLoader().getResource(filePath);
    }

    /**
     * 根据资源所在目录获取文件路径
     */
    public static File getResourceFile(String filePath) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(filePath);
        if (resource != null) {
            return new File(resource.getPath());
        }
        return null;
    }

    /**
     * 根据资源所在目录获取文件流
     */
    public static InputStream getResourceAsStream(String filePath) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
    }

    /**
     * 根据资源所在目录获取文件流
     */
    public static FileInputStream getResourceAsFileInputStream(String filePath) throws FileNotFoundException {
        return new FileInputStream(getResource(filePath).getPath());
    }

    /**
     * 根据资源所在目录下的所有文件/文件夹(只获取一级目录)
     */
    public static File[] getFiles(String directoryFullPath) {
        File fileDir = new File(directoryFullPath);
        return fileDir.listFiles();
    }

    /**
     * 根据资源所在目录下的所有文件/文件夹(只获取一级目录)
     */
    public static File[] getResourceFiles(String directoryPath) {
        URL url = ZFileUtils.getResource(directoryPath);
        File fileDir = new File(url.getPath());
        return fileDir.listFiles();
    }

    /**
     * 根据资源所在目录下的所有文件流(只获取一级目录)
     */
    public static List<FileInputStream> getResourceAsFileInputStreams(String directoryPath) throws FileNotFoundException {
        URL url = ZFileUtils.getResource(directoryPath);
        File fileDir = new File(url.getPath());
        File[] files = fileDir.listFiles();
        List<FileInputStream> list = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (File file : files) {
                list.add(new FileInputStream(file));
            }
        }
        return list;
    }

    /**
     * 文件移动到另一目录
     */
    public static boolean moveTo(String srcPath, String fileName, String toPath, String separator) {
        String startPath = srcPath + separator + fileName;
        String endPath = toPath + separator + fileName;

        File srcFile = new File(startPath);
        File toFile = new File(endPath);//获取文件夹路径
        if (!toFile.getParentFile().exists()) {//判断文件夹是否创建，没有创建则创建新文件夹
            toFile.getParentFile().mkdirs();
        }
        return srcFile.renameTo(toFile);
    }

    /**
     * 清空已有的文件内容，以便下次重新写入新的内容
     *
     * @param fileName file name
     */
    public static void clearInfoForFile(String fileName) throws IOException {
        boolean suc = true;
        File file = new File(fileName);
        if (!file.exists()) {
            suc = file.createNewFile();
        }
        if (suc) {
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write("");
                fileWriter.flush();
            }
        }
    }

    /**
     * 清空已有的文件内容，以便下次重新写入新的内容
     *
     * @param fileName file name
     */
    public static boolean delete(String fileName) throws IOException {
        File file = new File(fileName);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 将流转换成字符串
     */
    public static String readAllString(String filePath, String charset) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            return readAllString(fileInputStream, charset, BUFFER_LENGTH);
        }
    }

    /**
     * 将流转换成字符串
     */
    public static String readAllString(String filePath, String charset, int bufferLength) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            return readAllString(fileInputStream, charset, bufferLength);
        }
    }

    /**
     * 将流转换成字符串
     */
    public static String readAllString(InputStream inputStream, String charset) throws IOException {
        return readAllString(inputStream, charset, BUFFER_LENGTH);
    }

    /**
     * 将流转换成字符串
     */
    public static String readAllString(InputStream inputStream, String charset, int bufferLength) throws IOException {
        bufferLength = bufferLength <= 0 ? BUFFER_LENGTH : bufferLength;
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[bufferLength];
        int readCount = 0;
        try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset)) {
            try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                while ((readCount = bufferedReader.read(buffer)) > 0) {
                    sb.append(new String(buffer, 0, readCount));
                }

//            boolean suc = false;
//            do {
//                String line = bufferedReader.readLine();
//                suc = line != null && line.length() > 0;
//                if (suc) {
//                    sb.append(line);
//                }
//            } while (suc);

            }
        }
        return sb.toString();
    }

    /**
     * 将流转换成byte[]
     */
    public static byte[] readAll(InputStream inputStream) throws IOException {
        return readAll(inputStream, BUFFER_LENGTH);
    }

    /**
     * 将流转换成byte[]
     */
    public static byte[] readAll(InputStream inputStream, int bufferLength) throws IOException {
        bufferLength = bufferLength <= 0 ? BUFFER_LENGTH : bufferLength;

        byte[] buffer = new byte[bufferLength];
        int readCount = 0;
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                while ((readCount = bufferedInputStream.read(buffer)) > 0) {
                    byteArrayOutputStream.write(buffer, 0, readCount);
                }
                return byteArrayOutputStream.toByteArray();
            }
        }
    }

    /**
     * 读取文件
     */
    public static byte[] readFileAll(String filePath, long seek)
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return readFileAll(filePath, seek, BUFFER_LENGTH);
    }

    /**
     * 读取文件
     */
    public static byte[] readFileAll(String filePath, long seek, int bufferLength)
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        bufferLength = bufferLength <= 0 ? BUFFER_LENGTH : bufferLength;

        byte[] buffer = new byte[bufferLength];
        int readCount = 0;
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            try (FileChannel fc = raf.getChannel()) {
                long size = fc.size();
                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                    long len = size - seek;
                    MappedByteBuffer mbbo = fc.map(FileChannel.MapMode.READ_ONLY, seek, len);
                    while (len > 0) {
                        readCount = (int) (len >= bufferLength ? bufferLength : len);
                        len -= readCount;
                        mbbo.get(buffer, 0, readCount);
                        byteArrayOutputStream.write(buffer, 0, readCount);
                    }

                    unmap(fc, mbbo);    //释放

                    return byteArrayOutputStream.toByteArray();
                }
            }
        }
    }

    /**
     * 读取文件
     */
    public static byte[] readFile(String filePath, long seek, long readLength)
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return readFile(filePath, seek, readLength, BUFFER_LENGTH);
    }

    /**
     * 读取文件
     */
    public static byte[] readFile(String filePath, long seek, long readLength, int bufferLength)
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        bufferLength = bufferLength <= 0 ? BUFFER_LENGTH : bufferLength;
        RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
        try  {
            try (FileChannel fc = raf.getChannel()) {
                long size = fc.size();
                boolean isRealALl = false;
                if (readLength <= 0) {
                    isRealALl = true;
                    readLength = size;
                }
                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
//                    long len = isRealALl ? size - seek : readLength;
//                    MappedByteBuffer mbbo = fc.map(FileChannel.MapMode.READ_ONLY, seek, readLength);

                    long total = size - seek;
                    long len = isRealALl ? total : (total > readLength ? readLength : total);
                    MappedByteBuffer mbbo = fc.map(FileChannel.MapMode.READ_ONLY, seek, len);

                    int readCount = 0;
                    byte[] buffer = new byte[bufferLength];
                    while (len > 0) {
                        readCount = (int) (len >= bufferLength ? bufferLength : len);
                        len -= readCount;
                        mbbo.get(buffer, 0, readCount);
                        byteArrayOutputStream.write(buffer, 0, readCount);
                    }

                    unmap(fc, mbbo);    //释放

                    return byteArrayOutputStream.toByteArray();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            raf.close();
        }
        return null;
    }

    /**
     * 写入文件(如果文件已经存在，那么会覆盖旧的，就是旧的内容都会被清除)
     */
    public static void writeAll(String filePath, InputStream inputStream) throws IOException {
        writeAll(filePath, inputStream, BUFFER_LENGTH);
    }

    /**
     * 写入文件(如果文件已经存在，那么会覆盖旧的，就是旧的内容都会被清除)
     */
    public static void writeAll(String filePath, InputStream inputStream, int bufferLength) throws IOException {
        bufferLength = bufferLength <= 0 ? BUFFER_LENGTH : bufferLength;
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            try (BufferedOutputStream outputStream = new BufferedOutputStream(fileOutputStream)) {
                byte[] buffer = new byte[bufferLength];
                int readCount = 0;
                while ((readCount = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, readCount);
                }
            }
        }
    }

    /**
     * 写入文件(如果文件已经存在，那么会覆盖旧的，就是旧的内容都会被清除)
     */
    public static void writeAll(String filePath, byte[] datas) throws IOException {
        writeAll(filePath, datas, 0, datas.length);
    }

    /**
     * 写入文件(如果文件已经存在，那么会覆盖旧的，就是旧的内容都会被清除)
     */
    public static void writeAll(String filePath, byte[] datas, int index, int length) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            try (BufferedOutputStream outputStream = new BufferedOutputStream(fileOutputStream)) {
                outputStream.write(datas, index, length);
            }
        }
    }

    /**
     * 写入文件(如果文件存在，那么只会写入到 seek + inputStream.length 的地方上，文件其他地方都会保留)
     */
    public static void write(String filePath, long seek, InputStream inputStream)
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        write(filePath, seek, inputStream, BUFFER_LENGTH);
    }

    /**
     * 写入文件(如果文件存在，那么只会写入到 seek + inputStream.length 的地方上，文件其他地方都会保留)
     */
    public static void write(String filePath, long seek, InputStream inputStream, int bufferLength)
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        byte[] datas = readAll(inputStream, bufferLength);
        write(filePath, seek, datas);
    }

    /**
     * 写入文件(如果文件存在，那么只会写入到 seek + datas.length 的地方上，文件其他地方都会保留)
     */
    public static void write(String filePath, long seek, byte[] datas)
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        write(filePath, seek, datas, 0, datas.length);
    }

    /**
     * 写入文件(如果文件存在，那么只会写入到 seek + length 的地方上，文件其他地方都会保留)
     */
    public static void write(String filePath, long seek, byte[] datas, int index, int length)
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
            try (FileChannel fc = raf.getChannel()) {
                MappedByteBuffer mbbo = null;
                try {
                    mbbo = fc.map(FileChannel.MapMode.READ_WRITE, seek, length);
                    mbbo.put(datas, index, length);
                } finally {
                    if (mbbo != null) {
                        mbbo.clear();
//                        unmap(fc, mbbo);    //释放
                    }
                }
            }
        }
    }

    /**
     * MappedByteBuffer的释放操作
     */
    public static void unmap(FileChannel fco, MappedByteBuffer buffer) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //sun.misc.Cleaner是内部专用 API, 可能会在未来发行版中删除
        //sun.nio.ch.DirectBuffer是内部专用 API, 可能会在未来发行版中删除
//        Cleaner cl = ((DirectBuffer)buffer).cleaner();
//        if (cl != null)
//            cl.clean();

//        Class<? extends FileChannel> clazz = fco.getClass();
//        Method m = clazz.getDeclaredMethod("unmap",
//                MappedByteBuffer.class);
//        m.setAccessible(true);
//        m.invoke(clazz, buffer);
//        Method getCleanerMethod = buffer.getClass().getMethod("cleaner",new Class[0]);
//              getCleanerMethod.setAccessible(true);
//              getCleanerMethod.invoke(clazz,buffer);
//          sun.misc.Cleaner cleaner =(sun.misc.Cleaner)getCleanerMethod.invoke(buffer,new Object[0]);
//   cleaner.clean();
    }

    /**
     * 更新文件 删除文件
     */
    public static boolean updateFile(String path) {
        boolean suc = false;

        File file = new File(path);
        if (file.exists()) {
            suc = file.delete();
        }
        String splitSign = "";
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
            splitSign = "\\\\";
        } else {
            splitSign = "/";
        }
        String[] paths = file.getPath().split(splitSign);
        String filePath = path.substring(0, path.indexOf(paths[paths.length - 1]));
        file = new File(filePath);

        if (file.exists()) {
            suc = file.delete();
        }

        return suc;
    }

}
