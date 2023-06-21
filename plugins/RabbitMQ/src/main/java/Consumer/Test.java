package Consumer;

public class Test
{
    public static void main(String[] argv) throws Exception
    {
        String QUEUE_NAME = "test";
        String host = "8.130.42.107";
        int port = 5672;
        String virtualHost = "/";
        String userName = "admin";
        String userPassword = "123456";
        Consumer consumer = new Consumer(QUEUE_NAME, host, port, virtualHost, userName, userPassword);
        consumer.recv();
    }
}
