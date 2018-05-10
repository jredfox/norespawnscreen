package com.evilnotch.respawnscreen.network;

import com.evilnotch.respawnscreen.MainJava;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketParticle extends MessegeBase<PacketParticle> implements IMessage{

    public int particleId;
	public double x;
	public double y;
	public double z;
	
	public PacketParticle(){}

	public PacketParticle(EnumParticleTypes particle, double x,double y,double z)
	{
        this.particleId = particle.getParticleID();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void toBytes(ByteBuf buffer){
        buffer.writeInt(this.particleId);
        buffer.writeDouble(this.x);
        buffer.writeDouble(this.y);
        buffer.writeDouble(this.z);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        this.particleId = buffer.readInt();
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
    }

	@Override
	public void handleClientSide(PacketParticle message, EntityPlayer player) {
		MainJava.spawnParticles(player,message.particleId,message.x,message.y,message.z);
	}

	@Override
	public void handleServerSide(PacketParticle message, EntityPlayer player) {}

}
