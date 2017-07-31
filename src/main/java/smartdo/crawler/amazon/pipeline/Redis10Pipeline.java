package smartdo.crawler.amazon.pipeline;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.Map;

/**
 * Created by fate
 */
public class Redis10Pipeline implements Pipeline {

    private static final String r_url_list = "r_goods_url_list";

    protected JedisPool pool;
    public Redis10Pipeline(String host) {
        pool = new JedisPool(new JedisPoolConfig(), host);
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        Jedis jedis = this.pool.getResource();
        try {
            Map<String,Object> map = resultItems.getAll();
            for(String key:map.keySet())
                jedis.lpush(r_url_list,map.get(key).toString());
        } finally {
            this.pool.returnResource(jedis);
        }
    }
}
