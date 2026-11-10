package org.roniyaakobi.objects;

import java.io.IOException;

public abstract class RegitObject implements Hashable, Archivable {

    private RegitObjects.ObjectId objectId = null;

    /**
     * Get the id of the object
     * @return The objectID
     */
    @Override
    public RegitObjects.ObjectId getObjectId(){
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
        archive();
    }

}
