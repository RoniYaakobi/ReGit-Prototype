package org.roniyaakobi;

import org.roniyaakobi.objects.Blob;
import org.roniyaakobi.objects.RegitObject;
import org.roniyaakobi.objects.RegitObjects;
import org.roniyaakobi.objects.Tree;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public interface Compressable {
    byte[] compress();

     static byte[] decompress(RegitObjects.ObjectId id) throws IOException {
        try (InputStream file = Files.newInputStream(id.getPath());
             InflaterInputStream inflater = new InflaterInputStream(file);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            inflater.transferTo(output);
            return output.toByteArray();
        }
    }
}
