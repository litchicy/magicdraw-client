package DTSystem.Function.Login;

import DTSystem.Function.Login.Components.Account.LoginButton;
import DTSystem.Function.Login.Components.Account.Password.PasswordLabel;
import DTSystem.Function.Login.Components.Account.User.UserLabel;
import DTSystem.Function.Connection.NettyUtils.NettyClientHandler;
import DTSystem.Function.UpLoad.UpLoad;
import DTSystem.Function.Login.Components.Welcome;
import DTSystem.Function.Login.Components.Account.User.UserTextField;
import DTSystem.Function.Login.Components.Account.Password.PasswordTextField;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import com.nomagic.actions.ActionsCategory;
import com.nomagic.magicdraw.actions.MDAction;

import javax.swing.JFrame;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import java.awt.Color;
import java.awt.event.ActionEvent;

import java.net.UnknownHostException;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/*
    登录数字线索系统
    测试账号：MA_TEST_1(密码同用户名)
*/

public class Login extends MDAction
{
    private final ActionsCategory category;  // Magic Draw category
    private String onlineID;  // online account

    /*
      UI config
    */
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;
    private static final String TITLE = "登录";
    private final JFrame loginUI = new JFrame();

    public Login(ActionsCategory category)
    {
        super("", "登录", null, null);
        this.category = category;
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event)
    {
        /*
          Login module: UI config
         */
        loginUI.setSize(WIDTH, HEIGHT);
        loginUI.setTitle(TITLE);

        loginUI.setLayout(null);
        loginUI.setVisible(true);
        loginUI.setLocationRelativeTo(null);  // 设置对话框在屏幕中间弹出
        loginUI.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        loginUI.getContentPane().setBackground(new Color(0xF5F5F5));  // Set the background color

        // add components
        Welcome welcome = new Welcome();
        UserLabel userLabel = new UserLabel();
        PasswordLabel passwordLabel = new PasswordLabel();
        loginUI.add(welcome);
        loginUI.add(userLabel);
        loginUI.add(passwordLabel);

        UserTextField userTextField = new UserTextField();
        PasswordTextField passwordTextField = new PasswordTextField();
        LoginButton loginButton = new LoginButton();

        // Handle button events
        loginButton.addActionListener((e ->
        {
            String id = userTextField.getText();
            String password = new String(passwordTextField.getPassword());
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


            Runnable task = () ->
            {
                try
                {
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
                    // System.out.println("channel status-3: " + channelFuture.channel().isActive());

                    // 读取服务端发来的校验码
                    if(code == 400001)
                    {
                        JOptionPane.showMessageDialog(null, "用户名或密码错误", "数字线索系统", JOptionPane.PLAIN_MESSAGE);
                    }
                    else if(code == 200)
                    {
                        this.onlineID = userTextField.getText();
                        JOptionPane.showMessageDialog(null, "登录成功", "数字线索系统", JOptionPane.PLAIN_MESSAGE);
                        loginUI.setVisible(false);  // Make the login screen invisible
                        this.putValue(Action.NAME, "当前在线: " + this.onlineID);  // After successful login, the account name is displayed

                        // Add functions
                        try
                        {
                            category.addAction(new UpLoad(id, "MagicDraw", nettyClientHandler));
                        }
                        catch (UnknownHostException ex)
                        {
                            throw new RuntimeException(ex);
                        }
                    }
                    else if(code == 404)
                    {
                        JOptionPane.showMessageDialog(null, "网络错误", "数字线索系统", JOptionPane.PLAIN_MESSAGE);
                    }
                    channelFuture.channel().closeFuture().sync();
                }
                catch (Exception ex)
                {
                    throw new RuntimeException(ex);
                }
                finally
                {
                    group.shutdownGracefully();
                }

            };
            Thread thread = new Thread(task);
            thread.start();
        }));

        loginUI.add(userTextField);
        loginUI.add(passwordTextField);
        loginUI.add(loginButton);
    }
}
