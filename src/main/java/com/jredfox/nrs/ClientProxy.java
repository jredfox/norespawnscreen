package com.jredfox.nrs;

import net.minecraft.client.gui.GuiGameOver;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends Proxy {
	
	@Override
	public void preinit()
	{
		 MinecraftForge.EVENT_BUS.register(this);
	}
	
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void respawn(GuiOpenEvent e)
    {
    	if(e.getGui() instanceof GuiGameOver)
    	{
    		e.setGui(null);
    	}
    }

}
