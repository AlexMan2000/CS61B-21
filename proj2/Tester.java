import java.io.File;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

public class Tester {
    public static void main(String[] args) throws ParseException {
        File file1 = new File("./tempfiles/temp2.txt");
        File file2 = new File("./tempfiles/temp1.txt");
        System.out.println(file1.getName());
    }

}
