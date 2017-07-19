package smartdo.crawler.amazon;

import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.PhantomJSDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fate
 */
public class GoodsDetalisProcessor implements PageProcessor {
    static final String ASIN_REG = "dp/\\w{10}";
    @Override
    public void process(Page page) {
        try {
            Html html = new Html(page.getRawText());

            //price
            String price = html.xpath("//*[@id=\"priceblock_ourprice\"]/text()").get();
            if (StringUtils.isBlank(price))
                price = html.xpath("//*[@id=\"priceblock_saleprice\"]/text()").get();
            if (StringUtils.isBlank(price))
                price = html.xpath("//*[@id=\"priceblock_dealprice\"]/text()").get();
            if (StringUtils.isBlank(price))
                price = html.xpath("//*[@id=\"snsPrice\"]/div/span[2]/text()").get();
            page.putField("price", trim(price));

            //customer_review
            String customer_review = html.xpath("//*[@id=\"acrCustomerReviewText\"]/text()").get();
            if (StringUtils.isNotBlank(customer_review))
                customer_review = customer_review.replace(" customer reviews", "");
            page.putField("customer_review", trim(customer_review));

            //title
            String title = html.xpath("//*[@id=\"productTitle\"]/text()").get();
            page.putField("title", trim(title));

            //asin
            String asin = getAsin(page.getUrl().get());
            page.putField("asin", trim(asin));

            //big_type and small_type
            List<Selectable> list = html.xpath("//*[@id=\"wayfinding-breadcrumbs_feature_div\"]/ul/li").nodes();
            if (list.size() > 1) {
                String big_type = list.get(0).xpath("//*[@class=\"a-color-tertiary\"]/text()").get();
                page.putField("big_type", trim(big_type));
                String small_type = list.get(list.size() - 1).xpath("//*[@class=\"a-color-tertiary\"]/text()").get();
                page.putField("small_type", trim(small_type));
            }

            //score
            String score = html.xpath("//*[@id=\"acrPopover\"]/span[1]/a/i[1]/span/text()").get();
            if (StringUtils.isNotBlank(score))
                score = score.replace(" out of 5 stars", "");
            page.putField("score", trim(score));

            //is_ziying and is_fba
            String content = html.xpath("//*[@id=\"centerCol\"]/tidyText()").get();
            if (StringUtils.isBlank(content))
                content = html.xpath("//*[@id=\"merchant-info\"]/tidyText()").get();
            String is_ziying = "no";
            if (StringUtils.isNotBlank(content) && content.contains("sold by Amazon"))
                is_ziying = "yes";
            page.putField("is_ziying", trim(is_ziying));
            String is_fba = "no";
            if (StringUtils.isNotBlank(content) && content.contains("Fulfilled by Amazon"))
                is_fba = "yes";
            page.putField("is_fba", trim(is_fba));

            //image_path
            String img_ele = html.xpath("//*[@id=\"landingImage\"]/@data-old-hires").get();
//            String img_ele = html.xpath("//*[@id=\"ivLargeImage\"]/img/@src").get();
            page.putField("image_path", trim(img_ele));

            //big_type_rank
            String big_type_rank = html.xpath("//*[@id=\"SalesRank\"]/tidyText()").get();
            if (StringUtils.isNotBlank(big_type_rank) && big_type_rank.length() > 1) {
                big_type_rank = big_type_rank.substring(big_type_rank.indexOf("#"));
                big_type_rank = big_type_rank.substring(1, big_type_rank.indexOf(" "));
            }

            //small_type_rank
            List<Selectable> rank_list = html.xpath("//*[@id=\"SalesRank\"]/ul/li").nodes();
            String small_type_rank = "";
            for (Selectable li : rank_list)
                small_type_rank += li.xpath("//*[@class=\"zg_hrsr_rank\"]/text()") + ":" + li.xpath("//*[@class=\"zg_hrsr_ladder\"]/b/a/text()") + "\t";


            //另一种形式的排行获取
            List<Selectable> other_rank_list = html.xpath("//*[@id=\"productDetails_detailBullets_sections1\"]/tbody/tr").nodes();
            if (StringUtils.isBlank(big_type_rank)) {
                for (Selectable tr : other_rank_list) {
                    //*[@id="productDetails_detailBullets_sections1"]/tbody/tr[12]/th
                    String thStr = tr.xpath("//*[@class=\"a-color-secondary a-size-base prodDetSectionEntry\"]/text()").get();
                    if (StringUtils.isNotBlank(thStr) && "Best Sellers Rank".equals(thStr.trim())) {
                        big_type_rank = tr.xpath("td/span/span[1]/text()").get();
                        if (StringUtils.isNotBlank(big_type_rank) && big_type_rank.length() > 1) {
                            big_type_rank = big_type_rank.substring(1, big_type_rank.indexOf(" "));
                        }
                        rank_list = tr.xpath("td/span/span").nodes();
                        for (int i = 0; i < rank_list.size(); i++) {
                            if (i == 0)
                                continue;
                            Selectable li = rank_list.get(i);
                            String rank = li.xpath("span/text()").get();
                            if (StringUtils.isNotBlank(rank))
                                rank = rank.substring(1, rank.indexOf(" "));
                            String small = "";
                            List<Selectable> small_rank_list = li.xpath("a").nodes();
                            if (small_rank_list.size() > 0)
                                small = small_rank_list.get(small_rank_list.size() - 1).xpath("a/text()").get();
                            small_type_rank += rank + ":" + small + "\t";
                        }
                    }
                }
            }
            page.putField("big_type_rank", trim(big_type_rank));
            page.putField("small_type_rank", trim(small_type_rank));

            //url
            page.putField("url", trim(page.getRequest().getUrl()));
            System.out.println(page.getResultItems().getAll());

        } catch (Exception ex) {
            ex.printStackTrace();
            String[] retry = page.getRequest().getUrl().split("&");
            if (retry.length < 7) {
                page.addTargetRequest(new Request(page.getRequest().getUrl() + "&" + Math.random()));
            }
        }
    }

