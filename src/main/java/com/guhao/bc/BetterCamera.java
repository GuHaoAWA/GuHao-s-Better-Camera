package com.guhao.bc;

import com.guhao.bc.config.BetterCameraConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BetterCamera.MODID)
public class BetterCamera {
    public static final String MODID = "better_camera";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public BetterCamera(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.CLIENT, BetterCameraConfig.SPEC);
    }

}
