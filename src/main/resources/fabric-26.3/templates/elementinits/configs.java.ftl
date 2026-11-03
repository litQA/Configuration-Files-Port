package ${package}.init;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.neoforged.fml.config.ModConfig;

public class ${JavaModName}Configs implements ModInitializer {

	@Override
	public void onInitialize() {
		<#list configs as config>
		ConfigRegistry.INSTANCE.register("${modid}", ModConfig.Type.<#if config.configType == 0>COMMON<#elseif config.configType == 2>CLIENT<#else>SERVER</#if>, ${config.getModElement().getName()}Configuration.SPEC, "${config.file}.toml");
		</#list>
	}

}