    public String getAsin(String url) {
        Pattern r = Pattern.compile(ASIN_REG);
        Matcher m = r.matcher(url);
        String reStr = "";
        if (m.find()) {
            reStr = m.group(0);
        }
        if (StringUtils.isNotBlank(reStr))
            reStr = reStr.replace("dp/", "");
        return reStr;
    }

    public String trim(String str) {
        if (StringUtils.isNotBlank(str))
            return str.trim();
        return "";
    }

    public String getImgUrl() {
        return "";
    }

    @Override
    public Site getSite() {
        return Site
                .me()
                .setTimeOut(30000)//超时间隔
                .setSleepTime(2000)
                .setRetryTimes(3)//重试3次
                .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.109 Safari/537.36")//请求浏览器
                .setCycleRetryTimes(3);
    }

    public static void main(String[] args) {
        PhantomJSDownloader phantomJSDownloader = new PhantomJSDownloader("E:\\zc\\phantomjs\\bin\\phantomjs.exe --proxy=http://proxy.abuyun.com:9020 --proxy-auth=H9142YEPE0R727DD:B96781AC19AC1926", "E:\\zc\\crawl.js");
        phantomJSDownloader.setThread(5);
//        Spider.create(new GoodsDetalisProcessor())
//                .addUrl("https://www.amazon.com/Aromatherapy-Essential-Humidifier-Ultrasonic-Diffusers/dp/B06ZZLN6DP/ref=zg_bs_11056591_100?_encoding=UTF8&psc=1&refRID=SZPF15KEZHMSNRP9BNZQ")
//                .addUrl("https://www.amazon.com/doTERRA-Serenty-Essential-Restful-Blend/dp/B004NJXW1I/ref=zg_bs_11056591_99?_encoding=UTF8&refRID=SZPF15KEZHMSNRP9BNZQ&th=1")
//                .setDownloader(phantomJSDownloader)
//                .thread(2)
//                .run();

        Spider spider = Spider.create(new GoodsDetalisProcessor())
                .setDownloader(phantomJSDownloader)
                .setScheduler(new RedisScheduler("127.0.0.1"))
                .thread(10)
                .addPipeline(new AmazonDetailsPipeline("E:\\zc\\data"));
//        List<String> urlList = FileUtils.readFileByLines("E:\\zc\\test.json");
//        for (String url : urlList) {
//            Map<String, String> map = JSON.parseObject(url, Map.class);
//            for (String str : map.keySet())
//                spider.addUrl(map.get(str));
//        }
        spider.run();
    }
}
