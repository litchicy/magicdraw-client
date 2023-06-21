package DTSystem.Function.XMLGeneration;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.magicdraw.uml.ClassTypes;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.OpaqueAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ControlFlow;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdcommunications.TimeEvent;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdcommunications.Trigger;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.TimeExpression;
import com.nomagic.uml2.impl.ModelHierarchyVisitor;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Action which displays its name.
 *
 * @author Donatas Simkunas
 */
public class XMLGenerator extends DefaultBrowserAction
{

    /*
     * Hierarchy visitor, for counting elements.
     * Can do different actions with different element types.
     */
    private final ModelHierarchyVisitor visitor = new ModelHierarchyVisitor();

    /*
     * User and project information
     */
    private final String userID;
    private final String userIP;
    private final String date;
    private final String modelType;
    private final boolean update = false;

    /*
     * Build a document object to operate XML
     */
    private final Document document = DocumentHelper.createDocument();
    private final String docName;  //XML file name

    /*
     * UI config
     */
    private final JFrame UI = new JFrame();
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;
    private static final String TITLE = "云端同步";

    // Constructor
    public XMLGenerator(String userID, String modelType) throws UnknownHostException
    {
        super("", "云端同步", null, null);
        this.userID = userID;
        this.modelType = modelType;
        this.userIP = getPublicIP(); // Get host IP

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        this.date = dateFormatter.format(new Date());
        this.docName = this.userID + "_" + this.modelType + "_" + this.date;
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent evt)
    {
        // initialize UI
        UI.setSize(WIDTH, HEIGHT);
        UI.setTitle(TITLE);

        UI.setLayout(null);  // Custom layout
        UI.setVisible(true);
        UI.setLocationRelativeTo(null);
        UI.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        UI.getContentPane().setBackground(new Color(0xF5F5F5));  // Set the background color

        // count children of selected in browser element.
        Project project = Application.getInstance().getProject();
        if(project != null)
        {
            // Get the root element
            Package root = project.getPrimaryModel();
            Element[] elements = root.getOwnedElement().toArray(new Element[0]);
            Element element = elements[1];

            // Write user and project information to XML file
            document.clearContent();
            org.dom4j.Element userItem = document.addElement("User").addAttribute("id", this.userID)
                                                                       .addAttribute("ip", this.userIP)
                                                                       .addAttribute("modeltype", this.modelType)
                                                                       .addAttribute("project_name", project.getName() + ".mdzip")
                                                                       .addAttribute("update", String.valueOf(this.update))
                                                                       .addAttribute("date", date);

            // Convert model to XML
            visitChildren(element, userItem);

            // Text area settings
            JTextArea textArea = new JTextArea();
            Border border = BorderFactory.createEtchedBorder();
            textArea.setLineWrap(true);  // word wrap
            textArea.setEditable(false);
            textArea.setText("");
            textArea.setBounds(200, 20, 180, 220);
            textArea.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 5, 5, 5)));

            // Button settings
            JButton uploadButton = new JButton("同步更新");
            uploadButton.setFocusPainted(false);  // Set the paintFocus property, if set to false, the focus state will not be drawn
            uploadButton.setBorderPainted(false);  // Set thr paintBorder property, if set to false, the border state will not be drawn
            uploadButton.setForeground(new Color(0xFFFFFF));
            uploadButton.setBackground(new Color(0x4169E1));
            uploadButton.setFont(new Font("微软雅黑", Font.PLAIN,14));
            uploadButton.setBounds(50, 50, 100, 50);

            UI.add(textArea);
            UI.add(uploadButton);

            /*
            uploadButton.addActionListener((e ->
            {
                try
                {

                    // Send
                    String QUEUE_NAME = "test";
                    String host = "8.130.42.107";  // RabbitMQ host IP
                    int port = 5672;  // RabbitMQ host port
                    String virtualHost = "/";
                    String userName = "admin";
                    String userPassword = "123456";

                    Producer producer = new Producer(QUEUE_NAME, docName, document, host, port, virtualHost,
                                                     userName, userPassword, userID, modelType, update);
                    boolean flag = producer.send();

                    if(flag)
                    {
                        System.out.println("111");
                        String message1 = "模型已发送至RabbitMQ，等待服务器回应" + "\r\n";
                        textArea.append(message1);


                        // Create a new thread to receive the server acknowledgement message
                        Runnable socketTask = () ->
                        {
                            try
                            {
                                ServerSocket serverSocket = new ServerSocket(9999);
                                Socket socket = serverSocket.accept();
                                InputStream inputStream = socket.getInputStream();

                                byte[] buf = new byte[1024];
                                int readLine;
                                while ((readLine = inputStream.read(buf)) != -1)
                                {
                                    String confirmMessage = new String(buf, 0, readLine);
                                    if(confirmMessage.equals("success"))
                                    {
                                        String message2 = "模型同步成功" + "\r\n";
                                        textArea.append(message2);
                                    }
                                    else
                                    {
                                        String message3 = "模型同步失败" + "\r\n";
                                        textArea.append(message3);
                                    }
                                }
                                inputStream.close();
                                socket.close();
                                serverSocket.close();
                            }
                            catch(Exception ex)
                            {
                                throw new RuntimeException(ex);
                            }
                        };
                        new Thread(socketTask).start();


                        final Listen listen = new Listen();
                        // 创建EventLoopGroup
                        EventLoopGroup bossGroup = new NioEventLoopGroup();
                        EventLoopGroup workerGroup = new NioEventLoopGroup();
                        // 创建EventLoopGroup
                        ServerBootstrap b = new ServerBootstrap();
                        b.group(bossGroup, workerGroup)
                                .channel(NioServerSocketChannel.class)  //指定所使用的NIO传输Channel
                                .localAddress(new InetSocketAddress(9999))  //使用指定的端口设置套接字地址
                                .childHandler(new ChannelInitializer<SocketChannel>()  // 添加一个EchoServerHandler到Channel的ChannelPipeline
                                {
                                    @Override
                                    protected void initChannel(SocketChannel socketChannel) throws Exception
                                    {
                                        socketChannel.pipeline().addLast(listen);
                                    }
                                });

                        try
                        {
                            ChannelFuture f = b.bind().sync();  // 异步地绑定服务器;调用sync方法阻塞等待直到绑定完成
                            f.channel().closeFuture().sync();  // 获取Channel的CloseFuture，并且阻塞当前线程直到它完成

                            String message = listen.getMessage();
                            if(message.equals("success"))
                            {
                                String message2 = "模型同步成功" + "\r\n";
                                textArea.append(message2);
                            }
                            else
                            {
                                String message3 = "模型同步失败" + "\r\n";
                                textArea.append(message3);
                            }
                        }
                        catch (InterruptedException ex)
                        {
                            ex.printStackTrace();
                        }
                        finally
                        {
                            // 关闭EventLoopGroup，释放所有的资源
                            bossGroup.shutdownGracefully();
                            workerGroup.shutdownGracefully();
                        }
                    }
                    else
                    {
                        String message4 = "模型同步失败" + "\r\n";
                        textArea.append(message4);
                    }
                }
                catch (Exception ex)
                {
                    throw new RuntimeException(ex);
                }
            }));*/
        }
        else
        {
            JOptionPane.showMessageDialog(null, "请打开一个Magic Draw项目以实现同步", "云端同步", JOptionPane.PLAIN_MESSAGE);
        }
    }

    // Visit owned elements
    private void visitChildren(Element root, org.dom4j.Element rootItem)
    {
        ArrayList<Element> all = new ArrayList<>();
        all.add(root);

        // Get the root model element information
        String rootID = root.getID();
        String rootClassType = ClassTypes.getShortName(root.getClassType());
        String rootHumanType = root.getHumanType();
        String rootName = ((NamedElement)root).getName();
        // Write into XML file
        org.dom4j.Element magicDrawRootItem = rootItem.addElement(rootClassType).addAttribute("id", rootID)
                                                                                .addAttribute("type", rootHumanType)
                                                                                .addAttribute("name", rootName);

        // if current element has children, list will be increased.
        for (int i = 0; i < all.size(); i++)
        {
            Element current = all.get(i);
            try
            {
                // let's perform some action with this element in visitor.
                current.accept(visitor);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            // add all children into end of this list, so it emulates recursion.
            Collection<Element> childrenElement = current.getOwnedElement();
            all.addAll(childrenElement);

            for(Element childElement: childrenElement)
            {
                // Get the  childElement's parent element information
                String childParentID = childElement.getObjectParent().getID();
                String childParentClassType = ClassTypes.getShortName(childElement.getObjectParent().getClassType());

                // if childElement's parent element is the root
                if(Objects.equals(childParentID, rootID))
                {
                    // Get the child model element information
                    String childID = childElement.getID();
                    String childClassType = ClassTypes.getShortName(childElement.getClassType());
                    String childHumanType = childElement.getHumanType();
                    String childName = ((NamedElement)childElement).getName();
                    // Write into XML file
                    org.dom4j.Element childItem = magicDrawRootItem.addElement(childClassType).addAttribute("id", childID)
                                                                                              .addAttribute("type", childHumanType)
                                                                                              .addAttribute("name", childName);
                }
                else
                {
                    // Get all the context under the root Node
                    String select1 = "/User/" + rootClassType;
                    String select2 = "//" + childParentClassType + "[@id=" + "'" + childParentID + "'" + "]";

                    org.dom4j.Node rootNode = document.selectSingleNode(select1);
                    List<org.dom4j.Node> childParent = rootNode.selectNodes(select2);
                    org.dom4j.Element childParentItem = (org.dom4j.Element) childParent.get(0);

                    String childClassType = ClassTypes.getShortName(childElement.getClassType());
                    switch(childClassType)
                    {
                        case "ControlFlow":
                            ControlFlow elementControlFlow = (ControlFlow)childElement;
                            writeControlFlow2XML(childParentItem, elementControlFlow);
                            break;

                        case "OpaqueAction":
                            OpaqueAction elementOpaqueAction = (OpaqueAction)childElement;
                            writeOpaqueAction2XML(childParentItem, elementOpaqueAction);
                            break;

                        case "LiteralUnlimitedNatural":
                            LiteralUnlimitedNatural elementLiteralUnlimitedNatural = (LiteralUnlimitedNatural)childElement;
                            writeLiteralUnlimitedNatural2XML(childParentItem, elementLiteralUnlimitedNatural);
                            break;

                        case "Trigger":
                            Trigger elementTrigger = (Trigger)childElement;
                            writeTrigger2XML(childParentItem, elementTrigger);
                            break;

                        case "StringTaggedValue":
                            StringTaggedValue elementStringTaggedValue = (StringTaggedValue)childElement;
                            writeStringTaggedValue2XML(childParentItem, elementStringTaggedValue);
                            break;

                        default:
                            writeElement2XML(childParentItem, childElement);
                    }
                }
            }
        }
    }

    /*
     * For different types of elements,
     * add the unique attributes to the XML statement
     */
    private void writeElement2XML(org.dom4j.Element root, Element element)
    {
        // Get the child model element information
        String elementID = element.getID();
        String elementClassType = ClassTypes.getShortName(element.getClassType());
        String elementHumanType = element.getHumanType();
        String elementName = element.getHumanName();

        // Write into XML file
        root.addElement(elementClassType).addAttribute("id", elementID)
                                         .addAttribute("type", elementHumanType)
                                         .addAttribute("name", elementName);
    }

    // StringTaggedValue
    private void writeStringTaggedValue2XML(org.dom4j.Element root, StringTaggedValue element)
    {
        // Get the child model element information
        String elementID = element.getID();
        String elementClassType = ClassTypes.getShortName(element.getClassType());
        String elementHumanName = element.getHumanName();

        // Write into XML file
        root.addElement(elementClassType).addAttribute("id", elementID).addAttribute("type", elementHumanName);
    }

    // ControlFlow
    private void writeControlFlow2XML(org.dom4j.Element root, ControlFlow element)
    {
        // ControlFlow element has source and target, get them
        ActivityNode source = element.getSource();
        ActivityNode target = element.getTarget();

        // Get all the  element information
        String elementID = element.getID();
        String elementClassType = ClassTypes.getShortName(element.getClassType());
        String elementHumanType = element.getHumanType();
        String elementName = element.getName();
        String sourceID = null;
        String targetID = null;
        if (source != null)
        {
            sourceID = source.getID();
        }
        if (target != null)
        {
            targetID = target.getID();
        }

        // Write into XML file
        root.addElement(elementClassType).addAttribute("id", elementID)
                                         .addAttribute("type", elementHumanType)
                                         .addAttribute("name", elementName)
                                         .addAttribute("source", sourceID)
                                         .addAttribute("target", targetID);
    }


    // OpaqueAction
    private void writeOpaqueAction2XML(org.dom4j.Element root, OpaqueAction element)
    {
        // Get all the  element information
        List<String> body, language;
        String elementBody = null;
        String elementLanguage = null;
        String elementID = element.getID();
        String elementClassType = ClassTypes.getShortName(element.getClassType());
        String elementHumanType = element.getHumanType();
        String elementName = element.getName();

        // OpaqueAction element has body and language, get them
        boolean hasBody = element.hasBody();
        boolean hasLanguage = element.hasLanguage();
        if(hasBody)
        {
            body = element.getBody();
            elementBody = StringUtils.join(body, ";");
        }
        if(hasLanguage)
        {
            language = element.getLanguage();
            elementLanguage = StringUtils.join(language, ";");
        }

        // Write into XML file
        root.addElement(elementClassType).addAttribute("id", elementID)
                                         .addAttribute("type", elementHumanType)
                                         .addAttribute("name", elementName)
                                         .addAttribute("body", elementBody)
                                         .addAttribute("language", elementLanguage);
    }

    // LiteralUnlimitedNatural
    private void writeLiteralUnlimitedNatural2XML(org.dom4j.Element root, LiteralUnlimitedNatural element)
    {
        // Get all the  element information
        String elementID = element.getID();
        String elementClassType = ClassTypes.getShortName(element.getClassType());
        String elementHumanType = element.getHumanType();
        String elementName = element.getName();
        String weight = String.valueOf(element.getValue());

        // Write into XML file
        root.addElement(elementClassType).addAttribute("id", elementID)
                                         .addAttribute("type", elementHumanType)
                                         .addAttribute("name", elementName)
                                         .addAttribute("value", weight);
    }

    // Trigger
    private void writeTrigger2XML(org.dom4j.Element root, Trigger element)
    {
        // Get all the  element information
        String elementID = element.getID();
        String elementClassType = ClassTypes.getShortName(element.getClassType());
        String elementHumanType = element.getHumanType();
        String elementName = element.getName();
        String elementEventType = ClassTypes.getShortName(Objects.requireNonNull(element.getEvent()).getClassType());

        // Write into XML file
        org.dom4j.Element item = root.addElement(elementClassType).addAttribute("id", elementID)
                                                                  .addAttribute("type", elementHumanType)
                                                                  .addAttribute("name", elementName)
                                                                  .addAttribute("event", elementEventType);

        /*
         * For different types of events,
         * add the unique attributes to the XML statement
         */
        if(Objects.equals(elementEventType, "TimeEvent"))
        {
            TimeEvent timeEvent = (TimeEvent)element.getEvent();
            TimeExpression when = timeEvent.getWhen();
            String time = null;
            if(when != null)
            {
                LiteralString expr = (LiteralString)when.getExpr();
                if(expr != null)
                {
                    time = expr.getValue();
                }
            }
            item.addAttribute("when", time);
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