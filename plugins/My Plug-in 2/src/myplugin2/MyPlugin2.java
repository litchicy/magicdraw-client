package myplugin2;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.plugins.Plugin;

public class MyPlugin2 extends Plugin
{
	@Override
	public void init()
	{
		Application.getInstance().getGUILog().showMessage("My Plug-in 2 initialized.");
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
}
