package com.evilnotch.respawnscreen.network;

import com.evilnotch.respawnscreen.MainJava;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;

public class PacketParticle extends MessegeBase<PacketParticle>{

    public int particleId;
	public int entid;
	
	public PacketParticle(){}

	public PacketParticle(EnumParticleTypes particle, int id){
		
        this.particleId = particle.getParticleID();
        System.out.println("Server:" + this.particleId);
        this.entid = id;
    }

    @Override
    public void toBytes(ByteBuf buffer){
        buffer.writeInt(this.particleId);
        buffer.writeInt(this.entid);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        this.particleId = buffer.readInt();
        this.entid = buffer.readInt();
    }

	@Override
	public void handleClientSide(PacketParticle message, EntityPlayer player) {
		System.out.println("idEnt:" + this.entid + " idParticle:" + this.particleId);
		Entity e = Minecraft.getMinecraft().player.world.loadedEntityList.get(this.entid);
		if(e != null)
			MainJava.spawnParticles(e,this.particleId);
	}

	@Override
	public void handleServerSide(PacketParticle message, EntityPlayer player) {}

}
