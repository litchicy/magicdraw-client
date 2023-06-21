package DTSystem.Function.UpLoad;

import DTSystem.Function.Connection.NettyUtils.NettyClientHandler;
import DTSystem.Function.Connection.Pojo.DTModel;
import DTSystem.Function.Connection.Pojo.DTProject;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.magicdraw.uml.ClassTypes;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import java.awt.event.ActionEvent;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.UnknownHostException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Producer.Producer;

/**
 * 将模型文件转换为json文件并上传服务器
 */
public class UpLoad extends DefaultBrowserAction
{
    /*
     * 用户和项目信息
     */
    private final String userID;
    private final String userIP;
    private final String modelType;
    private final boolean cover = false;  // 多版本

    /*
     * 建立一个map，它将用于json的转换
     */
    private final Map<String, Object> map = new HashMap<>();

    private final ArrayList<DTProject> dtProjectList;

    private final NettyClientHandler nettyClientHandler;
    // 构造函数
    public UpLoad(String userID, String modelType, NettyClientHandler nettyClientHandler) throws UnknownHostException
    {
        super("", "云端同步", null, null);
        this.userID = userID;
        this.modelType = modelType;
        this.userIP = getPublicIP(); // Get host IP
        this.nettyClientHandler = nettyClientHandler;
        this.dtProjectList = nettyClientHandler.getProjectList();
    }

    private DTProject findSelectedDTProject(String projectName)
    {
        DTProject result = null;
        for(int i = 0; i < dtProjectList.size(); i++)
        {
            DTProject dtProject = dtProjectList.get(i);
            if(projectName.equals(dtProject.getProjectName()))
            {
                result = dtProject;
                break;
            }
        }
        return result;
    }

