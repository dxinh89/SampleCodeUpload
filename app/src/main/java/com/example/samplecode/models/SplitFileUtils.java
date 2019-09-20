package com.example.samplecode.models;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class SplitFileUtils {
    Context mContext;

    public SplitFileUtils() {
    }

    public SplitFileUtils(Context context) {
        mContext = context;
    }

    public List<String> splitFile(String filePath) throws Exception {

        List<String> partFilePaths = new ArrayList<>();
        final int _5MB = 50000; // 5 * 1024 * 1024; //mb kb b

        File file = new File(filePath);
        int totalSize = Integer.parseInt(String.valueOf(file.length())); // - unit: bytes
        if (totalSize < _5MB) {
            partFilePaths.add(filePath);
            return partFilePaths;
        }

        if (file.exists()) {
            Log.i("ON_BROWSE", "File exists:" + totalSize);
        } else
            Log.i("ON_BROWSE", "File isn't exists!");

        InputStream inputStream = new FileInputStream(file);//getResources().openRawResource(R.raw.test);
        OutputStream out = null;

        String dir = mContext.getFilesDir().toString();
        Log.i("ON_BROWSE", "real==" + dir); // /storage/emulated/0/DCIM/Camera/compressed.zip

        byte[] buf = new byte[1024];
        int len;
        int count = 0;
        int total = 0;
        while ((len = inputStream.read(buf, 0, Math.min(buf.length, _5MB - total))) > 0) {
            if (out == null) {

                File folder = new File(dir + File.separator + "split");
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdirs();
                }
                if (success) {
                    String itemPath = dir + "/split/file_" + String.format(Locale.US, "%03d", count); // /data/user/0/com.example.samplecode/files
                    out = new FileOutputStream(new File(itemPath));

                    partFilePaths.add(itemPath);
                }
            }
            out.write(buf, 0, len);
            total += len;
            if (total >= _5MB) {
                out.flush();
                out.close();
                out = null;
                total = 0;
                count++;
            }
        }
        if (out != null) {
            out.close();
        }
        inputStream.close();

        return partFilePaths;
    }


//    private static final String dir = ""; //"D:/tmp/";
//    private static final String suffix = ".splitPart";
//
//    /**
//     * Split a file into multiples files.
//     *
//     * @param fileName   Name of file to be split.
//     * @param mBperSplit maximum number of MB per file.
//     * @throws IOException
//     */
//    public static List<Path> splitFile(final String fileName, final int mBperSplit) throws IOException {
//
//        if (mBperSplit <= 0) {
//            throw new IllegalArgumentException("mBperSplit must be more than zero");
//        }
//
//        int index = fileName.lastIndexOf(File.separator);
//        String baseFileName = fileName.substring(index + 1);
//
//        List<Path> partFiles = new ArrayList<>();
//        final long sourceSize = Files.size(Paths.get(fileName));
//        final long bytesPerSplit = 1024L * 1024L * mBperSplit;
//        final long numSplits = sourceSize / bytesPerSplit;
//        final long remainingBytes = sourceSize % bytesPerSplit;
//        int position = 0;
//
//        try (RandomAccessFile sourceFile = new RandomAccessFile(fileName, "r");
//             FileChannel sourceChannel = sourceFile.getChannel()) {
//
//            int preIndex = 0;
//
//            for (; position < numSplits; position++, preIndex++) {
//                // write multipart files.
//                writePartToFile(preIndex, bytesPerSplit, position * bytesPerSplit, sourceChannel, partFiles);
//            }
//
//            if (remainingBytes > 0) {
//                writePartToFile(preIndex, remainingBytes, position * bytesPerSplit, sourceChannel, partFiles);
//            }
//        }
//        return partFiles;
//    }
//
//    private static void writePartToFile(int preIndex, long byteSize, long position, FileChannel sourceChannel,
//                                        List<Path> partFiles) throws IOException {
//        Path fileName = Paths.get(dir + String.format("%05d", preIndex) + "_" + UUID.randomUUID() + suffix);
//        try (RandomAccessFile toFile = new RandomAccessFile(fileName.toFile(), "rw");
//             FileChannel toChannel = toFile.getChannel()) {
//            sourceChannel.position(position);
//            toChannel.transferFrom(sourceChannel, 0, byteSize);
//        }
//        partFiles.add(fileName);
//    }

}
