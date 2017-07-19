package smartdo.crawler.amazon;

import com.alibaba.fastjson.JSON;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.FilePersistentBase;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class AmazonPipeline extends FilePersistentBase implements Pipeline {

    public AmazonPipeline(String path) {
        this.path = path;
    }

    public synchronized void process(ResultItems resultItems, Task task) {
        System.out.println("write data path:" + this.path);
        BufferedWriter out = null;
        try {
            if (resultItems.get("url1") != null) {
                String path = this.path + PATH_SEPERATOR + "yms.json";
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, true)));
                out.write(JSON.toJSONString(resultItems.getAll()) + "\n");
            }
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}