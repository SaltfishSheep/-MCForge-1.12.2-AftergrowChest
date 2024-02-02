package saltsheep.aftergrow.chest;

import com.google.common.collect.Maps;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import saltsheep.aftergrow.chest.command.CommandAGChest;
import saltsheep.aftergrow.chest.handle.DataContainer;
import saltsheep.aftergrow.chest.handle.ServerHandler;

public class Proxy {

	public static void preInit() {
		MinecraftForge.EVENT_BUS.register(new ServerHandler());
	}
	
	public static void init() {
	}
	
	public static void onServerStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandAGChest());
	}

	public static void onServerStopped(FMLServerStoppedEvent event) {
		DataContainer.reset();
		ServerHandler.isMarking = Maps.newHashMap();
	}
	
}
