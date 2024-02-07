import java.io.File;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

public class Tester {
    public static void main(String[] args) throws ParseException {
        String temp = "23232bds: sfs/fsd/s";
        if (temp.contains(":")) {
            String path = temp.split(":")[1].substring(1);
            System.out.println(path);
        }
    }

}
