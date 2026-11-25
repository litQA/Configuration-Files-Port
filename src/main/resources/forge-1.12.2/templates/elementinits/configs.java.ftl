package ${package}.init;

import ${package}.configuration.*;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;

@Mod.EventBusSubscriber(modid = "${modid}")
public class ${JavaModName}Configs {

	private static boolean initialized;

	public static void init(FMLPreInitializationEvent event) {
		init(event.getSuggestedConfigurationFile().getParentFile());
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		init(Loader.instance().getConfigDir());
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		init(Loader.instance().getConfigDir());
	}

	private static synchronized void init(File configDir) {
		if (initialized)
			return;
		initialized = true;
		<#list configs as config>
		${config.getModElement().getName()}Configuration.init(new File(configDir, "${config.file}.cfg"));
		</#list>
	}

}
