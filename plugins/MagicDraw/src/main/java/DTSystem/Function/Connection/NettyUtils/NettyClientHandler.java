package DTSystem.Function.Connection.NettyUtils;

import DTSystem.Function.Connection.Pojo.DTProject;
import DTSystem.Function.DataModification.DataModification;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NettyClientHandler extends ChannelInboundHandlerAdapter
{
    private  ChannelHandlerContext ctx;
    private final String json;

    // 控制等待回应的操作
    private CountDownLatch latch;
    private String response;

    // 客户端请求的心跳命令
    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("hb_request", CharsetUtil.UTF_8));
    private int fcount = 1;  // 心跳循环次数

    private final ArrayList<DTProject> projectList = new ArrayList<>();  // 用户项目列表
    private int uploadCode;

    public NettyClientHandler(String json)
    {
        this.json = json;
    }

    public void sendMessage()
    {
        latch = new CountDownLatch(1);  // 发送消息的时候，将计数器的设置为1
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        // System.out.println("channel status-1: " + ctx.channel().isActive());
        this.ctx = ctx;
        ctx.writeAndFlush(Unpooled.copiedBuffer(json, CharsetUtil.UTF_8));
        ctx.fireChannelActive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        // 接收服务端发送过来的消息
        ByteBuf in = (ByteBuf) msg;
        this.response = in.toString(CharsetUtil.UTF_8);
        System.out.println(response);

        JSONObject jsonResponse = JSONObject.parseObject(this.response);
        int code = jsonResponse.getInteger("code");

        //  接收服务器的登录验证信息(代码200)
        if(code == 200)
        {
            JSONArray data = jsonResponse.getJSONArray("data");
            for(int i = 0; i < data.size(); i++)
            {
                JSONObject jsonObject = (JSONObject)data.get(i);
                DTProject project = new DTProject(jsonObject);
                projectList.add(project);
            }
        }
        // 接收服务器的修改信息(代码4100)
        else if(code == 4100)
        {
            JSONObject data = JSONObject.parseObject(jsonResponse.getString("data"));
            String newValue = data.getString("newValue");
            String id = data.getString("id");
            String elementName = data.getString("elementName");

            DataModification dataModification = new DataModification(id, elementName, newValue);
            boolean flag = dataModification.modify();

            Map<String, String> responseMap = new HashMap<>();
            if(flag)
            {
                System.out.println("修改成功");
                responseMap.put("method", "responseModifyModelData");
                responseMap.put("response", "success");
            }
            else
            {
                System.out.println("修改失败");
                responseMap.put("method", "responseModifyModelData");
                responseMap.put("response", "error");
            }
            String json = JSON.toJSONString(responseMap, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                    SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteNullListAsEmpty);
            byte[] bytes = json.getBytes(CharsetUtil.UTF_8);
            ByteBuf out = Unpooled.wrappedBuffer(bytes);
            ctx.writeAndFlush(out);
        }
        else if(code == 210)
        {
            System.out.println("上传成功");
            this.uploadCode = 210;
        }
        else if(code == 211)
        {
            System.out.println("上传失败");
            this.uploadCode = 211;
        }

        latch.countDown();  // 收到服务器的消息的时候，将计数器减1
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception
    {
        System.out.println("[INFO] time: " + date() + ", count: " + fcount);
        if (obj instanceof IdleStateEvent)
        {
            IdleStateEvent event = (IdleStateEvent) obj;
            if (IdleState.WRITER_IDLE.equals(event.state()))
            {
                ctx.channel().writeAndFlush(HEARTBEAT_SEQUENCE.duplicate());
            }
        }
        fcount++;
    }

    public void waitForResponse() throws InterruptedException
    {
        // 在等待回应的操作中，调用await方法进行等待，直到计数器为0，即可收到服务端的回应
        boolean success = latch.await(10, TimeUnit.SECONDS);
        if (!success)
        {
            System.out.println("丢失了服务器的返回的一条消息！！！ \n");
        }
    }

    public String getResponse()
    {
        return this.response;
    }

    public ArrayList<DTProject> getProjectList()
    {
        return this.projectList;
    }

    public int getUploadCode()
    {
        return this.uploadCode;
    }

    private String date()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }
}
