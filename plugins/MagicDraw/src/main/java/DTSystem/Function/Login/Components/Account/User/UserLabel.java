package DTSystem.Function.Login.Components.Account.User;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;

public class UserLabel extends JLabel
{
    private static final int USER_X = 35;
    private static final int USER_Y = 70;
    private static final int USER_WIDTH = 100;
    private static final int USER_HEIGHT = 50;
    private static final String CONTEXT = "账号  ";

    public UserLabel()
    {
        super(CONTEXT);
        setForeground(new Color(0x000000));
        setFont(new Font("微软雅黑", Font.PLAIN,18));
        setBounds(USER_X, USER_Y, USER_WIDTH, USER_HEIGHT);
    }
}
