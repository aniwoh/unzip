package com.aniwoh.unzip;

import net.lingala.zip4j.ZipFile;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

import java.io.*;
import java.nio.charset.Charset;

public class UnzipUtility {
    public static void unzip_zip(String zipFilePath, String destDir) {
        try {
            ZipFile zipFile = new ZipFile(zipFilePath);
            // 设置文件名的编码格式
            zipFile.setCharset(Charset.forName("gbk"));
            // 设置解压目标路径
            zipFile.extractAll(destDir);
            System.out.println("Unzip successful!");
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void unzip_rar5(String rarDir, String outDir)throws IOException{
        RandomAccessFile randomAccessFile = null;
        IInArchive inArchive = null;
        // 第一个参数是需要解压的压缩包路径，第二个参数参考JdkAPI文档的RandomAccessFile
        randomAccessFile = new RandomAccessFile(rarDir, "r");
        inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));

        int[] in = new int[inArchive.getNumberOfItems()];
        for (int i = 0; i < in.length; i++) {
            in[i] = i;
        }
        inArchive.extract(in, false, new ExtractCallback(inArchive, "366", outDir));
    }

    /**
     *7Z 压缩
     * @param name 压缩后的文件路径（如 D:\SevenZip\test.7z）
     * @param files 需要压缩的文件
     */
    public static void zip_7z(String name, File... files) {
        try (
                SevenZOutputFile out = new SevenZOutputFile(new File(name))){
            for (File file : files){
                addToArchiveCompression(out, file, ".");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解压7z文件
     * @param orgPath 源压缩文件地址
     * @param tarpath 解压后存放的目录地址
     */
    public static void unzip_7z(String orgPath, String tarpath) {
        try {
            SevenZFile sevenZFile = new SevenZFile(new File(orgPath));
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            while (entry != null) {
                File file = new File(tarpath + File.separator + entry.getName());
                if (entry.isDirectory()) {
                    if(!file.exists()) {
                        file.mkdirs();
                    }
                    entry = sevenZFile.getNextEntry();
                    continue;
                }
                if(!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                FileOutputStream out = new FileOutputStream(file);
                byte[] content = new byte[(int) entry.getSize()];
                sevenZFile.read(content, 0, content.length);
                out.write(content);
                out.close();
                entry = sevenZFile.getNextEntry();
            }
            sevenZFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addToArchiveCompression(SevenZOutputFile out, File file, String dir) {
        String name = dir + File.separator + file.getName();
        if(dir.equals(".")) {
            name = file.getName();
        }
        if (file.isFile()){
            SevenZArchiveEntry entry = null;
            FileInputStream in = null;
            try {
                entry = out.createArchiveEntry(file, name);
                out.putArchiveEntry(entry);
                in = new FileInputStream(file);
                byte[] b = new byte[1024];
                int count = 0;
                while ((count = in.read(b)) > 0) {
                    out.write(b, 0, count);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    out.closeArchiveEntry();
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null){
                for (File child : children){
                    addToArchiveCompression(out, child, name);
                }
            }
        } else {
            System.out.println(file.getName() + " is not supported");
        }
    }

    public static void main(String zipFilePath, String destDir) throws IOException {
        // 测试解压缩功能
//        String zipFilePath = "C:\\Users\\aniwoh\\Desktop\\test.zip";
//        String destDir = "C:\\Users\\aniwoh\\Desktop\\dest";
        String extendname;
        int lastIndex = zipFilePath.lastIndexOf(".");
        if (lastIndex != -1 && lastIndex < zipFilePath.length() - 1) {
            extendname=zipFilePath.substring(lastIndex + 1);
        } else {
            extendname = "";
        }
        switch (extendname) {
            case "rar":
                System.out.println("文件类型为rar,开始解压");
                unzip_rar5(zipFilePath, destDir);
                break;
            case "zip":
                System.out.println("文件类型为zip,开始解压");
                unzip_zip(zipFilePath, destDir);
                break;
            case "7z":
                System.out.println("文件类型为7z,开始解压");
                unzip_7z(zipFilePath, destDir);
                break;
            case "":
                System.out.println("文件拓展名不存在,流程中断");
                break;
            default:
                System.out.println("文件格式暂不支持");
                break;
        }
        System.out.println(extendname);
    }
}