package smartdo.crawler.amazon;

import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 * Created by fate
 */
public class RedisPipeline implements Pipeline {

    private static final String r_url_list = "r_url_list";

    protected JedisPool pool;
    public RedisPipeline(String host) {
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
