package org.roniyaakobi.objects;

import org.roniyaakobi.Compressable;
import org.roniyaakobi.Hashable;
import org.roniyaakobi.utils.FileWritingUtil;

import java.io.IOException;

public abstract class RegitObject implements Compressable, Hashable {

    private RegitObjects.ObjectId objectId = null;

    public RegitObjects.ObjectId getId(){
        if (objectId == null){
            objectId = hash();
        }

        return objectId;
    }

    public void store() throws IOException {
        byte[] compressed = compress();
        FileWritingUtil.writeFile(getId().getPath(), compressed);
    }
}
