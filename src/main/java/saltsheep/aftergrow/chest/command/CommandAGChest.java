package saltsheep.aftergrow.chest.command;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import saltsheep.aftergrow.chest.handle.DataContainer;
import saltsheep.aftergrow.chest.handle.ServerHandler;

public class CommandAGChest extends CommandBase {

	@Override
	public String getName() {
		return "agchest";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "§c/agchest mark 标记箱子(需游戏内玩家执行)\n§c/agchest unmark 去除箱子标记(需游戏内玩家执行)\n§c/agchest reset (可选:玩家名) 重置玩家的箱子记录";
	}
	
	@Override
	public int getRequiredPermissionLevel()
    {
        return 3;
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		Entity entity = sender.getCommandSenderEntity();
		boolean isPlayer = entity instanceof EntityPlayer;
		if(args.length==1&&args[0].equalsIgnoreCase("mark")&&isPlayer) {
			if(ServerHandler.isMarking.containsKey(((EntityPlayer)sender.getCommandSenderEntity()))) {
				sender.sendMessage(new TextComponentString("You are already in marking state!"));
				return;
			}
			ServerHandler.isMarking.put((EntityPlayer) sender.getCommandSenderEntity(), "COMMAND_MARK");
			sender.sendMessage(new TextComponentString("Right click a chest,and the chest will be marked."));
			return;
		}else if(args.length==1&&args[0].equalsIgnoreCase("unmark")&&isPlayer) {
			if(ServerHandler.isMarking.containsKey(((EntityPlayer)sender.getCommandSenderEntity()))) {
				sender.sendMessage(new TextComponentString("You are already in marking state!"));
				return;
			}
			ServerHandler.isMarking.put((EntityPlayer) sender.getCommandSenderEntity(), "COMMAND_REMOVE");
			sender.sendMessage(new TextComponentString("Right click a chest,and the chest's mark will be removed if it has mark."));
			return;
		}else label:if(args.length<=2&&args.length>=1&&args[0].equalsIgnoreCase("reset")) {
			EntityPlayer player = isPlayer? (EntityPlayer)entity:null;
			if(args.length==2)
				player = server.getPlayerList().getPlayerByUsername(args[1]);
			if(player==null)
				break label;
			DataContainer.resetTaken(player);
			sender.sendMessage(new TextComponentString("Reset all chest for player:"+player.getName()+" successful."));
			player.sendMessage(new TextComponentString("Reset all chest successful."));
			return;
		}
		sender.sendMessage(new TextComponentString(this.getUsage(sender)));
	}
	
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos){
        if(args.length==1) 
        	return getListOfStringsMatchingLastWord(args, "mark","unmark","reset");
        if(args.length==2&&args[0].equalsIgnoreCase("reset"))
        	return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        return super.getTabCompletions(server, sender, args, targetPos);
    }
}
