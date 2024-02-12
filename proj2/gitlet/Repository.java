package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.sql.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author AlexMan
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File OBJECT_DIR = join(GITLET_DIR, "objects");
    public static final File HEADS_DIR = join(GITLET_DIR, "refs");
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    public static final File LOCAL_HEADS = join(HEADS_DIR, "heads");
    public static final File REMOTE_HEADS = join(HEADS_DIR, "remotes");
    public static final File STAGING = join(GITLET_DIR, "index");

    /**
     * Create the .gitlet folder for version control, for init command
     */
    public static void setUpPersistence () throws GitletException {
        // 1. Create a new folder called ".gitlet"
        if(!GITLET_DIR.exists()) {
            // Create ".gitlet" folder, the outmost one
            GITLET_DIR.mkdir();
            // Create a folder for commit objects and blob objects
            OBJECT_DIR.mkdir();
            // Create a folder for head pointers
            HEADS_DIR.mkdir();
            // Inside the heads directory, create
            // 1. Local heads folder, which contains branch information and head pointers
            LOCAL_HEADS.mkdir();
            // 2. Remote heads folder
            REMOTE_HEADS.mkdir();
        } else {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
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
            // Write to HEAD file which branch the HEAD is pointer to, initially it is
            // pointing to the latest commit in the master branch
            // In detached head state, HEAD file stores a specific commit ID
            writeContents(HEAD, "ref: refs/heads/master");
            // 4. Create a master branch, which is a pointer to the initial commit
            File masterPath = join(LOCAL_HEADS, "master");
            masterPath.createNewFile();
            // The refs/heads/master file contains the initial commit ID
            writeContents(masterPath, UID);
        }catch (Exception e) {
            e.printStackTrace();
        }
        // 4. Initialize an empty stage object, representing staging area
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
            // Get the all the blobs managed by the current commit
            Map<String, String> fileNameToBlob = currentCommit.getPathToBlob();
            if (fileNameToBlob.containsKey(filename)) {
                // If the file exists in the recent commit, compare the content
                String blobID = fileNameToBlob.get(filename);
                // Read the Blob Object from File System
                Blob blob = Blob.fromFile(blobID);
                // Compare the content of the file in the CWD with that in the commit pointed by the active point
                // If the content are the same, do not add to the staging area and  do not create the blob
                if (!Arrays.equals(blob.getFileContent(),readContents(filePointer))) {
                    // If not the same, create a blob and save it under the blobs folder
                    createBlobAndSave(filePointer); // Save to the blob folder
                }
            } else {
                // If the file does not exist in the recent commit, create a blob and save it under the blobs folder
                createBlobAndSave(filePointer);
            }
        } else {
            System.out.println("File does not exist.");
            System.exit(0);
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
        boolean staged = false;
        boolean tracked = false;
        if(addingStagePathToBlob.containsKey(filename)) {
            // If staged for addition, just delete it from the addition map
            staged = true;
            addingStagePathToBlob.remove(filename);
        }
        // See if the file is tracked in the current commit
        if (commit.getPathToBlob().containsKey(filename)) {
            tracked = true;
            // See if the file to be removed actually exists
            File fileToBeDeleted = join(CWD, filename);
            if (fileToBeDeleted.exists()) {
                // Delete the file in the CWD
                fileToBeDeleted.delete();
            }
            // If after commit, user manually delete it from the directory
            // Delete it from the addition map and add it to the removal map
            Map<String, String> removalStagePathToBlob = stage.getPathToBlobIDRemoval();
            String fileBlobUID = Blob.fromFile(commit.getPathToBlob().get(filename)).getUID();
            addingStagePathToBlob.remove(filename);
            removalStagePathToBlob.put(filename, fileBlobUID);
            cwdFilePointer.delete();
        }
        if (!staged && !tracked) {
            // The file is neither in the staging area nor in the commit
            // i.e. the file is untracked by git
            System.out.println("No reason to remove the file.");
        }
        stage.saveStage();
    }


    /**
     * Take a snapshot of current working directory and make a commit
     * @param message The commit message specified by users
     */
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
        newCommit.setPathToBlob(fileMappingToBeModified); // Not necessary, pass by reference
        // 5. Set the UID of the new commit
        newCommit.setUID(newCommit.generateID());
        // 6. Serialize and save the commit object to the disk
        newCommit.saveCommit();
        // 7. Move the head pointer of the current branch and HEAD
        String currentBranch = parseCurrentBranchHEAD(readContentsAsString(HEAD));
        File masterPath = join(LOCAL_HEADS, currentBranch);
        writeContents(masterPath, newCommit.getUID());
    }


    /**
     * Print out all the commit information(in backward ordering)
     */
    public static void log() {
        StringBuilder sb = new StringBuilder();
        Commit curr = getCommit();
        while (curr.getParentID().size() != 0) {
            sb.append("==="+"\r\n");
            sb.append(curr);
            sb.append("\r\n\r\n");
            curr = getCommit(curr.getParentID().get(0));
        }
        sb.append("==="+"\r\n");
        sb.append(curr);
        sb.append("\r\n");
        System.out.println(sb);
    }


    /**
     * Print out all the commit information(in arbitrary ordering)
     */
    public static void globalLog() {
        StringBuilder sb = new StringBuilder();
        Commit curr;
        for (String filename: plainFilenamesIn(OBJECT_DIR)) {
            // Circumvent those non-commit object
            try {
                curr = getCommit(filename);
                curr.getType();
            } catch(Exception e) {
                continue;
            }
            sb.append("==="+"\r\n");
            sb.append(curr);
            sb.append("\r\n\r\n");
        }
        System.out.println(sb);
    }


    /**
     * Find commit by its message
     * @param message The commit message specified by the users
     */
    public static void find(String message) {
        StringBuilder sb = new StringBuilder();
        Commit curr;
        for (String filename: plainFilenamesIn(OBJECT_DIR)) {
            // Circumvent those non-commit object
            try {
                curr = getCommit(filename);
                curr.getType();
                if (curr.getMessage().equals(message)) {
                    sb.append("==="+"\r\n");
                    sb.append(curr);
                    sb.append("\r\n");
                }
            } catch(Exception e) {
                continue;
            }
        }
        System.out.println(sb);
    }


    /**
     * Print out the current status of different regions of gitlet internals.
     */
    public static void status() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Branches ==="+"\r\n");
