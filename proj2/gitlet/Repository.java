package gitlet;

import java.io.File;
import java.io.IOException;
import java.sql.Array;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author AlexMan
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /* TODO: fill in the rest of this class. */
//    public static final File STAGING_DIR = join(GITLET_DIR, "staging");
//    public static final File ADDITION_DIR = join(STAGING_DIR, "addition");
//    public static final File REMOVAL_DIR = join(STAGING_DIR, "removal");

    // Good way of differentiating the blob and commit hash IDs
    public static final File COMMIT_DIR = join(GITLET_DIR, "commits");
    public static final File BLOB_DIR = join(GITLET_DIR, "blobs");
    public static final File HEADS_DIR = join(GITLET_DIR, "refs");
    public static final File HEAD = join(GITLET_DIR, "HEAD.txt");
    public static final File LOCAL_HEADS = join(HEADS_DIR, "heads");
    public static final File REMOTE_HEADS = join(HEADS_DIR, "remotes");

    /**
     * Create the .gitlet folder for version control, for init command
     */
    public static void setUpPersistence () throws GitletException {
        // 1. Create a new folder called ".gitlet"
        if(!GITLET_DIR.exists()) {
            // Create ".gitlet" folder, the outmost one
            GITLET_DIR.mkdir();
            // Create a folder for commit objects and blob objects
            COMMIT_DIR.mkdir();
            BLOB_DIR.mkdir();
            // Create a folder for head pointers(Sha1 ID of an arbitrary commit)
            HEADS_DIR.mkdir();
            LOCAL_HEADS.mkdir();
            REMOTE_HEADS.mkdir();
        } else {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
        }
    }

    public static void init() {
        // 1. Initialize the file structures, .gitlet , .gitlet/objects, .gitlet/refs
        setUpPersistence();
        // 2. Initialize initial commit, with no parents(Node) and save to the file system
        Commit initial = new Commit("initial commit", new ArrayList<String>());
        initial.saveCommit();
        // 3. Create a HEAD pointer, pointing to the current commit
        try{
            String UID = initial.getUID();
            HEAD.createNewFile();
            writeContents(HEAD, UID);
            // 4. Create a master branch, which is a pointer to the initial commit
            File masterPath = join(LOCAL_HEADS, "master.txt");
            masterPath.createNewFile();
            writeContents(masterPath, UID);
        }catch (Exception e) {
            e.printStackTrace();
        }
        // 4. Initialize an empty stage object
        Stage stage = new Stage();
        stage.saveStage();
    }

    /**
     * Add command
     * @param filename filename, not a path, i.e f.txt
     */
    public static void add(String filename) {
        // 1. See if the file exists in the CWD
        File filePointer = join(CWD, filename);
        if (filePointer.exists()) {
            // 1.1 See if the file content is the same as in the current commit.
            // Get the current commit(active branch)
            Commit currentCommit = getCommit();
            // Get the blobID
            Map<String, String> fileNameToBlob = currentCommit.getPathToBlob();
            if (fileNameToBlob.containsKey(filename)) {
                // If the file exists in the recent commit, compare the content
                String blobID = fileNameToBlob.get(filename);
                // Read the Blob Object from File System
                Blob blob = Blob.fromFile(blobID);
                File blobFilePointer = blob.getFileContent();
                // Compare the content of the file in the CWD with that in the commit pointed by the active point
                // If the content are the same, do not add to the staging area and  do not create the blob
                if (!Arrays.equals(readContents(blobFilePointer),readContents(filePointer))) {
                    // If not the same, create a blob and save it under the blobs folder
                    createBlobAndSave(filePointer); // Save to the blob folder
                }
            } else {
                // If the file does not exist in the recent commit, create a blob and save it under the blobs folder
                createBlobAndSave(filePointer);
            }
        } else {
            System.out.println("File does not exist.");
        }
    }


    /**
     * Remove the tracked files from current directory
     * @param filename
     */
    public static void rm(String filename) {
        // Get the staging area
        Stage stage = getStage();
        // Get the current commit
        Commit commit = getCommit();
        File cwdFilePointer = join(CWD, filename);
        // See if the file is staged for addition
        Map<String, String> addingStagePathToBlob = stage.getPathToBlobIDAddition();
        if(addingStagePathToBlob.containsKey(filename)) {
            // If staged for addition, just delete it from the addition map
            addingStagePathToBlob.remove(filename);
        } else {
            // See if the file is tracked in the current commit
            if (commit.getPathToBlob().containsKey(filename)) {
                // See if the file to be removed actually exists
                File fileToBeDeleted = join(CWD, filename);
                if (fileToBeDeleted.exists()) {
                    // Delete the file in the CWD
                    fileToBeDeleted.delete();
                }
                // Delete it from the addition map and add it to the removal map
                Map<String, String> removalStagePathToBlob = stage.getPathToBlobIDRemoval();
                String fileBlobUID = Blob.fromFile(addingStagePathToBlob.get(filename)).getUID();
                addingStagePathToBlob.remove(filename);
                removalStagePathToBlob.put(filename, fileBlobUID);
                cwdFilePointer.delete();
            } else {
                // The file is neither in the staging area nor in the commit
                // i.e. the file is untracked by git
                System.out.println("No reason to remove the file.");
            }
        }


    }
    public static void commit(String message) {
        if (message == null) {
            System.out.println("Please enter a commit message.");
            return;
        }
        // 1. Get the current commit(Pointed by HEAD)
        Commit currentCommit = getCommit();
        // 2. Initialize a new Commit, pointing the parentID to the currentCommit
        List<String> parentIDs = new ArrayList<>();
        parentIDs.add(currentCommit.getUID());
        Commit newCommit = new Commit(message, parentIDs);
        // 3. Copy the fileMapping from its parent
        newCommit.copyFromCommit(currentCommit);
        // 4. Update the file mapping by comparing with the current staging
        Stage currentStage = Stage.fromFile();
        // 4.0  If no file has been staged for addition, then abort
        if (currentStage.getPathToBlobIDAddition().isEmpty() && currentStage.getPathToBlobIDRemoval().isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        Map<String, String> fileMappingToBeModified = newCommit.getPathToBlob();
        // 4.1 Add all the file-mapping that are in the addingStage(files that are staged for commit)
        fileMappingToBeModified.putAll(currentStage.getPathToBlobIDAddition());
        // 4.2 Untrack all the file-mapping that are in the removalStage
        for(Map.Entry<String, String> entry: currentStage.getPathToBlobIDRemoval().entrySet()){
            fileMappingToBeModified.remove(entry.getKey());
        }
        // 4.3 Clear the staging area and save it back to the file system
        currentStage.clearFileMapping();
        currentStage.saveStage();
        // 4.4 Set the modified fileMapping to the newCommit
        newCommit.setPathToBlob(fileMappingToBeModified);
        // 5. Set the UID of the new commit
        newCommit.setUID(newCommit.generateID());
        // 6. Serialize and save the commit object to the disk
        newCommit.saveCommit();
        // 7. Move the head pointer of the current branch and HEAD
        writeContents(HEAD, newCommit.getUID());
        File masterPath = join(LOCAL_HEADS, "master.txt");
        writeContents(masterPath, newCommit.getUID());
    }

    public static void log() {
        StringBuilder sb = new StringBuilder();
        Commit curr = getCommit();
        while (curr.getParentID().size() != 0) {
            sb.append("==="+"\r\n");
            sb.append(curr);
            sb.append("\r\n\r\n");
            System.out.println(curr.getParentID());
            curr = getCommit(curr.getParentID().get(0));
        }
        sb.append("==="+"\r\n");
        sb.append(curr);
        sb.append("\r\n\r\n");
        System.out.println(sb);
    }


    public static void globalLog() {
        StringBuilder sb = new StringBuilder();
        Commit curr;
        for (String filename: plainFilenamesIn(COMMIT_DIR)) {
            curr = getCommit(filename);
            sb.append("==="+"\r\n");
            sb.append(curr);
            sb.append("\r\n\r\n");
        }
        System.out.println(sb);
    }


    public static void find(String message) {
        StringBuilder sb = new StringBuilder();
        boolean flag = false;
        Commit curr;
        for (String filename: plainFilenamesIn(COMMIT_DIR)) {
            curr = getCommit(filename);
            if (message.equals(curr.getMessage())) {
                sb.append(curr.getUID());
                System.out.println(sb);
                return;
            }
        }
        System.out.println("Found no commit with that message.");
    }


    public static void status() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Branches ==="+"\r\n");
        String currentBranchID = readContentsAsString(HEAD);
        for (String filename: plainFilenamesIn(LOCAL_HEADS)) {
            String branchID = readContentsAsString(join(LOCAL_HEADS,filename));
            if (currentBranchID.equals(branchID)) {
                sb.append("*" + filename + "\r\n"); // i,e. master.txt, other-branch.txt
            } else {
                sb.append(filename + "\r\n"); // i,e. master.txt, other-branch.txt
            }
        };
        sb.append("\r\n");
        sb.append("=== Staged Files ==="+"\r\n");
        Stage stage = Stage.fromFile();
        Map<String,String> stagedAddition = stage.getPathToBlobIDAddition();
        for (String filename: stagedAddition.keySet()) {
            sb.append(filename + "\r\n");
        }
        sb.append("\r\n");
        sb.append("=== Removed Files ==="+"\r\n");
        Map<String,String> stagedRemoval = stage.getPathToBlobIDRemoval();
        for (String filename: stagedRemoval.keySet()) {
            sb.append(filename + "\r\n");
        }
        sb.append("\r\n");
        sb.append("=== Modifications Not Staged For Commit ==="+"\r\n");
//        for (String file: modificationsNotStaged) {
//            sb.append(file + "\r\n");
//        }
        sb.append("\r\n");
        sb.append("=== Untracked Files ==="+"\r\n");
//        for (String file: untracked) {
//            sb.append(file + "\r\n");
//        }
        System.out.println(sb);
//        return sb.toString();
    }



    public static void checkoutFile(String filename) {

    }

    public static void checkoutFile(String commitID, String filename) {

    }

    public static void checkoutBranch(String branchName) {

    }

    /**
     * Create a new branch
     * @param name
     */
    public static void branch(String name) {
        for(String filename: plainFilenamesIn(LOCAL_HEADS)) {
            if ((name + ".txt").equals(filename)) {
                System.out.println("A branch with that name already exists.");
                return;
            }
        }
        String currentCommitID = getCommit().getUID();
        File branchPath = join(LOCAL_HEADS, name + ".txt");
        try {
            branchPath.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        writeContents(branchPath, currentCommitID);
    }


    public static void rmBranch(String name) {
        for (String filename: plainFilenamesIn(LOCAL_HEADS)) {
            String targetFileName = name + ".txt";
            File targetFilePointer = join(LOCAL_HEADS, name + ".txt");
            String headPointerUID = getCommit().getUID();
            if (targetFileName.equals(filename)) {
                if (!readContentsAsString(targetFilePointer).equals(headPointerUID)) {
                    targetFilePointer.delete();
                } else {
                    System.out.println("Cannot remove the current branch.");
                }
                return;
            }
        }
        System.out.println("A branch with that name does not exist.");
    }


    public static void reset(String commitID) {

    }


    public static void merge(String branchName) {

    }






    private static Commit getCommit() {
        return Commit.fromFile(readContentsAsString(HEAD));
    }

    private static Commit getCommit(String UID) {
        return Commit.fromFile(UID);
    }


    private static Stage getStage() {
        return Stage.fromFile();
    }


    /**
     * This function creates a blob at the specified filepath
     * @param file filepath
     */
    private static void createBlobAndSave(File filePointer) {
        Blob newBlob = new Blob(filePointer);
        newBlob.saveBlob();
        addStaging(filePointer, newBlob.getUID());
    }

    private static void addStaging(File filePointer, String BID) {
        Stage stage = getStage();
        stage.getPathToBlobIDAddition().put(filePointer.getName(), BID);
        stage.saveStage();
    }

    private static void rmStaging() {
//        Stage stage = Stage.fromFile();
//        stage.getPathToBlobIDAddition()
    }


}
