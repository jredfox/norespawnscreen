package com.evilnotch.respawnscreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;

public class ClientProxy extends ServerProxy{
	
	@Override
	public void preinit(){
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public static boolean hasSpawned = false;
	public static EntityPlayer p = null;
    @SubscribeEvent
    public void gui(GuiOpenEvent e)
    {
        if(e.getGui() == null)
            return;
        if(e.getGui().getClass() == GuiGameOver.class)
        {
        	if(p != Minecraft.getMinecraft().player)
        		p = null;
        	if(Minecraft.getMinecraft().player.isDead && p == null)
        	{
        		p = Minecraft.getMinecraft().player;
        		Minecraft.getMinecraft().player.respawnPlayer();
        		System.out.println("fired client only death:" + p.getName());
        	}
            e.setCanceled(true);
        }
    }

}
