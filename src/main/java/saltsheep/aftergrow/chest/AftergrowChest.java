package saltsheep.aftergrow.chest;

import org.apache.logging.log4j.Logger;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;

@Mod(modid = AftergrowChest.MODID, name = AftergrowChest.NAME, version = AftergrowChest.VERSION, useMetadata = true)
public class AftergrowChest
{
    public static final String MODID = "aftergrowchest";
    public static final String NAME = "AftergrowChest";
    public static final String VERSION = "1.0";
    public static AftergrowChest instance;

    private static Logger logger;

    public AftergrowChest() {
    	instance = this;
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
        logger = event.getModLog();
        Proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event){
    	Proxy.init();
    }
    
    @EventHandler
    public static void onServerStarting(FMLServerStartingEvent event){
    	Proxy.onServerStarting(event);
	}
    
    @EventHandler
    public static void onServerStopped(FMLServerStoppedEvent event){
    	Proxy.onServerStopped(event);
	}
    
    public static Logger getLogger() {
    	return logger;
    }
    
    public static MinecraftServer getMCServer() {
    	return FMLCommonHandler.instance().getMinecraftServerInstance();
    }
    
    public static void printError(Throwable error) {
        StringBuilder messages = new StringBuilder();
        for(StackTraceElement stackTrace : error.getStackTrace()) {
            messages.append("\n").append(stackTrace.toString());
        }
        logger.error("Warning!The mod of me(Saltfish_Sheep) meet an error:\n"+"Error Type:"+error.getClass()+"-"+error.getMessage()+"\n"+messages);
    }
    
    public static void info(String str) {
    	logger.info(str);
    }
    
    public static void info(Object obj) {
    	if(obj == null)
    		logger.info("null has such obj.");
    	else
    		logger.info(obj.toString());
    }
    
    
}
