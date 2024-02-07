package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    // Not serialized
    private static final File SAVE_DIR = Repository.OBJECT_DIR;

    // Serialized
    private String UID;  // Unique Identifier
    private File filePointer; // A pointer to the stored file on the disk
    private String fileName;  // The file that blob object is pointing to

    private static final String TYPE = "blob";
    // The same Blob object means the same fileName and the same content from filePointer

    public Blob(File filePointer) {
        this.filePointer = filePointer;
        this.fileName = filePointer.getName();
        this.UID = this.generateID();
    }

    // no static here since we want to save a particular blob object
    public void saveBlob() {
        Utils.writeObject(Utils.join(SAVE_DIR, this.UID), this);
    }


    // static here since we don't have the Blob object in advance, so we need to
    // invoke the method from the Blob Class, requiring the method to be static
    public static Blob fromFile(String UID) {
        return Utils.readObject(Utils.join(SAVE_DIR, UID), Blob.class);
    }

    public File getFileContent() {
        return filePointer;
    }

    public byte[] getFileBytes() {
        return Utils.readContents(filePointer);
    }

    public void setFileContent(File fileContent) {
        this.filePointer = fileContent;
    }

    // Same Blob means the same content.
    public String generateID() {
        return Utils.sha1(Utils.readContents(filePointer),fileName,TYPE);
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return null;
    }
}