//        String currentCommitSHA1ID = parseHEAD(readContentsAsString(HEAD));
        String currentBranchName = parseCurrentBranchHEAD(readContentsAsString(HEAD));
        for (String filename: plainFilenamesIn(LOCAL_HEADS)) {
//            String branchFrontID = readContentsAsString(join(LOCAL_HEADS,filename));
            if (currentBranchName.equals(filename)) {
                sb.append("*" + filename + "\r\n");
            } else {
                sb.append(filename + "\r\n");
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


    /**
     * Recover the file from current commit
     * @param filename the file to be recovered
     */
    public static void checkoutFile(String filename) {
        Commit headCommit = getCommit();
        String blobID = headCommit.getBlobFromFileName(filename);
        if (blobID != null) {
            Blob blob = Blob.fromFile(blobID);
            writeContents(join(CWD, filename), blob.getFileContent());
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    /**
     *
     * @param commitID
     * @param filename
     */
    public static void checkoutFile(String commitID, String filename) {
        try {
            Commit commitObj = getCommit(commitID);
            String blobID = commitObj.getBlobFromFileName(filename);
            if (blobID != null) {
                Blob blob = Blob.fromFile(blobID);
                writeContents(join(CWD, filename), blob.getFileContent());
            }
        } catch (Exception e) {
            System.out.println("No commit with that id exists.");
        }

    }


    /**
     * Switch to a different branch, note that this is different from real git
     * @param branchName
     */
    public static void checkoutBranch(String branchName) {
        File branchFilePointer = join(LOCAL_HEADS, branchName);
        // Get the current branch name
        String currentBranchName = parseCurrentBranchHEAD(readContentsAsString(HEAD));
        // Failure Case 1: Branch Name DNE
        if (!branchFilePointer.exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        // Failure Case 2: Branch Name is the current branch name
        if (currentBranchName.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }

        checkoutCommit(readContentsAsString(branchFilePointer));

//        Commit currentCommit = getCommit();
//        Commit branchHeadCommit = getCommit(readContentsAsString(branchFilePointer));
//
//        Map<String, String> currentFileNameToBlobIdMapping = currentCommit.getPathToBlob();
//        Map<String, String> branchFileNameToBlobIdMappping = branchHeadCommit.getPathToBlob();
//
//        for (String filename: branchFileNameToBlobIdMappping.keySet()) {
//            boolean inCurrent = currentFileNameToBlobIdMapping.containsKey(filename);
//            boolean inCWD = join(CWD, filename).exists();
//
//            Blob branchBlob = Blob.fromFile(branchFileNameToBlobIdMappping.get(filename));
//            // Failure Case 3: Filename is in new branch, not in current branch but in CWD
//            if (!inCurrent && inCWD) {
//                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
//                System.exit(0);
//            }
//            // Overwrite the file in the CWD
//            writeContents(join(CWD, filename), branchBlob.getFileBytes());
//        }
//
//        // Delete all the files tracked by current branch but not tracked in target branch
//        for (String filename: plainFilenamesIn(CWD)) {
//            boolean inBranch = branchFileNameToBlobIdMappping.containsKey(filename);
//            if (!inBranch) {
//                join(CWD, filename).delete();
//            }
//        }
//
//        // Clear the staging area.
//        Stage currentStage = Stage.fromFile();
//        currentStage.clearFileMapping();
//        currentStage.saveStage();

        // Write the new branch information to HEAD(write refs/heads/branch name)
        writeContents(HEAD, "ref: refs/heads/" + branchName);
    }

    /**
     * Create a new branch, with its head pointing to current commit
     * @param name
     */
    public static void branch(String name) {
        for(String filename: plainFilenamesIn(LOCAL_HEADS)) {
            if ((name).equals(filename)) {
                System.out.println("A branch with that name already exists.");
                System.exit(0);
            }
        }
        String currentCommitID = getCommit().getUID();
        File branchPath = join(LOCAL_HEADS, name);
        try {
            branchPath.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        writeContents(branchPath, currentCommitID);
    }


    public static void rmBranch(String branchName) {
        String currentBranchName = parseCurrentBranchHEAD(readContentsAsString(HEAD));
        if (currentBranchName.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        for (String nameBranch: plainFilenamesIn(LOCAL_HEADS)) {
            if (nameBranch.equals(branchName)) {
                join(LOCAL_HEADS, nameBranch).delete();
                System.exit(0);
            }
        }
        System.out.println("A branch with that name does not exist.");
        System.exit(0);
    }


    /**
     * Check out all the files tracked by the given commit
     * @param commitID
     */
    public static void reset(String commitID) {
        if (!checkCommit(commitID)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        checkCommit(commitID);
    }


    public static void merge(String branchName) {

    }





/**
 * Helper functions
 * =====================================================================
 */



    /**
     * Switch to an arbitrary commit with commitID
     * @param commitID The commit ID of an arbitrary commit.
     */
    public static void checkoutCommit(String commitID) {
        Commit currentCommit = getCommit();
        Commit targetCommit = getCommit(commitID);

        Map<String, String> currentFileNameToBlobIdMapping = currentCommit.getPathToBlob();
        Map<String, String> targetFileNameToBlobIdMappping = targetCommit.getPathToBlob();

        for (String filename: targetFileNameToBlobIdMappping.keySet()) {
            boolean inCurrent = currentFileNameToBlobIdMapping.containsKey(filename);
            boolean inCWD = join(CWD, filename).exists();

            Blob branchBlob = Blob.fromFile(targetFileNameToBlobIdMappping.get(filename));
            // Failure Case 3: Filename is in new branch, not in current branch but in CWD
            if (!inCurrent && inCWD) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
            // Overwrite the file in the CWD
            writeContents(join(CWD, filename), branchBlob.getFileBytes());
        }

        // Delete all the files tracked by current branch but not tracked in target branch
        for (String filename: plainFilenamesIn(CWD)) {
            boolean inBranch = targetFileNameToBlobIdMappping.containsKey(filename);
            if (!inBranch) {
                join(CWD, filename).delete();
            }
        }

        // Clear the staging area.
        Stage currentStage = Stage.fromFile();
        currentStage.clearFileMapping();
        currentStage.saveStage();
    }


    /**
     * Get the commit that's being pointed by HEAD
     * @return a commit object currently being pointed at
     */
    private static Commit getCommit() {
        String content = readContentsAsString(HEAD);
        return Commit.fromFile(parseHEAD(content));
    }

    /**
     * Check whether a commit object with commitID exists
     * @param commitID The commitID that pinponts a unique commit object
     * @return Whether that commit has been created
     */
    private static boolean checkCommit(String commitID) {
        for (String commitSHA1: plainFilenamesIn(OBJECT_DIR)) {
            if (commitSHA1.equals(commitID)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Parse the content of HEAD file and return whether the current HEAD is in detached state
     * @param content The content of the HEAD file
     * @return whether the current HEAD is in detached state
     */
    private static boolean parseHEADDetached(String content) {
        return content.contains(":") ? false: true;
    }

    /**
     * Return the current branch name
     * @param content The content of the HEAD file
     * @return
     */
    private static String parseCurrentBranchHEAD(String content) {
        if (!parseHEADDetached(content)) {
            Pattern pattern = Pattern.compile(".*/(\\w+)");
            Matcher m = pattern.matcher(content);
            if (m.matches()) {
                return m.group(1);
            } else {
                return null;
            }
        } else {
            // 2. It is a SHA1 ID, directly return, DETACHED HEAD STATE
            return content;
        }
    }

    /**
     * Return the SHA1 ID of a commit given the content in the HEAD file
     * @param content The content of HEAD file
     * @return SHA1 ID of a commit object
     */
    private static String parseHEAD(String content) {
        // We could use the property that SHA1 ID only contains 0-9a-f
        if (!parseHEADDetached(content)) {
            // 1. It is not SHA1ID, but ref: refs/heads/current_branch
            String[] splits = content.split(":");

            File filepath = join(GITLET_DIR, splits[1].substring(1));
            return readContentsAsString(filepath);
        } else {
            // 2. It is a SHA1 ID, directly return
            return content;
        }
    }

    /**
     * Overloaded
     * @param UID
     * @return
     */
    private static Commit getCommit(String UID) {
        return Commit.fromFile(UID);
    }


    /**
     * Get the current state of staging area
     * @return
     */
    private static Stage getStage() {
        return Stage.fromFile();
    }


    /**
     * This function creates a blob at the specified filepath
     * @param filePointer filepath
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
}
