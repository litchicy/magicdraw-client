package DTSystem.Function.Login.Components.Account.Password;

import javax.swing.*;
import java.awt.*;

public class PasswordLabel extends JLabel
{
    private static final int PASSWORD_X = 35;
    private static final int PASSWORD_Y = 130;
    private static final int PASSWORD_WIDTH = 100;
    private static final int PASSWORD_HEIGHT = 50;
    private static final String CONTEXT = "密码  ";

    public PasswordLabel()
    {
        super(CONTEXT);
        setForeground(new Color(0x000000));
        setFont(new Font("微软雅黑", Font.PLAIN,18));
        setBounds(PASSWORD_X, PASSWORD_Y, PASSWORD_WIDTH, PASSWORD_HEIGHT);
    }
}