    private String findSelectedDTModelID(DTProject dtProject, String modelName)
    {
        String dtModelID = null;
        ArrayList<DTModel> dtModelList = dtProject.getModelList();
        for(int i = 0; i < dtModelList.size(); i++)
        {
            DTModel dtModel = dtModelList.get(i);
            if(modelName.equals(dtModel.getModelName()))
            {
                dtModelID = dtModel.getModelID();
                break;
            }
        }
        return dtModelID;
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        // 构建json对象
        Project project = Application.getInstance().getProject();
        if(project != null)
        {
            JFrame frame = new JFrame("同步更新");
            frame.setSize(400, 300);
            frame.setLocationRelativeTo(null);  // 设置对话框在屏幕中间弹出
            JPanel panel = new JPanel();

            JLabel label1 = new JLabel("项目 ");
            JLabel label2 = new JLabel("模型 ");
            JComboBox<String> comboBox1 = new JComboBox<>();
            JComboBox<String> comboBox2 = new JComboBox<>();
            JButton button = new JButton("确认上传");

            comboBox1.addItem("-项目-");
            comboBox2.addItem("-模型-");

            final DTProject[] selectedDTProject = {null};
            final String[] selectedDTProjectID = new String[1];
            final String[] selectedDTModelID = new String[1];
            for(int i = 0; i < dtProjectList.size(); i++)
            {
                DTProject dtProject = dtProjectList.get(i);
                comboBox1.addItem(dtProject.getProjectName());
            }
            comboBox1.addActionListener(e ->
            {
                selectedDTProject[0] = findSelectedDTProject(Objects.requireNonNull(comboBox1.getSelectedItem()).toString());
                if(selectedDTProject[0] != null)
                {
                    selectedDTProjectID[0] = selectedDTProject[0].getProjectID();
                    int index = comboBox1.getSelectedIndex() - 1;
                    ArrayList<DTModel> modelList = dtProjectList.get(index).getModelList();
                    for(int i = 0; i < modelList.size(); i++)
                    {
                        DTModel model = modelList.get(i);
                        comboBox2.addItem(model.getModelName());
                    }
                }
                else
                {
                    selectedDTProjectID[0] = null;
                }
            });
            comboBox2.addActionListener(e ->
            {
                selectedDTModelID[0] = findSelectedDTModelID(selectedDTProject[0], Objects.requireNonNull(comboBox2.getSelectedItem()).toString());
            });

            button.addActionListener(e ->
            {
                if(selectedDTProjectID[0] == null)
                {
                    JOptionPane.showMessageDialog(null, "请选择项目", "同步更新", JOptionPane.PLAIN_MESSAGE);
                }
                else if(selectedDTModelID[0] == null)
                {
                    JOptionPane.showMessageDialog(null, "请选择模型", "同步更新", JOptionPane.PLAIN_MESSAGE);
                }
                else
                {
                    // 获取模型的根元素
                    Package root = project.getPrimaryModel();
                    Element[] elements = root.getOwnedElement().toArray(new Element[0]);
                    Element rootElement = elements[1];
                    Map<String, Object> rootElementMap = new HashMap<>();
                    rootElementMap.put("id", rootElement.getID());
                    rootElementMap.put("type", ClassTypes.getShortName(rootElement.getClassType()));
                    rootElementMap.put("name", rootElement.getHumanName());

                    List<Element> childElements = (List<Element>) rootElement.getOwnedElement();
                    List<Map<String, Object>> childList = new ArrayList<>();
                    for (Element element : childElements)
                    {
                        Map<String, Object> tempMap = new HashMap<>();
                        visitChildren(element, tempMap);
                        childList.add(tempMap);
                    }
                    if (childList.size() > 0)
                    {
                        rootElementMap.put("child_elements", childList);
                    }

                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                    String date = dateFormatter.format(new Date());
                    String fileName = this.userID + "_" + this.modelType + "_" + date;

                    map.put("user_id", this.userID);
                    map.put("user_ip", this.userIP);
                    map.put("model_type", this.modelType);
                    map.put("model_id", selectedDTModelID[0]);
                    map.put("model_name", project.getName() + ".mdzip");
                    map.put("project_id", selectedDTProjectID[0]);
                    map.put("project_name", selectedDTProject[0].getProjectName());
                    map.put("file_name", fileName + ".json");
                    map.put("cover", String.valueOf(this.cover));
                    map.put("date", date);
                    map.put("model_data", rootElementMap);

                    // 生成json
                    String json = JSON.toJSONString(map, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                            SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteNullListAsEmpty);
                    JSONObject jsonObject = JSONObject.parseObject(json);

                    // 配置RabbitMQ信息
                    String QUEUE_NAME = "hello";
                    String host = "8.130.42.107";
                    String virtualHost = "/";
                    String userName = "admin";
                    String userPassword = "123456";
                    int port = 5672;
                    Producer producer = new Producer(QUEUE_NAME, jsonObject, host, port, virtualHost, userName, userPassword);
                    boolean flag = producer.send();
                    System.out.println(flag);

                    try
                    {
                        nettyClientHandler.waitForResponse();
                        // Thread.sleep(3000);
                        int uploadCode = nettyClientHandler.getUploadCode();
                        if(uploadCode == 210)
                        {
                            JOptionPane.showMessageDialog(null, "上传成功", "同步更新", JOptionPane.PLAIN_MESSAGE);
                        }
                        else if(uploadCode == 211)
                        {
                            JOptionPane.showMessageDialog(null, "上传失败", "同步更新", JOptionPane.PLAIN_MESSAGE);
                        }
                    }
                    catch (InterruptedException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                }
            });

            panel.add(label1);
            panel.add(comboBox1);
            panel.add(label2);
            panel.add(comboBox2);
            panel.add(button);

            frame.add(panel);
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            frame.setVisible(true);
        }
        else
        {
            JOptionPane.showMessageDialog(null, "请打开一个Magic Draw项目以实现同步", "云端同步", JOptionPane.PLAIN_MESSAGE);
        }
    }

    // Visit owned elements
    private void visitChildren(Element element, Map<String, Object> map)
    {
        map.put("id", element.getID());
        map.put("type", ClassTypes.getShortName(element.getClassType()));
        map.put("name", element.getHumanName());

        List<Element> childElements = (List<Element>)element.getOwnedElement();
        List<Map<String, Object>> childList = new ArrayList<>();
        for(Element e: childElements)
        {
            Map<String, Object> tempMap = new HashMap<>();
            visitChildren(e, tempMap);
            childList.add(tempMap);
        }
        if(childList.size() > 0)
        {
            map.put("child_elements", childList);
        }
    }

    // Get the local public network IP
    private String getPublicIP()
    {
        String ip = "";
        String chinaz = "https://ip.chinaz.com";

        StringBuilder inputLine = new StringBuilder();
        String read;
        URL url;
        HttpURLConnection urlConnection;
        BufferedReader in = null;

        try
        {
            url = new URL(chinaz);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
            while ((read = in.readLine()) != null)
            {
                inputLine.append(read).append("\r\n");
            }
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        Pattern p = Pattern.compile("\\<dd class\\=\"fz24\">(.*?)\\<\\/dd>");
        Matcher m = p.matcher(inputLine.toString());
        if (m.find())
        {
            ip = m.group(1);
        }
        return ip;
    }
}