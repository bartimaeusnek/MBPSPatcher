package com.github.bartimaeusnek.mbps;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.misc.URLClassPath;
import sun.net.util.URLUtil;

import javax.swing.*;
import java.awt.*;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.SortingIndex(-999999999) //load this before anything else
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.Name("mbps")
public class MBPSPlugin implements IFMLLoadingPlugin {
    static File minecraftDir = null;
    static boolean wasPatched = false;
    static Map<String, String> patchmap = new LinkedHashMap<String, String>();
    Logger logger = LogManager.getLogger("BPS Patcher");

    public MBPSPlugin() throws InterruptedException {
        //Injection Code taken from CodeChickenLib
        if (minecraftDir != null)
            return;//get called twice, once for IFMLCallHook
        minecraftDir = (File) FMLInjectionData.data()[6];
        SpecialDepLoader.load();
        Configuration c = new Configuration(new File(new File(minecraftDir, "MBPS"), "config.cfg"));
        String[] tmp = c.get("Patches", "Patch/Original List", new String[0], "FIRST Name of Patch, NEW LINE, Name of Original").getStringList();

        if (tmp.length != 0) {
            for (int i = 1; i < tmp.length; i++) {
                if (i % 2 == 1) {
                    patchmap.put(tmp[i - 1], tmp[i]);
                }
            }
        }
        for (String s : patchmap.keySet()) {
            File original = new File(new File(minecraftDir, "mods"), patchmap.get(s));
            unloadMod(original);
        }
        Thread t = new Thread(new actualPatch());
        t.start();
        t.join();
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

    /**
     * Taken from CodeChickenCore
     * @param mod
     */
    private void unloadMod(File mod){
        try {
            ClassLoader cl = MBPSPlugin.class.getClassLoader();
            URL url = mod.toURI().toURL();
            Field f_ucp = URLClassLoader.class.getDeclaredField("ucp");
            Field f_loaders = URLClassPath.class.getDeclaredField("loaders");
            Field f_lmap = URLClassPath.class.getDeclaredField("lmap");
            f_ucp.setAccessible(true);
            f_loaders.setAccessible(true);
            f_lmap.setAccessible(true);
            URLClassPath ucp = (URLClassPath)f_ucp.get(cl);
            Closeable loader = (Closeable)((Map)f_lmap.get(ucp)).remove(URLUtil.urlNoFragString(url));
            if (loader != null) {
                loader.close();
                ((List)f_loaders.get(ucp)).remove(loader);
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }
    }

    class actualPatch implements Runnable{

        /**
         * Taken from CodeChickenCore
         * @param mod
         */
        private void deleteMod(File mod) {
            if (!mod.delete()) {
                unloadMod(mod);
                if (!mod.delete()) {
                    mod.deleteOnExit();
                    String msg = "BPS Patcher Mod was unable to delete file " + mod.getPath() + " the game will now try to delete it on exit. If this dialog appears again, delete it manually.";
                    System.err.println(msg);
                    if (!GraphicsEnvironment.isHeadless()) {
                        JOptionPane.showMessageDialog((Component)null, msg, "An update error has occured", 0);
                    }
                }
            }
        }

        @Override
        public void run() {
            Configuration c = new Configuration(new File(new File(minecraftDir, "MBPS"), "config.cfg"));
            String[] update = c.get("Patches", "UpdateList", new String[0], "Place the name of the Files here, that get Updated").getStringList();

            int done = 0;
            for (String s : patchmap.keySet()) {
                File nu = new File(new File(minecraftDir, "mods"), s.substring(0, s.length() - 4) + ".jar");
                if (!(nu.exists())) {
                    File patch = new File(new File(minecraftDir, "MBPS"), s);
                    File original = new File(new File(minecraftDir, "mods"), patchmap.get(s));
                    boolean noerror;
                    try {
                        logger.info("Patching "+original.getName()+" to "+patch.getName().substring(0,patch.getName().length() - 4)+".jar!");
                        unloadMod(original);
                        noerror = new BPSpatcher(patch, original, nu).patch();
                        done++;
                    } catch (IOException e) {
                        e.printStackTrace();
                        noerror = false;
                    }
                    if (noerror) {
                        logger.info(done + " of " + patchmap.keySet().size() + " patched!");
                        wasPatched = true;
                        deleteMod(original);
                    }
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
            if(wasPatched) {
                JOptionPane pane = new JOptionPane( "Some mods have been patched! Minecraft will now shutdown! This is intended and not an Error!",JOptionPane.INFORMATION_MESSAGE);
                JDialog dialog = pane.createDialog("BPS Patcher Mod");
                pane.selectInitialValue();
                dialog.setAlwaysOnTop(true);
                dialog.show();
                dialog.dispose();
                pane.grabFocus();
                pane.setWantsInput(true);
                pane.setVisible(true);
                FMLLog.bigWarning("Some mods have been patched! Minecraft will now shutdown! This is intended and not an Error!");
                Method exit = null;
                try {
                    exit = Class.forName("java.lang.Shutdown").getDeclaredMethod("exit", int.class);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                ((Method) exit).setAccessible(true);
                try {
                    exit.invoke(null,1);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
