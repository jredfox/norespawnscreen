package com.evilnotch.respawnscreen;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import com.EvilNotch.lib.Api.MCPMappings;
import com.EvilNotch.lib.Api.ReflectionUtil;
import com.EvilNotch.lib.minecraft.EntityUtil;
import com.evilnotch.respawnscreen.network.NetWorkHandler;
import com.evilnotch.respawnscreen.network.PacketParticle;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketCombatEvent;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

@Mod(modid = MainJava.MODID, name = MainJava.NAME, version = MainJava.VERSION, dependencies = "required-after:evilnotchlib")
public class MainJava
{
    public static final String MODID = "norespawnscreen";
    public static final String NAME = "No Respawn Screen";
    public static final String VERSION = "1.2";
    @SidedProxy(clientSide = "com.evilnotch.respawnscreen.ClientProxy", serverSide = "com.evilnotch.respawnscreen.ServerProxy")
	public static ServerProxy proxy;
    public static Method spawnShoulderEntities;
    public static Method destroyVanishingCursedItems;
    public static Method setFlag;
    
    public static String recentlyHit;
	private static String scoreValue;
	public static String rnd;
	public static String attackingPlayer;
	public static Method getExperiencePoints;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	ConfigRespawn.loadConfig(new File(event.getModConfigurationDirectory(),"norespawnscreen.cfg"));
    	proxy.preinit();
    	MinecraftForge.EVENT_BUS.register(this);
    	NetWorkHandler.init();
        try 
        {
			spawnShoulderEntities = EntityPlayer.class.getDeclaredMethod(MCPMappings.getMethod(EntityPlayer.class,"spawnShoulderEntities"));
			destroyVanishingCursedItems = EntityPlayer.class.getDeclaredMethod(MCPMappings.getMethod(EntityPlayer.class,"destroyVanishingCursedItems"));
			setFlag = Entity.class.getDeclaredMethod(MCPMappings.getMethod(Entity.class, "setFlag"), int.class,boolean.class);
			getExperiencePoints = EntityLivingBase.class.getDeclaredMethod(MCPMappings.getMethod(EntityLivingBase.class, "getExperiencePoints"), EntityPlayer.class);
					
			spawnShoulderEntities.setAccessible(true);
			destroyVanishingCursedItems.setAccessible(true);
			setFlag.setAccessible(true);
			getExperiencePoints.setAccessible(true);
			
			recentlyHit = MCPMappings.getField(EntityLivingBase.class, "recentlyHit");
			scoreValue = MCPMappings.getField(EntityLivingBase.class, "scoreValue");
			rnd = MCPMappings.getField(Entity.class, "rand");
			attackingPlayer = MCPMappings.getField(EntityLivingBase.class, "attackingPlayer");
		} 
        catch (Throwable t)
        {
			t.printStackTrace();
		}
    }
    
    /**
     * fire fixer for auto respawn
     */
    @SubscribeEvent
    public void onFTick(TickEvent.PlayerTickEvent e)
    {
    	if(e.phase != Phase.END || e.player.world.isRemote || e.player.ticksExisted > 20)
    		return;
    	if( ((EntityPlayerMP)e.player).getStatFile().readStat(StatList.TIME_SINCE_DEATH) <= 20)
    	{
    		e.player.extinguish();
    	}
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDeath(LivingDeathEvent e)
    {
        if(e.getEntity() instanceof EntityPlayerMP)
        {
        	if(ConfigRespawn.slowDeath)
        		return;
            
        	EntityPlayerMP player = (EntityPlayerMP)e.getEntity();
        	try
        	{
        		killPlayer(player, e.getSource());
        	}
        	catch(Throwable t)
        	{
        		t.printStackTrace();
        	}
            e.setCanceled(true);
            
            World oldWorld = player.world;
            player.dismountRidingEntity();
        	player.world.removeEntityDangerously(player);
            if(oldWorld.provider.getDimension() == 1)
            {
            	EntityUtil.removeDragonBars(oldWorld);
            }
            EntityPlayerMP newPlayer = player.getServer().getPlayerList().recreatePlayerEntity(player, player.dimension, false);
            player.connection.player = newPlayer;
            
            if (newPlayer.mcServer.isHardcore())
            {
            	newPlayer.setGameType(GameType.SPECTATOR);
            	newPlayer.getServerWorld().getGameRules().setOrCreateGameRule("spectatorsGenerateChunks", "false");
            }
        }
    }
    
	/**
	 * kill player during kill event without it causing it to repost the event
	 */
	public static void killPlayer(EntityPlayerMP player,DamageSource cause) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
	{
        boolean flag = player.world.getGameRules().getBoolean("showDeathMessages");
        player.isDead = true;
        player.connection.sendPacket(new SPacketCombatEvent(player.getCombatTracker(), SPacketCombatEvent.Event.ENTITY_DIED, flag));

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
        dropXP(player);
        NetWorkHandler.INSTANCE.sendToDimension(new PacketParticle(EnumParticleTypes.EXPLOSION_NORMAL,player.posX,player.posY,player.posZ),player.dimension);
	}

	public static void spawnParticles(Entity e,int particleId,double x, double y, double z) 
	{
        for (int k = 0; k < 20; ++k)
        {
        	Random rand = getRND(e);
            double d2 = rand.nextGaussian() * 0.02D;
            double d0 = rand.nextGaussian() * 0.02D;
            double d1 = rand.nextGaussian() * 0.02D;
            e.world.spawnParticle(EnumParticleTypes.getParticleFromId(particleId), x + (double)(rand.nextFloat() * e.width * 2.0F) - (double)e.width, y + (double)(rand.nextFloat() * e.height), e.posZ + (double)(rand.nextFloat() * e.width * 2.0F) - (double)e.width, d2, d0, d1);
        }
	}

	public static Random getRND(Entity e) {
		return (Random) ReflectionUtil.getObject(e, Entity.class, rnd);
	}
	public static EntityPlayer getAttackingPlayer(EntityLivingBase e) {
		return (EntityPlayer) ReflectionUtil.getObject(e, EntityLivingBase.class, attackingPlayer);
	}

	public static void dropXP(EntityLivingBase base) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
	{
		EntityPlayer attackingPlayer = getAttackingPlayer(base);
        int i = getExperiencePoints(base,attackingPlayer);
        i = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(base, attackingPlayer, i);
        while (i > 0)
        {
            int j = EntityXPOrb.getXPSplit(i);
            i -= j;
            base.world.spawnEntity(new EntityXPOrb(base.world, base.posX, base.posY, base.posZ, j));
        }
	}

	public static int getExperiencePoints(EntityLivingBase base, EntityPlayer attackingPlayer) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
	{
		getExperiencePoints.setAccessible(true);
		return (Integer) getExperiencePoints.invoke(base,attackingPlayer);
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
