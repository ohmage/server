package org.ohmage.mongodb.bin;

import java.util.List;

import org.ohmage.bin.MediaBin;
import org.ohmage.domain.survey.Media;

import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * <p>
 * The MongoDB implementation of the database-backed media repository.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoMediaBin extends MediaBin {
    /**
     * The name of the collection that contains all of the survey responses.
     */
    public static final String COLLECTION_NAME = "media_bin";

    /**
     * A connection to the container for the media within survey responses.
     */
    private final GridFS mediaConnection;

    /**
     * Default constructor.
     */
    protected MongoMediaBin() {
        // Connect to the container for the media for survey responses.
        mediaConnection =
            new GridFS(
                MongoBinController.getInstance().getDb(),
                COLLECTION_NAME);
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.MediaBin#addMedia(org.ohmage.domain.survey.Media)
     */
    @Override
    public void addMedia(final Media media) throws IllegalArgumentException {
        // Create the file.
        GridFSInputFile file =
            mediaConnection.createFile(media.getStream(), media.getId());
        file.setContentType(media.getContentType());

        // Save the file.
        try {
            file.save();
        }
        catch(MongoException e) {
            throw
                new IllegalArgumentException(
                    "Could not save the media file: " + media.getId(),
                    e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.MediaBin#getMedia(java.lang.String)
     */
    @Override
    public Media getMedia(
        final String mediaId)
        throws IllegalArgumentException {

        // Get all of the files with the given filename.
        List<GridFSDBFile> files = mediaConnection.find(mediaId);

        // If no files were found, return null.
        if(files.size() == 0) {
            return null;
        }
        // If multiple files were found, that is a violation of the system.
        if(files.size() > 1) {
            throw
                new IllegalStateException(
                    "Multiple files have the same filename: " + mediaId);
        }

        // Get the file.
        GridFSDBFile file = files.get(0);

        // Create and return the Media object.
        return
            new Media(
                mediaId,
                file.getInputStream(),
                file.getLength(),
                file.getContentType());
    }
}