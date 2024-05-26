package com.jredfox.nrs.asm;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@IFMLLoadingPlugin.Name("norespawnscreen")
@IFMLLoadingPlugin.SortingIndex(1002)
@IFMLLoadingPlugin.MCVersion("1.12.2")
@TransformerExclusions("com.jredfox.nrs.asm.")
public class Plugin implements IFMLLoadingPlugin{

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] {"com.jredfox.nrs.asm.Transformer"};
    }

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
