package com.github.bartimaeusnek.mbps;

import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.Name("mbps")
public class MBPSPlugin implements IFMLLoadingPlugin {
    public static File minecraftDir = null;
    public static boolean wasPatched = false;

    public MBPSPlugin() {
        //Injection Code taken from CodeChickenLib
        if (minecraftDir != null)
            return;//get called twice, once for IFMLCallHook
        minecraftDir = (File) FMLInjectionData.data()[6];

        Configuration c = new Configuration(new File(new File(minecraftDir, "MBPS"), "config.cfg"));
        String[] tmp = c.get("Patches", "Patch/Original List", new String[0], "FIRST Name of Patch, NEW LINE, Name of Original").getStringList();
        String[] update = c.get("Patches", "UpdateList", new String[0], "Place the name of the Files here, that get Updated").getStringList();
        Map<String, String> patchmap = new LinkedHashMap<String, String>();
        if (tmp.length != 0) {
            for (int i = 1; i < tmp.length; i++) {
                if (i % 2 == 1) {
                    patchmap.put(tmp[i - 1], tmp[i]);
                }
            }
        }

        for (String s : patchmap.keySet()) {
            File nu = new File(new File(minecraftDir, "mods"), s.substring(0, s.length() - 4) + ".jar");
            if (!(nu.exists())) {
                File patch = new File(new File(new File(minecraftDir, "MBPS"), "Patches"), s);
                File original = new File(new File(new File(minecraftDir, "MBPS"), "Originals"), patchmap.get(s));
                try {
                    new BPSpatcher(patch, original, nu).patch();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                wasPatched = true;
            }
        }
        if (update.length != 0) {
            for (String s : update) {
                if (s.contains(".bps") || s.contains(".BPS"))
                    s = s.substring(0, s.length() - 4) + ".jar";
                if (!s.contains("."))
                    s = s + ".jar";
                File nu = new File(new File(minecraftDir, "mods"), s);
                if (nu.exists())
                    nu.delete();
            }
        }

        if (c.hasChanged())
            c.save();
    }


    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
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
