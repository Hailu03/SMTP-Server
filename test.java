import java.text.SimpleDateFormat;
import java.util.Date;

public class test {
    public static void main(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss");
        System.out.println("Current Date: " + new Date());
        System.out.println(sdf.format(new Date()));
    }
}
