package ${package}.init;

import ${package}.configuration.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

@Mod.EventBusSubscriber(modid = "${modid}")
public class ${JavaModName}Configs {

	@SubscribeEvent
	public static void register(FMLPreInitializationEvent event) {
		File configDir = event.getSuggestedConfigurationFile().getParentFile();
		<#list configs as config>
		${config.getModElement().getName()}Configuration.init(new File(configDir, "${config.file}.cfg"));
		</#list>
	}

}
