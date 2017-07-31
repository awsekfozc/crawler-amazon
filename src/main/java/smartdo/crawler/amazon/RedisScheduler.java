package smartdo.crawler.amazon;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.Scheduler;

/**
 * Created by fate
 */
public class RedisScheduler implements Scheduler {

    protected JedisPool pool;
    private static final String r_url_list = "r_url_list";

    public RedisScheduler(String host) {
        pool = new JedisPool(new JedisPoolConfig(), host);
    }

    @Override
    public void push(Request request, Task task) {
        Jedis jedis = this.pool.getResource();
        try {
            jedis.lpush(r_url_list,request.getUrl());
        } finally {
            this.pool.returnResource(jedis);
        }
    }

    @Override
    public Request poll(Task task) {
        Jedis jedis = this.pool.getResource();
        Request request;
        try {
            String url = jedis.lpop(r_url_list);
            request = new Request(url);
        } finally {
            this.pool.returnResource(jedis);
        }
        return request;
    }
}
