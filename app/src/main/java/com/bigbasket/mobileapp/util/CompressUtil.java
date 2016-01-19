package com.bigbasket.mobileapp.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class CompressUtil {
    private CompressUtil() {
    }

    public static byte[] gzipCompress(String in) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(in.length());
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write(in.getBytes());
        gzipOutputStream.close();
        byte[] compressed = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return compressed;
    }

    public static String gzipDecompress(byte[] compress) throws IOException {
        final int BUFFER_LEN = 32;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compress);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream, BUFFER_LEN);
        StringBuilder stringBuilder = new StringBuilder();
        byte[] holderArr = new byte[BUFFER_LEN];
        int bytesRead;
        while ((bytesRead = gzipInputStream.read(holderArr)) != -1) {
            stringBuilder.append(new String(holderArr, 0, bytesRead));
        }
        gzipInputStream.close();
        byteArrayInputStream.close();
        return stringBuilder.toString();
    }
}
