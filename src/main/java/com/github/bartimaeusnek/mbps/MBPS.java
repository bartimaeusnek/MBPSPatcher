package com.github.bartimaeusnek.mbps;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import javax.swing.*;

@Mod(modid = "mpbs", version = "0.0.1", name = "BPS Patcher Mod")
public class MBPS {

    @Mod.EventHandler
    public static void onPreInit(FMLPreInitializationEvent event) {
        if (MBPSPlugin.wasPatched) {
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
            FMLCommonHandler.instance().exitJava(0, false);
        }
    }
}
