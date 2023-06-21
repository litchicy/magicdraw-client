package DTSystem;

import DTSystem.Function.UpLoad.UpLoad;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.plugins.Plugin;

import DTSystem.MainMenu.MainMenuConfigurator;
import DTSystem.Function.Login.Login;

import java.net.UnknownHostException;

public class Main extends Plugin
{
    /**
     * Adding actions on plugin init.
     */
    @Override
    public void init()
    {
        ActionsConfiguratorsManager manager = ActionsConfiguratorsManager.getInstance();
        try
        {
            manager.addMainMenuConfigurator(new MainMenuConfigurator(separatedActions()));  // adding actions with separator
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean close()
    {
        return true;
    }

    @Override
    public boolean isSupported()
    {
        return true;
    }

    /**
     * Creates group of actions. This group is separated from others using menu separator (when it represented in menu).
     * Separator is added for group of actions in one actions category.
     */
    private NMAction separatedActions() throws UnknownHostException
    {
        ActionsCategory category = new ActionsCategory();
        // UpLoad upLoad = new UpLoad("admin", "magicdraw");
        // category.addAction(upLoad);
        category.addAction(new Login(category));
        return category;
    }
}
