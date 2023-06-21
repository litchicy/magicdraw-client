package DTSystem.Function.Login.Components.Account;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Font;

public class LoginButton extends JButton
{
    private static final int BUTTON_X = 135;
    private static final int BUTTON_Y = 200;
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 40;
    private static final String CONTEXT = "登录";

    public LoginButton()
    {
        super(CONTEXT);
        setFocusPainted(false);  // Set the paintFocus property, if set to false, the focus state will not be drawn
        setBorderPainted(false);  // Set thr paintBorder property, if set to false, the border state will not be drawn
        setForeground(new Color(0xFFFFFF));
        setBackground(new Color(0x4169E1));
        setFont(new Font("微软雅黑", Font.PLAIN,18));
        setBounds(BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
}
