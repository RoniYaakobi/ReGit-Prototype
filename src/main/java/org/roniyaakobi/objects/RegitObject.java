package org.roniyaakobi.objects;

import org.roniyaakobi.utils.FileWritingUtil;

import java.io.IOException;

public abstract class RegitObject implements Compressable, Hashable {

    private RegitObjects.ObjectId objectId = null;

    /**
     * Get the id of the object
     * @return The objectID
     */
    public RegitObjects.ObjectId getId(){
        if (objectId == null){
            objectId = hash();
        }

        return objectId;
    }

    /**
     * Store the object
     * @throws IOException
     */
    public void store() throws IOException {
        FileWritingUtil.writeFile(getId().getPath(), compress());
    }
}
