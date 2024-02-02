package saltsheep.aftergrow.chest.handle;

import java.util.Map;
import com.google.common.collect.Maps;

import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import saltsheep.aftergrow.chest.AftergrowChest;
import saltsheep.aftergrow.chest.handle.DataContainer.MarkChestResult;

public class ServerHandler {
	
	public static Map<EntityPlayer,String> isMarking = Maps.newHashMap();
	
	private BlockPos interactPos = null;
	private long interactTick = 0;
	
	@SubscribeEvent
	public void onContainerOpen(PlayerContainerEvent.Open event) {
		if(!event.getEntityPlayer().isServerWorld())
			return;
		if(!(event.getEntityPlayer().openContainer instanceof ContainerChest))
			return;
		if(interactTick == event.getEntityPlayer().world.getTotalWorldTime()&&interactPos!=null) {
			EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
			ContainerChest realChest = (ContainerChest) player.openContainer;
			player.openContainer.removeListener(player);
			player.openContainer = new ContainerChest(player.inventory, DataContainer.get(player.world).getChest(player, realChest, interactPos), player) {
				@Override
				public void onContainerClosed(EntityPlayer playerIn)
			    {
			        super.onContainerClosed(playerIn);
			        //*使箱子关闭动画正常进行
			        realChest.onContainerClosed(playerIn);
			    }
			};
			player.openContainer.windowId = player.currentWindowId;
			player.openContainer.addListener(player);
			interactPos = null;
			interactTick = 0;
		}
	}

	@SubscribeEvent
	public void onInteractBlock(PlayerInteractEvent.RightClickBlock event) {
		if(event.getSide()!=Side.SERVER)
			return;
		DataContainer data = DataContainer.get(event.getEntityPlayer().world);
		if(!(event.getWorld().getBlockState(event.getPos()).getBlock() instanceof BlockChest))
			return;
		if(isMarking.containsKey(event.getEntityPlayer())) {
			//*如果是玩家正在标记箱子的状态
			String cname = isMarking.get(event.getEntityPlayer());
			if(cname.equals("COMMAND_REMOVE")) {
				//*该操作是移除标记
				if(!data.isMarked(event.getPos())) {
					//*箱子没有标记，不可移去
					event.getEntityPlayer().sendMessage(new TextComponentString("The chest isn't marked,you can't remove its mark."));
				}else {
					//*移除箱子标记
					data.unmarkChest(event.getPos());
					event.getEntityPlayer().sendMessage(new TextComponentString("Successful remove chest mark"));
					AftergrowChest.info("Successful remove chest mark,pos:"+event.getPos().toString());
				}
			}else {
				//*标记箱子
				MarkChestResult result = data.markChest(event.getPos());
				event.getEntityPlayer().sendMessage(new TextComponentString(result.message));
				if(result.success)
					AftergrowChest.info("Successful to mark a new chest,pos:"+event.getPos().toString());
			}
			//*退出标记状态
			isMarking.remove(event.getEntityPlayer());
			//*使箱子无法打开
			event.setCanceled(true);
		}else if(data.isMarked(event.getPos())) {
			interactPos = event.getPos();
			interactTick = event.getWorld().getTotalWorldTime();
		}
	}
	
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event) {
		if(DataContainer.get(event.getWorld()).isMarked(event.getPos()))
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event) {
		if(event.getEntityPlayer().world.isRemote)
			return;
		NBTTagCompound playerTag = event.getEntityPlayer().getEntityData();
		playerTag.setTag("AftergrowChestTaken", event.getOriginal().getEntityData().getCompoundTag("AftergrowChestTaken"));
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event) {
		if(event.side!=Side.SERVER||event.phase!=Phase.START)
			return;
		for(EntityPlayer player:isMarking.keySet())
			if(!player.isAddedToWorld()) {
				isMarking.remove(player);
				AftergrowChest.info("Player "+player.getName()+" failed to mark chest.");
			}
	}
	
	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event) {
		if(event.side!=Side.SERVER||event.phase!=Phase.START)
			return;
		DataContainer.get(event.world).update();
		
	}
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if(event.getWorld().isRemote)
			return;
		DataContainer.load((WorldServer) event.getWorld());;
	}
	
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if(event.getWorld().isRemote)
			return;
		DataContainer.unload((WorldServer) event.getWorld());
	}
	
	public static BlockPos getPos(double x, double y, double z) {
		return new BlockPos(x,y,z);
	}
	
	/*@SubscribeEvent
	public void onTick(PlayerTickEvent event) {
		if(event.side!=Side.SERVER)
			return;
		AftergrowChest.info(event.player);
		event.player.dimension
	}*/
	
}
