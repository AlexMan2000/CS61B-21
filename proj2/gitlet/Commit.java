package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {

    private static final File savePath = Repository.COMMIT_DIR;

    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    // "Wed Dec 31 16:00:00 1969 -0800" Time when commit is created
    private String timestamp;
    // Unique Identifier
    private String UID;
    // Maps from filepath to blobID
    private Map<String, String> pathToBlob = new HashMap<>();
    // All parents' ID of current Commit Object, could be more than one when merging happens
    private List<String> parentID;

    /** The message of this Commit, specified by the user */
    private String message;


    /* TODO: fill in the rest of this class. */

    public Commit (String message, List<String> parentID) {
        this.message = message;
        this.parentID = parentID;
        // 如果没有Parent
        if (this.parentID.size() == 0) {
            // EEE MMM d HH:mm:ss yyyy Z format, for gradescope
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = new Date(0L);
            this.timestamp = sdf.format(date);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = new Date();
            this.timestamp = sdf.format(date);
        }
        this.UID = this.generateID();
    }

    public void saveCommit() {
        Utils.writeObject(Utils.join(savePath, this.UID), this);
    }


    public static Commit fromFile(String UID) {
        return Utils.readObject(Utils.join(savePath, UID), Commit.class);
    }

    // See the design spec, same Commit means the same metadata, same fileToBlobMapping,
    // same parentID
    public String generateID() {
        return Utils.sha1(timestamp, pathToBlob.toString(), parentID.toString());
    }

    /**
     * Copy fileMapping from the given commit
     * @param commit
     */
    public void copyFromCommit(Commit commit) {
        this.pathToBlob = commit.getPathToBlob();
    }


    /**
     * Copy fileMapping from which parent, could be two
     * @param index
     */
    public void copyFromParent(int index) {
        this.pathToBlob = Commit.fromFile(this.parentID.get(index)).getPathToBlob();
    }




    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public Map<String, String> getPathToBlob() {
        return pathToBlob;
    }

    public void setPathToBlob(Map<String, String> pathToBlob) {
        this.pathToBlob = pathToBlob;
    }

    public List<String> getParentID() {
        return parentID;
    }

    public void setParentID(List<String> parentID) {
        this.parentID = parentID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("commit " + this.UID + "\r\n");
        if (this.parentID.size() > 1) {
            sb.append("Merge: " + this.parentID.get(0)+ " " + this.parentID.get(1));
            sb.append("\r\n");
        }
        sb.append("Date: " + this.timestamp + "\r\n");
        sb.append(message);
        return sb.toString();
    }

//    public static void main(String[] args) {
//        Commit c = new Commit("initial commit", null);
//        System.out.println(c);
//    }
}
