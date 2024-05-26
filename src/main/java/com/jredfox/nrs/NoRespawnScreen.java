package com.jredfox.nrs;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

@Mod(modid = NoRespawnScreen.MODID, name = NoRespawnScreen.NAME, version = NoRespawnScreen.VERSION, dependencies = "required-before:evilnotchlib@[1.2.3.08,)")
public class NoRespawnScreen
{
    public static final String MODID = "norespawnscreen";
    public static final String NAME = "No Respawn Screen";
    public static final String VERSION = "2.0.0";
	@SidedProxy(clientSide = "com.jredfox.nrs.ClientProxy", serverSide = "com.jredfox.nrs.Proxy")
	public static Proxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
       proxy.preinit();
       MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void loginFix(PlayerLoggedInEvent e)
    {
    	if(!(e.player instanceof EntityPlayerMP))
    		return;
    	
    	EntityPlayerMP p = (EntityPlayerMP) e.player;
    	if(p.isDead || p.getHealth() <= 0.0F)
    	{
    		onDeath((EntityPlayerMP)e.player);
    	}
    }
    
    /**
     * Instant Respawn Hook
     */
    public static void onDeath(EntityPlayerMP p)
    {
    	//forces the xp to drop
		p.deathTime = 19;
		p.connection.update();
		//perform the respawn as if the client sent the packet
		p.connection.processClientStatus(new CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN));
    }
    
}
