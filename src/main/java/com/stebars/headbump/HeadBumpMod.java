package com.stebars.headbump;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;


@Mod(HeadBumpMod.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class HeadBumpMod {
	public final static String MOD_ID = "headbump";

	public HeadBumpMod() {
		MinecraftForge.EVENT_BUS.register(this);
	}

}