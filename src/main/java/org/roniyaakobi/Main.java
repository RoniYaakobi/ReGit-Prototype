package org.roniyaakobi;

import org.roniyaakobi.objects.Blob;
import org.roniyaakobi.objects.ObjectStore;
import org.roniyaakobi.objects.RegitObjects;
import org.roniyaakobi.objects.Tree;
import org.roniyaakobi.utils.Visualizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1){
            throw new RuntimeException("Regit needs a file!");
        }

        Blob blob = Blob.from(Path.of(args[0]));
        RegitObjects.ObjectId id  = Hasher.hash(blob);

        byte[] compressed = Compressor.compress(blob);

        ObjectStore.StoreBlob(id, compressed);

        Visualizer.printBytesAsString(Compressor.decompress(id));



    }
}