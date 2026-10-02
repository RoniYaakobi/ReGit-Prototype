package org.roniyaakobi.objects;

import org.roniyaakobi.utils.FileWritingUtil;

import java.io.IOException;

public class ObjectStore {
    public static void StoreBlob(RegitObjects.ObjectId objectId, byte[] compressed) throws IOException {

        FileWritingUtil.writeFile(objectId.getPath(), compressed);
    }
}
