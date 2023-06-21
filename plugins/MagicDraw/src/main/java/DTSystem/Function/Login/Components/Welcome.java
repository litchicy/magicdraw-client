package DTSystem.Function.Login.Components;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;

public class Welcome extends JLabel
{
    private static final int MESSAGE_X = 75;
    private static final int MESSAGE_Y = 20;
    private static final int WIDTH = 400;
    private static final int HEIGHT = 30;
    private static final String CONTEXT = "欢迎使用数字线索系统";

    public Welcome()
    {
        super(CONTEXT);
        setFont(new Font("微软雅黑", Font.PLAIN,24));
        setForeground(new Color(0x000000));
        setBounds(MESSAGE_X, MESSAGE_Y, WIDTH, HEIGHT);
    }
}
