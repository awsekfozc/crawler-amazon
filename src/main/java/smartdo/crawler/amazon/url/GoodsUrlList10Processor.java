package smartdo.crawler.amazon.url;

import smartdo.crawler.amazon.pipeline.Redis10Pipeline;
import smartdo.crawler.amazon.scheduler.Redis10Scheduler;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by fate
 */
public class GoodsUrlList10Processor implements PageProcessor {

    static List<String> startUrl = new ArrayList<>();
    static {
//        startUrl.add("https://www.amazon.com/Best-Sellers-Patio-Lawn-Garden-Outdoor-Fireplaces/zgbs/lawn-garden/3742251/ref=zg_bs_nav_lg_4_16135375011");

        startUrl.add("https://www.amazon.com/Best-Sellers-Home-Kitchen/zgbs/home-garden/ref=zg_bs_nav_0?page_smartdo_depth=1");
        startUrl.add("https://www.amazon.com/Best-Sellers-Home-Improvement/zgbs/hi/ref=zg_bs_nav_0?page_smartdo_depth=1");
        startUrl.add("https://www.amazon.com/Best-Sellers-Patio-Lawn-Garden/zgbs/lawn-garden/ref=zg_bs_nav_0?page_smartdo_depth=1");
        startUrl.add("https://www.amazon.com/Best-Sellers-Kitchen-Dining/zgbs/kitchen/ref=zg_bs_nav_0?page_smartdo_depth=1");
    }

    @Override
    public void process(Page page) {
        Html html = new Html(page.getRawText());
        Selectable ul = html.xpath("//*[@id=\"zg_browseRoot\"]/ul/ul");

        Selectable chirldUl = ul;
        int i = 1;
        while (true){
            Selectable now = chirldUl;
            chirldUl = chirldUl.xpath("ul/ul");
            if("null".equals(chirldUl.get()) || chirldUl.get() == null){
                if(!now.get().contains("<span class=\"zg_selected\">")) {
                    List<String> Listlinks = now.links().all();
                    page.addTargetRequests(Listlinks);
                }
                String goodUrl = html.xpath("//*[@id=\"zg_centerListWrapper\"]/div[11]/div[2]/div/a/@href").get();
                page.putField("goodurl", "https://www.amazon.com" + goodUrl + "&page_smartdo_depth=" + i);
                break;
            }
            i++;
        }
    }

    public String getPageDepth(String url){
        String page_depth = url.substring(url.indexOf("page_smartdo_depth="));
        return page_depth;
    }

    @Override
    public Site getSite() {
        return Site
                .me()
                .setTimeOut(30000)//超时间隔
//                .setSleepTime(1000)
                .setRetryTimes(5)//重试5次
                .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.109 Safari/537.36")//请求浏览器
                .setCycleRetryTimes(3)
                ;
    }

    public static void main(String[] args) {
        HttpClientDownloader downloader = new HttpClientDownloader();
        downloader.setProxyProvider(SimpleProxyProvider.from(new Proxy("proxy.abuyun.com", 9020,"HS0S18E6J6DY575D","5989EDDF1F7E872D")));
        Spider spider = Spider.create(new GoodsUrlList10Processor())
                .setScheduler(new Redis10Scheduler("127.0.0.1"))
                .addPipeline(new Redis10Pipeline("127.0.0.1"));

        for (String url : startUrl) {
            spider.addUrl(url);
        }
        spider.setDownloader(downloader);
        spider.thread(15);
        spider.run();
    }
}
