package ${package}.init;

import ${package}.configuration.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

<#assign commonConfigs = configs?filter(e -> e.configType == 0 || e.configType == 1)>
<#assign clientConfigs = configs?filter(e -> e.configType == 2)>

public class ${JavaModName}Configs {

<#if commonConfigs?has_content>
	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class CommonRegistry {
		@SubscribeEvent
		public static void register(FMLConstructModEvent event) {
			event.enqueueWork(() -> {
				ModContainer container = ModList.get().getModContainerById("${modid}").get();
				<#list commonConfigs as config>
				container.registerConfig(ModConfig.Type.<#if config.configType == 0>COMMON<#else>SERVER</#if>, ${config.getModElement().getName()}Configuration.SPEC, "${config.file}.toml");
				</#list>
			});
		}
	}
</#if>

<#if clientConfigs?has_content>
	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientRegistry {
		@SubscribeEvent
		public static void register(FMLConstructModEvent event) {
			event.enqueueWork(() -> {
				ModContainer container = ModList.get().getModContainerById("${modid}").get();
				<#list clientConfigs as config>
				container.registerConfig(ModConfig.Type.CLIENT, ${config.getModElement().getName()}Configuration.SPEC, "${config.file}.toml");
				</#list>
			});
		}
	}
</#if>

}
