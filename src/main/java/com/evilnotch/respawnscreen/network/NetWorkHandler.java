package com.evilnotch.respawnscreen.network;

import com.evilnotch.respawnscreen.MainJava;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetWorkHandler {
	
	public static SimpleNetworkWrapper INSTANCE;
	static int networkid = 0;
	
	public static void init()
	{
		INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(MainJava.MODID);
		INSTANCE.registerMessage(PacketParticle.class, PacketParticle.class, networkid++, Side.CLIENT);
	}
	

}
