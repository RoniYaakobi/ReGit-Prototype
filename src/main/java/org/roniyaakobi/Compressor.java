package org.roniyaakobi;

import org.roniyaakobi.objects.Blob;
import org.roniyaakobi.objects.RegitObjects;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Compressor {
    public static byte[] compress(Blob blob) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             DeflaterOutputStream deflater =
                     new DeflaterOutputStream(output)) {


            deflater.write(blob.buildHeaderBytes());

            blob.streamFileContents(
                    streamData -> {
                        try {
                            deflater.write(streamData.bytes(), 0, streamData.length());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

            deflater.finish();

            return output.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static byte[] decompress(RegitObjects.ObjectId id) throws IOException {
        try (InputStream file = Files.newInputStream(id.getPath());
             InflaterInputStream inflater = new InflaterInputStream(file);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            inflater.transferTo(output);
            return output.toByteArray();
        }
    }
}
