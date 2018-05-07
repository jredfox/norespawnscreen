package com.evilnotch.respawnscreen;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class ConfigRespawn {
	
	public static boolean slowDeath = false;
	
	
	public static void loadConfig(File cfgdir)
	{
		Configuration cfg = new Configuration(cfgdir);
		cfg.load();
		cfg.addCustomCategoryComment("general", "making slowdeath to true will disable server functionality requiring all client to have the mod");
		slowDeath = cfg.get("general","slowDeath",false).getBoolean();
		cfg.save();
	}

}
