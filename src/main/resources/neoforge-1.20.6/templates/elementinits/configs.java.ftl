package ${package}.init;

import fuzs.forgeconfigapiport.neoforge.api.forge.v4.ForgeConfigRegistry;
import ${package}.configuration.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

<#assign commonConfigs = configs?filter(e -> e.configType == 0 || e.configType == 1)>
<#assign clientConfigs = configs?filter(e -> e.configType == 2)>

public class ${JavaModName}Configs {

<#if commonConfigs?has_content>
	@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
	public static class CommonRegistry {
		@SubscribeEvent
		public static void register(FMLConstructModEvent event) {
			event.enqueueWork(() -> {
				<#list commonConfigs as config>
				ForgeConfigRegistry.INSTANCE.register("${modid}", ModConfig.Type.<#if config.configType == 0>COMMON<#else>SERVER</#if>, ${config.getModElement().getName()}Configuration.SPEC, "${config.file}.toml");
				</#list>
			});
		}
	}
</#if>

<#if clientConfigs?has_content>
	@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientRegistry {
		@SubscribeEvent
		public static void register(FMLConstructModEvent event) {
			event.enqueueWork(() -> {
				<#list clientConfigs as config>
				ForgeConfigRegistry.INSTANCE.register("${modid}", ModConfig.Type.CLIENT, ${config.getModElement().getName()}Configuration.SPEC, "${config.file}.toml");
				</#list>
			});
		}
	}
</#if>

}
