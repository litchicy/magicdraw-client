package Producer;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import Conversion.Conversion;

public class Producer
{
    private final JSONObject jsonObject;

    // Rabbitmq config
    private final String QUEUE_NAME;
    private final String host;
    private final int port;
    private final String virtualHost;
    private final String userName;
    private final String userPassword;

    // Construction function
    public Producer(String QUEUE_NAME, JSONObject jsonObject, String host, int port, String virtualHost, String userName, String userPassword)
    {
        this.QUEUE_NAME = QUEUE_NAME;
        this.jsonObject = jsonObject;

        this.host = host;
        this.port = port;
        this.virtualHost = virtualHost;
        this.userName = userName;
        this.userPassword = userPassword;
    }

    public boolean send()
    {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.host);
        factory.setPort(this.port);
        factory.setVirtualHost(this.virtualHost);
        factory.setUsername(this.userName);
        factory.setPassword(this.userPassword);

        try(Connection connection = factory.newConnection())
        {
            Channel channel = connection.createChannel();
            Conversion conversion = new Conversion();

            // Declare the queue, if the queue does not exist, the queue will be created
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.confirmSelect();  // Enable message confirmation
            channel.basicPublish("", QUEUE_NAME, null, conversion.jsonToByte(jsonObject));  // Send Message
            return channel.waitForConfirms();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
    }
}