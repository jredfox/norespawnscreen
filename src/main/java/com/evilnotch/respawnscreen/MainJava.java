package com.evilnotch.respawnscreen;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.EvilNotch.lib.Api.MCPMappings;
import com.EvilNotch.lib.Api.ReflectionUtil;
import com.EvilNotch.lib.minecraft.EntityUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = MainJava.MODID, name = MainJava.NAME, version = MainJava.VERSION,acceptableRemoteVersions = "*", dependencies = "required-after:evilnotchlib")
public class MainJava
{
    public static final String MODID = "norespawnscreen";
    public static final String NAME = "No Respawn Screen";
    public static final String VERSION = "1.1";
    @SidedProxy(clientSide = "com.evilnotch.respawnscreen.ClientProxy", serverSide = "com.evilnotch.respawnscreen.ServerProxy")
	public static ServerProxy proxy;
    public static Method spawnShoulderEntities;
    public static Method destroyVanishingCursedItems;
    public static Method setFlag;
    
    public static String recentlyHit;
	private static String scoreValue;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	ConfigRespawn.loadConfig(new File(event.getModConfigurationDirectory(),"norespawnscreen.cfg"));
    	proxy.preinit();
        MinecraftForge.EVENT_BUS.register(this);
        try 
        {
			spawnShoulderEntities = EntityPlayer.class.getDeclaredMethod(MCPMappings.getMethod(EntityPlayer.class,"spawnShoulderEntities"));
			destroyVanishingCursedItems = EntityPlayer.class.getDeclaredMethod(MCPMappings.getMethod(EntityPlayer.class,"destroyVanishingCursedItems"));
			setFlag = Entity.class.getDeclaredMethod(MCPMappings.getMethod(Entity.class, "setFlag"), int.class,boolean.class);
			
			spawnShoulderEntities.setAccessible(true);
			destroyVanishingCursedItems.setAccessible(true);
			setFlag.setAccessible(true);
			
			recentlyHit = MCPMappings.getField(EntityLivingBase.class, "recentlyHit");
			scoreValue = MCPMappings.getField(EntityLivingBase.class, "scoreValue");
		} 
        catch (Throwable t)
        {
			t.printStackTrace();
		}
    }
    
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onDeath(LivingDeathEvent e)
	{
		if(e.getEntity() instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)e.getEntity();
			if(player.world.provider.getDimension() == 1)
				EntityUtil.removeDragonBars(player.world);
			
			if(ConfigRespawn.slowDeath)
				return;
			if(com.EvilNotch.lib.main.MainJava.isClient)
			{
				if(!Minecraft.getMinecraft().getIntegratedServer().getPublic() )
				{
					System.out.println("Not Open To Lan Using Client Only Code:");
					return;
				}
			}
			
			try
			{
				killPlayer(player,e.getSource());
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
			player.extinguish();
			player.connection.player = player.getServer().getPlayerList().recreatePlayerEntity(player, player.dimension, false);
			e.setCanceled(true);
		}
	}

	/**
	 * kill player during kill event without it causing it to repost the event
	 */
	public static void killPlayer(EntityPlayerMP player,DamageSource cause) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
	{
        boolean flag = player.world.getGameRules().getBoolean("showDeathMessages");
//        player.connection.sendPacket(new SPacketCombatEvent(this.getCombatTracker(), SPacketCombatEvent.Event.ENTITY_DIED, flag));

        if (flag)
        {
            Team team = player.getTeam();

            if (team != null && team.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS)
            {
                if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS)
                {
                	player.mcServer.getPlayerList().sendMessageToAllTeamMembers(player, player.getCombatTracker().getDeathMessage());
                }
                else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM)
                {
                	player.mcServer.getPlayerList().sendMessageToTeamOrAllPlayers(player, player.getCombatTracker().getDeathMessage());
                }
            }
            else
            {
            	player.mcServer.getPlayerList().sendMessage(player.getCombatTracker().getDeathMessage());
            }
        }
        spawnShoulderEntities.setAccessible(true);
        spawnShoulderEntities.invoke(player);

        if (!player.world.getGameRules().getBoolean("keepInventory") && !player.isSpectator())
        {
        	player.captureDrops = true;
        	player.capturedDrops.clear();
        	destroyVanishingCursedItems.setAccessible(true);
        	destroyVanishingCursedItems.invoke(player);
        	player.inventory.dropAllItems();

        	player.captureDrops = false;
            net.minecraftforge.event.entity.player.PlayerDropsEvent event = new net.minecraftforge.event.entity.player.PlayerDropsEvent(player, cause, player.capturedDrops, getRecentlyHit(player) > 0);
            if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
            {
                for (net.minecraft.entity.item.EntityItem item : player.capturedDrops)
                {
                	player.world.spawnEntity(item);
                }
            }
        }

        for (ScoreObjective scoreobjective : player.world.getScoreboard().getObjectivesFromCriteria(IScoreCriteria.DEATH_COUNT))
        {
            Score score = player.getWorldScoreboard().getOrCreateScore(player.getName(), scoreobjective);
            score.incrementScore();
        }

        EntityLivingBase entitylivingbase = player.getAttackingEntity();

        if (entitylivingbase != null)
        {
            EntityList.EntityEggInfo entitylist$entityegginfo = EntityList.ENTITY_EGGS.get(EntityList.getKey(entitylivingbase));

            if (entitylist$entityegginfo != null)
            {
            	player.addStat(entitylist$entityegginfo.entityKilledByStat);
            }

            entitylivingbase.awardKillScore(player, getScore(player), cause);
        }

        player.addStat(StatList.DEATHS);
        player.takeStat(StatList.TIME_SINCE_DEATH);
        player.extinguish();
        setFlag.setAccessible(true);
        setFlag.invoke(player, 0, false);
        player.getCombatTracker().reset();
	}

	public static int getScore(EntityPlayerMP player) 
	{
		return (Integer) ReflectionUtil.getObject(player, EntityLivingBase.class, scoreValue);
	}

	public static int getRecentlyHit(EntityPlayerMP player) 
	{
		return (Integer) ReflectionUtil.getObject(player, EntityLivingBase.class, recentlyHit);
	}
	
}
