package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Utils.join;

public class Stage implements Serializable {

    private static final File savePath = join(Repository.GITLET_DIR, "index");

    private Map<String, String> pathToBlobIDAddition = new HashMap<>();
    private Map<String, String> pathToBlobIDRemoval = new HashMap<>();

    public Map<String, String> getPathToBlobIDAddition() {
        return pathToBlobIDAddition;
    }

    public void saveStage() {
        Utils.writeObject(Utils.join(savePath), this);
    }

    public static Stage fromFile() {
        return Utils.readObject(Utils.join(savePath), Stage.class);
    }

    public void setPathToBlobIDAddition(Map<String, String> pathToBlobIDAddition) {
        this.pathToBlobIDAddition = pathToBlobIDAddition;
    }

    public Map<String, String> getPathToBlobIDRemoval() {
        return pathToBlobIDRemoval;
    }

    public void setPathToBlobIDRemoval(Map<String, String> pathToBlobIDRemoval) {
        this.pathToBlobIDRemoval = pathToBlobIDRemoval;
    }

    public void clearFileMapping() {
        this.pathToBlobIDAddition.clear();
        this.pathToBlobIDRemoval.clear();
    }
}
