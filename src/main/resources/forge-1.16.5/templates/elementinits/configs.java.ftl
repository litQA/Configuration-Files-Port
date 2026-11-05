package ${package}.init;

import ${package}.configuration.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = "${modid}", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ${JavaModName}Configs {

	@SubscribeEvent
	public static void register(FMLConstructModEvent event) {
		event.enqueueWork(() -> {
			<#list configs as config>
			ModLoadingContext.get().registerConfig(ModConfig.Type.<#if config.configType == 0>COMMON<#elseif config.configType == 2>CLIENT<#else>SERVER</#if>, ${config.getModElement().getName()}Configuration.SPEC, "${config.file}.toml");
			</#list>
		});
	}

}
