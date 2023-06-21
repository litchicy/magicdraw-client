package DTSystem.Function.Login.Components.Account.Password;

import javax.swing.JPasswordField;
import java.awt.*;

public class PasswordTextField extends JPasswordField
{
    private static final int TEXT_X = 100;
    private static final int TEXT_Y = 135;
    private static final int TEXT_WIDTH = 250;
    private static final int TEXT_HEIGHT = 40;

    public PasswordTextField()
    {
        super(20);
        setEchoChar('*');
        setForeground(new Color(0x000000));
        setFont(new Font("微软雅黑", Font.PLAIN,18));
        setBounds(TEXT_X, TEXT_Y, TEXT_WIDTH, TEXT_HEIGHT);
    }
}
