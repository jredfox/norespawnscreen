package com.evilnotch.respawnscreen.network;

import com.evilnotch.respawnscreen.MainJava;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketParticle extends MessegeBase{

    public int particleId;
	public int id;

	public PacketParticle(EnumParticleTypes particle, int id){
        particleId = particle.ordinal();
        this.id = id;
    }

    @Override
    public void toBytes(ByteBuf buffer){
        buffer.writeInt(particleId);
        buffer.writeDouble(this.id);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        particleId = buffer.readInt();
        this.id = buffer.readInt();
    }

	@Override
	public void handleClientSide(IMessage message, EntityPlayer player) 
	{
		if( !(message instanceof PacketParticle) )
			return;
		MainJava.spawnParticles(player,this.particleId);
	}

	@Override
	public void handleServerSide(IMessage message, EntityPlayer player) {}

}
