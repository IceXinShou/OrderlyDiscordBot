package main.java.YoutubeDownloader.convert;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class BufferedWritableFileByteChannel implements WritableByteChannel {
    //    private static final int BUFFER_CAPACITY = 1000000;
    private static final int BUFFER_CAPACITY = 10485760;

    private boolean isOpen = true;
    private final FileOutputStream outputStream;
    private final ByteBuffer byteBuffer;
    private final byte[] rawBuffer = new byte[BUFFER_CAPACITY];

    BufferedWritableFileByteChannel(FileOutputStream outputStream) {
        this.outputStream = outputStream;
        this.byteBuffer = ByteBuffer.wrap(rawBuffer);
    }

    @Override
    public int write(ByteBuffer inputBuffer) {
        int inputBytes = inputBuffer.remaining();

        if (inputBytes > byteBuffer.remaining()) {
            dumpToFile();
            byteBuffer.clear();
            System.out.print(".");
            if (inputBytes > byteBuffer.remaining()) {
                System.out.println("Size ok song size is not ok");
                throw new BufferOverflowException();
            }
        }

        byteBuffer.put(inputBuffer);

        return inputBytes;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public void close() {
        dumpToFile();
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        isOpen = false;
    }

    private void dumpToFile() {
        try {
            outputStream.write(rawBuffer, 0, byteBuffer.position());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
