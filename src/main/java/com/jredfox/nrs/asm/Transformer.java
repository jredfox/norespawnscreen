package com.jredfox.nrs.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.evilnotch.lib.api.mcp.MCPSidedString;
import com.evilnotch.lib.asm.ConfigCore;
import com.evilnotch.lib.asm.classwriter.MCWriter;
import com.evilnotch.lib.asm.util.ASMHelper;

import net.minecraft.launchwrapper.IClassTransformer;

public class Transformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) 
	{
		if(transformedName.equals("net.minecraft.entity.player.EntityPlayerMP"))
		{
			try
			{
	            ClassNode classNode = new ClassNode();
	            ClassReader classReader = new ClassReader(basicClass);
	            classReader.accept(classNode, 0);
	            
	            transform(classNode);
	            
	            ClassWriter classWriter = new MCWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
	            classNode.accept(classWriter);
	            
	            byte[] bytes = classWriter.toByteArray();
	            if(ConfigCore.dumpASM)
	            {
	            	ASMHelper.dumpFile(name, bytes);
	            }
	            return bytes;
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
		}
		return basicClass;
	}

	public void transform(ClassNode classNode)
	{
		MethodNode m = ASMHelper.getMethodNode(classNode, new MCPSidedString("onDeath", "func_70645_a").toString(), "(Lnet/minecraft/util/DamageSource;)V");
		InsnList li = new InsnList();
		li.add(new VarInsnNode(Opcodes.ALOAD, 0));
		li.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jredfox/nrs/NoRespawnScreen", "onDeath", "(Lnet/minecraft/entity/player/EntityPlayerMP;)V", false));
		AbstractInsnNode spot = ASMHelper.getLastLabelNode(m, false);
		m.instructions.insert(spot, li);//insert
	}

}
