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
    public int entId;
	public double x;
	public double y;
	public double z;
	public float width;
	public float height;
	
	public PacketParticle(){}

	public PacketParticle(EnumParticleTypes particle,int id, double x,double y,double z,float width,float height)
	{
        this.particleId = particle.getParticleID();
        this.entId = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        buffer.writeInt(this.particleId);
        buffer.writeInt(this.entId);
        
        buffer.writeDouble(this.x);
        buffer.writeDouble(this.y);
        buffer.writeDouble(this.z);
        
        buffer.writeFloat(this.width);
        buffer.writeFloat(this.height);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        this.particleId = buffer.readInt();
        this.entId = buffer.readInt();
        
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
        
        this.width = buffer.readFloat();
        this.height = buffer.readFloat();
    }

	@Override
	public void handleClientSide(PacketParticle message, EntityPlayer clientP) {
		Entity e = Minecraft.getMinecraft().world.getEntityByID(message.entId);
		if(e != null)
			MainJava.spawnParticles(e,message.particleId,message.x,message.y,message.z,message.width,message.height);
	}

	@Override
	public void handleServerSide(PacketParticle message, EntityPlayer player) {}

}
