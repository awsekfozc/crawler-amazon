package smartdo.crawler.amazon;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fate
 */
public class Test {

    public static void main(String[] args) {
        String str= "/Ninja-Professional-Blender-Nutri-BL660/dp/B00939FV8K/ref=zg_bs_home-garden_20/145-0789631-8639348?_encoding=UTF8&psc=1&refRID=JG8HYYHTDJ05N2J5P1YY";
        String reg = "\\w+/ref=zg_bs_\\w+_\\w+\\w+";
        boolean isMatch = Pattern.matches(reg, str);
        System.out.println(isMatch);

        String big_type_rank = "https://www.amazon.com/Aromatherapy-Essential-Humidifier-Ultrasonic-Diffusers/dp/B06ZZLN6DP/ref=zg_bs_11056591_100?_encoding=UTF8&psc=1&refRID=SZPF15KEZHMSNRP9BNZ&0.123123123&0.33213123";
        String[] arry = big_type_rank.split("&");
        System.out.println(arry.length);
    }
}
