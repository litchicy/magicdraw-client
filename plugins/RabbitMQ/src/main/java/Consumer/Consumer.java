package Consumer;

import com.alibaba.fastjson.JSONObject;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetSocketAddress;

import Conversion.Conversion;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Consumer
{
    private final String QUEUE_NAME;
    // rabbitmq config
    private final String host;
    private final int port;
    private final String virtualHost;
    private final String userName;
    private final String userPassword;

    private JSONObject jsonObject = null;

    public Consumer(String QUEUE_NAME, String host, int port, String virtualHost, String userName, String userPassword)
    {
        this.QUEUE_NAME = QUEUE_NAME;
        this.host = host;
        this.port = port;
        this.virtualHost = virtualHost;
        this.userName = userName;
        this.userPassword = userPassword;
    }

    public void recv() throws Exception
    {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.host);
        factory.setPort(this.port);
        factory.setVirtualHost(this.virtualHost);
        factory.setUsername(this.userName);
        factory.setPassword(this.userPassword);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(this.QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) ->
        {
            Conversion conversion = new Conversion();
            byte[] bytes = delivery.getBody();
            System.out.println(" [x] Received 1 message(size: " + bytes.length + " bytes)");

            try
            {
                this.jsonObject = conversion.byteToJson(bytes);
                JSONFileGeneration(jsonObject);

                System.out.println("Json Document has been generated.");
                System.out.println("=====================================");
                System.out.println("User ID: " + jsonObject.getString("user_id"));
                System.out.println("User IP: " + jsonObject.getString("user_ip"));
                System.out.println("Model Type: " + jsonObject.getString("model_type"));
                System.out.println("Project Name: " + jsonObject.getString("project_name"));
                System.out.println("File Name: " + jsonObject.getString("file_name"));
                System.out.println("Update: " + jsonObject.getString("update"));
                System.out.println("Send Time: " + jsonObject.getString("date"));
                System.out.println("=====================================");
            }
            catch (ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }
        };
        channel.basicConsume(this.QUEUE_NAME, true, deliverCallback, consumerTag -> { });

        final NettyServerHandler nettyServerHandler = new NettyServerHandler();
        // 创建EventLoopGroup
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        // 创建EventLoopGroup
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)  //指定所使用的NIO传输Channel
                .localAddress(new InetSocketAddress(2181))  //使用指定的端口设置套接字地址
                .childHandler(new ChannelInitializer<SocketChannel>()  // 添加一个EchoServerHandler到Channel的ChannelPipeline
                {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception
                    {
                        socketChannel.pipeline().addLast(nettyServerHandler);
                    }
                });

        try
        {
            ChannelFuture f = b.bind().sync();  // 异步地绑定服务器;调用sync方法阻塞等待直到绑定完成
            f.channel().closeFuture().sync();  // 获取Channel的CloseFuture，并且阻塞当前线程直到它完成
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            // 关闭EventLoopGroup，释放所有的资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void JSONFileGeneration(JSONObject jsonObject)
    {
        String jsonStr = JSONObject.toJSONString(jsonObject);
        String path = jsonObject.getString("file_name");

        try(FileOutputStream fos = new FileOutputStream(path))
        {
            OutputStreamWriter out = new OutputStreamWriter(fos);
            BufferedWriter bufWriter = new BufferedWriter(out);
            bufWriter.write(jsonStr);
            bufWriter.close();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}