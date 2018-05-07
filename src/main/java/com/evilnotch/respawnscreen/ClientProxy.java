package com.evilnotch.respawnscreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends ServerProxy{
	
	@Override
	public void preinit(){
		MinecraftForge.EVENT_BUS.register(this);
	}
	
    @SubscribeEvent
    public void gui(GuiOpenEvent e)
    {
        if(e.getGui() == null)
            return;
        if(e.getGui().getClass() == GuiGameOver.class)
        {
        	if(Minecraft.getMinecraft().player.isDead && ConfigRespawn.slowDeath || !ConfigRespawn.slowDeath)
        	{
        		Minecraft.getMinecraft().player.respawnPlayer();
        		System.out.println("here death screen");
        	}
            e.setCanceled(true);
        }
    }

}
