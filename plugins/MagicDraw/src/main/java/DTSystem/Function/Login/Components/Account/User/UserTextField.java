package DTSystem.Function.Login.Components.Account.User;

import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Font;

public class UserTextField extends JTextField
{
    private static final int TEXT_X = 100;
    private static final int TEXT_Y = 75;
    private static final int TEXT_WIDTH = 250;
    private static final int TEXT_HEIGHT = 40;

    public UserTextField()
    {
        super(20);
        setForeground(new Color(0x000000));
        setFont(new Font("微软雅黑", Font.PLAIN,18));
        setBounds(TEXT_X, TEXT_Y, TEXT_WIDTH, TEXT_HEIGHT);
    }
}
