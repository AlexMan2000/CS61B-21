package gitlet;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        String temp = "23232bds: sfs/fsd/s";
        if (temp.contains(":")) {
            final String CWD =
            String path = temp.split(":")[1].substring(1);
            System.out.println(path);
        }
    }
}
