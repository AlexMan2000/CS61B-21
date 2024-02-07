package gitlet;

import java.io.File;
import java.io.Serializable;

public abstract class GitObject implements Serializable {
    private static final File SAVE_DIR = Repository.OBJECT_DIR;
    private String UID;  // Unique Identifier
    private String TYPE;


    /**
     * Serialize current object to the specified savePath
     */
    public void saveGitObject() {
        Utils.writeObject(Utils.join(SAVE_DIR, this.UID), this);
    }

    /**
     * Deserialize the object given its SHA1ID
     * @param UID SHA1 ID of the object
     * @return An object with SHA1 ID
     */
    public static GitObject readGitObject(String UID) {
        return Utils.readObject(Utils.join(SAVE_DIR, UID), GitObject.class);
    }


    /**
     * Generate the SHA1 ID for current commit object
     * @return The generated SHA1 ID of current commit object
     */
    public abstract String generateID();
}
