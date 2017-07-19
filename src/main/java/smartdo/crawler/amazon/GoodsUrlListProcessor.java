package smartdo.crawler.amazon;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;
import us.codecraft.webmagic.selector.Html;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by fate
 */
public class GoodsUrlListProcessor implements PageProcessor {

    static String LIST_REG = "https://www.amazon.com/Best-Sellers-\\w+";
    static List<String> startUrl = new ArrayList<>();

    static {
//    startUrl.add("https://www.amazon.com/Best-Sellers-Beauty-Skin-Care-Products/zgbs/beauty/11060451/ref=zg_bs_nav_bt_1_bt/134-3759332-6465817?_encoding=UTF8&pg=1&ajax=1");
        startUrl.add("https://www.amazon.com/Best-Sellers-Beauty/zgbs/beauty/ref=zg_bs_nav_0");
        startUrl.add("https://www.amazon.com/Best-Sellers-Health-Personal-Care/zgbs/hpc/ref=zg_bs_nav_0");
        startUrl.add("https://www.amazon.com/Best-Sellers-Home-Kitchen/zgbs/home-garden/ref=zg_bs_nav_0");
        startUrl.add("https://www.amazon.com/Best-Sellers-Kitchen-Dining/zgbs/kitchen/ref=zg_bs_nav_0");
        startUrl.add("https://www.amazon.com/Best-Sellers-Patio-Lawn-Garden/zgbs/lawn-garden/ref=zg_bs_nav_0");
        startUrl.add("https://www.amazon.com/Best-Sellers-Home-Improvement/zgbs/hi/ref=zg_bs_nav_0");
        startUrl.add("https://www.amazon.com/Best-Sellers-Electronics/zgbs/electronics/ref=zg_bs_nav_0");
        startUrl.add("https://www.amazon.com/Best-Sellers-Toys-Games/zgbs/toys-and-games/ref=zg_bs_nav_0");
        startUrl.add("https://www.amazon.com/Best-Sellers-Pet-Supplies/zgbs/pet-supplies/ref=zg_bs_nav_0");
        startUrl.add("https://www.amazon.com/Best-Sellers-Cell-Phones-Accessories/zgbs/wireless/ref=zg_bs_nav_0");
        startUrl.add("https://www.amazon.com/Best-Sellers-Computers-Accessories/zgbs/pc/ref=zg_bs_nav_0");
        startUrl.add("https://www.amazon.com/Best-Sellers-Sports-Outdoors/zgbs/sporting-goods/ref=zg_bs_nav_0");
        startUrl.add("https://www.amazon.com/Best-Sellers-Automotive/zgbs/automotive/ref=zg_bs_nav_0");
        startUrl.add("https://www.amazon.com/best-sellers-camera-photo/zgbs/photo/ref=zg_bs_nav_0");
        startUrl.add("https://www.amazon.com/Best-Sellers-Arts-Crafts-Sewing/zgbs/arts-crafts/ref=zg_bs_nav_0");
    }

    @Override
    public void process(Page page) {
        Html html = new Html(page.getRawText());
        if (!page.getUrl().get().contains("pg=5&ajax=1") && !page.getUrl().get().contains("pg=1&ajax=1")) {
            List<String> Listlinks = html.xpath("//*[@id=\"zg_browseRoot\"]/ul/ul").links().all();
            page.addTargetRequests(Listlinks);//add list url to resource pool
            for (String url : Listlinks) {
                System.out.println("add url to pool mater:" + url);
                System.out.println("add url to pool #5:" + url + "?_encoding=UTF8&pg=5&ajax=1");
                System.out.println("add url to pool #1:" + url + "?_encoding=UTF8&pg=1&ajax=1");
                page.addTargetRequest(url + "?_encoding=UTF8&pg=1&ajax=1");
                page.addTargetRequest(url + "?_encoding=UTF8&pg=5&ajax=1");
            }
        }

        List<String> detalisLinks = html.xpath("/html/body/div/div[2]/div/a").links().all();
        if (page.getUrl().get().contains("pg=5&ajax=1") || page.getUrl().get().contains("pg=1&ajax=1")) {
            if (page.getUrl().get().contains("pg=5&ajax=1")) {
                page.putField("url1", "https://www.amazon.com" + detalisLinks.get(19));
                page.putField("url2", "https://www.amazon.com" + detalisLinks.get(18));
                page.putField("url3", "https://www.amazon.com" + detalisLinks.get(17));
            } else {
                page.putField("url1", "https://www.amazon.com" + detalisLinks.get(0));
                page.putField("url2", "https://www.amazon.com" + detalisLinks.get(1));
                page.putField("url3", "https://www.amazon.com" + detalisLinks.get(2));
            }
        }
    }

    @Override
    public Site getSite() {
        return Site
                .me()
                .setTimeOut(30000)//超时间隔
                .setSleepTime(2000)
                .setRetryTimes(3)//重试3次
                .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.109 Safari/537.36")//请求浏览器
                .setCycleRetryTimes(3)
                ;
    }

    public static void main(String[] args) {
        HttpClientDownloader downloader = new HttpClientDownloader();
        downloader.setProxyProvider(SimpleProxyProvider.from(new Proxy("proxy.abuyun.com", 9020, "H9142YEPE0R727DD", "B96781AC19AC1926")));
        Spider spider = Spider.create(new GoodsUrlListProcessor())
                .addPipeline(new RedisPipeline("127.0.0.1"));
        for (String url : startUrl) {
            spider.addUrl(url);
        }
        spider.setDownloader(downloader);
        spider.thread(10);
        spider.run();
    }
}
