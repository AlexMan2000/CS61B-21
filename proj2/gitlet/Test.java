package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gitlet.Utils.*;

public class Test {
    public static void main(String[] args) {
        String temp = "23232bds: sfs/fsd/s";
        Pattern pattern = Pattern.compile(".*/(\\w+)");
        Matcher m = pattern.matcher(temp);
        if (m.matches()) {
            System.out.println(m.group(1));
        }
    }
}
