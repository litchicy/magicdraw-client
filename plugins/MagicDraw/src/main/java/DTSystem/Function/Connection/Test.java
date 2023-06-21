package DTSystem.Function.Connection;

import DTSystem.Function.Connection.NettyUtils.NettyClientHandler;
import DTSystem.Function.Connection.Pojo.DTModel;
import DTSystem.Function.Connection.Pojo.DTProject;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Test
{
    public static void main(String[] args) throws InterruptedException
    {
        String id = "MA_TEST_1";
        String password = "MA_TEST_1";
        Map<String, Object> map = new HashMap<>();
        map.put("username", id);
        map.put("password", password);
        map.put("method", "login");
        map.put("model_type", "MagicDraw");
        String json = JSON.toJSONString(map, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                                             SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteNullListAsEmpty);

        // 向服务器发送信息验证账号密码是否正确
        NettyClientHandler nettyClientHandler = new NettyClientHandler(json);
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception
                    {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 1, 0, TimeUnit.SECONDS));
                        pipeline.addLast("handler", nettyClientHandler);
                    }
                });

        // String serverIP = "localhost";
        String serverIP = "8.130.42.107";
        int serverPort = 2181;
        ChannelFuture channelFuture = bootstrap.connect(serverIP, serverPort).sync();
        nettyClientHandler.sendMessage();
        nettyClientHandler.waitForResponse();

        // System.out.println(nettyClientHandler.getResponse());
        JSONObject jsonResponse = JSONObject.parseObject(nettyClientHandler.getResponse());
        int code = jsonResponse.getInteger("code");
        System.out.println("code: " + code);

        ArrayList<DTProject> projectList = nettyClientHandler.getProjectList();

        /*
        for(int i = 0; i < projectList.size(); i++)
        {
            DTProject project = projectList.get(i);
            System.out.println("project_id: " + project.getProjectID());
            System.out.println("project_name: " + project.getProjectName());

            ArrayList<DTModel> modelList = project.getModelList();
            for(int j = 0; j < modelList.size(); j++)
            {
                System.out.println("model_id: " + modelList.get(j).getModelID());
                System.out.println("model_name: " + modelList.get(j).getModelName());
            }
        }*/

        JFrame frame = new JFrame("同步更新");
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);  // 设置对话框在屏幕中间弹出
        JPanel panel = new JPanel();

        JLabel label1 = new JLabel("项目 ");
        JLabel label2 = new JLabel("模型 ");
        JComboBox<String> comboBox1 = new JComboBox<>();
        JComboBox<String> comboBox2 = new JComboBox<>();
        JButton button = new JButton("确认上传");

        comboBox1.addItem("-项目-");
        comboBox2.addItem("-模型-");

        final String[] projectID = new String[1];
        final String[] modelID = new String[1];
        for(int i = 0; i < projectList.size(); i++)
        {
            DTProject project = projectList.get(i);
            comboBox1.addItem(project.getProjectName());
        }
        comboBox1.addActionListener(e ->
        {
            projectID[0] = Objects.requireNonNull(comboBox1.getSelectedItem()).toString();
            int index = comboBox1.getSelectedIndex() - 1;
            ArrayList<DTModel> modelList = projectList.get(index).getModelList();
            for(int i = 0; i < modelList.size(); i++)
            {
                DTModel model = modelList.get(i);
                comboBox2.addItem(model.getModelName());
            }

        });
        comboBox2.addActionListener(e ->
        {
            modelID[0] = Objects.requireNonNull(comboBox2.getSelectedItem()).toString();
        });

        button.addActionListener(e ->
        {
            System.out.println(projectID[0]);
            System.out.println(modelID[0]);
        });

        panel.add(label1);
        panel.add(comboBox1);
        panel.add(label2);
        panel.add(comboBox2);
        panel.add(button);

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        channelFuture.channel().closeFuture().sync();
    }
}
