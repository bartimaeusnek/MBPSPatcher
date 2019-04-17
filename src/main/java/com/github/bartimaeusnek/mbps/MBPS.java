package com.github.bartimaeusnek.mbps;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "mpbs",version = "0.0.1",name = "BPS Patcher Mod")
public class MBPS {

    @Mod.EventHandler
    public static void onPreInit(FMLPreInitializationEvent event){
//        if(MBPSPlugin.wasPatched)
//            FMLCommonHandler.instance().exitJava(0,false);
    }
}
