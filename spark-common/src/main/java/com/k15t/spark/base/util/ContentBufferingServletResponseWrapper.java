package com.k15t.spark.base.util;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;


public class ContentBufferingServletResponseWrapper extends HttpServletResponseWrapper {

    private final static Charset UTF8 = Charset.forName("UTF-8");

    private final ContentBufferingServletOutputStream out = new ContentBufferingServletOutputStream();
    private PrintWriter printWriter;


    public ContentBufferingServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }


    @Override
    public PrintWriter getWriter() throws IOException {
        if (printWriter == null) {
            printWriter = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
        }
        return printWriter;
    }


    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return out;
    }


    public byte[] toByteArray() {
        return out.toByteArray();
    }


    public String toString() {
        return new String(toByteArray(), UTF8);
    }


    public static class ContentBufferingServletOutputStream extends ServletOutputStream {

        private final ByteArrayOutputStream out = new ByteArrayOutputStream();

        @Override
        public void write(int b) throws IOException {
            out.write(b);
        }

        public byte[] toByteArray() {
            return out.toByteArray();
        }

    }

}
