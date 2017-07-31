package smartdo.crawler.amazon.scheduler;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.Scheduler;

/**
 * Created by fate
 */
public class Redis10DetailsScheduler implements Scheduler {

    protected JedisPool pool;
    private static final String r_url_list = "r_goods_url_list";

    public Redis10DetailsScheduler(String host) {
        pool = new JedisPool(new JedisPoolConfig(), host);
    }

    @Override
    public void push(Request request, Task task) {
        if(request.getUrl() != null) {
            Jedis jedis = this.pool.getResource();
            try {
                jedis.lpush(r_url_list, request.getUrl());
            } finally {
                this.pool.returnResource(jedis);
            }
        }
    }

    @Override
    public Request poll(Task task) {
        Jedis jedis = this.pool.getResource();
        Request request;
        try {
            String url = jedis.lpop(r_url_list);
            if(url == null){
                return null;
            }else{
                request = new Request(url);
                return request;
            }
        } finally {
            this.pool.returnResource(jedis);
        }

    }
}
