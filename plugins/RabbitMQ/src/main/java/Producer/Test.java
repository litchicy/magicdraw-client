package Producer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.HashMap;
import java.util.Map;

public class Test
{
    public static void main(String[] argv)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", "B19080618");
        map.put("user_ip", "127.0.0.1");
        map.put("model_type", "magic draw");
        map.put("project_name", "project.mdzip");
        map.put("file_name", "test.json");
        map.put("update", "false");
        map.put("date", "2023-5-2 17:31");
        String json = JSON.toJSONString(map, SerializerFeature.PrettyFormat, SerializerFeature.WriteNullListAsEmpty);
        JSONObject jsonObject = JSONObject.parseObject(json);

        String QUEUE_NAME = "hello";
        String host = "8.130.42.107";
        String virtualHost = "/";
        String userName = "admin";
        String userPassword = "123456";
        int port = 5672;
        Producer producer = new Producer(QUEUE_NAME, jsonObject, host, port, virtualHost, userName, userPassword);
        boolean flag = producer.send();
        System.out.println(flag);
    }
}
