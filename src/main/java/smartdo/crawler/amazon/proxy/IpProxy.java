package smartdo.crawler.amazon.proxy;

import smartdo.crawler.amazon.FileUtils;
import us.codecraft.webmagic.proxy.Proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IpProxy {

    static List<String> ips = new ArrayList<>();
    static int i = 0;

    static {
        ips = FileUtils.readFileByLines("C:\\智干工作\\ipproxy\\ips.txt");
    }
    public static Map<String,String> getIp(){
        if(i == ips.size())
            i = 0;
        String[] array = ips.get(i++).split(":");
        Map<String,String> map = new HashMap<>();
        map.put("ip",array[0]);
        map.put("port",array[1]);
        return map;
    }

    public static Proxy[] getIpProxy(){
        Proxy[] list = new Proxy[ips.size()];
        for(String str:ips) {
            String[] array = str.split(":");
            list[i++] = new Proxy(array[0], Integer.parseInt(array[1]));
        }
        return list;
    }
}
