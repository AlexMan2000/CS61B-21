package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static gitlet.Utils.*;

public class Test {
    public static void main(String[] args) {
        String temp = "23232bds: sfs/fsd/s";
        if (temp.contains(":")) {
            File CWD = new File(System.getProperty("user.dir"));
            System.out.println(CWD.getPath());
            String path = temp.split(":")[1].substring(1);
            System.out.println(join(CWD,path));
        }
    }
}
