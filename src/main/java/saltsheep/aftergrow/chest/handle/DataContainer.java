package saltsheep.aftergrow.chest.handle;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import saltsheep.aftergrow.chest.AGChestConfig;

public class DataContainer {
	
	private static Map<World,DataContainer> datas = Maps.newHashMap();
	
	private final World world;
	private final Map<BlockPos,String> marks = Maps.newHashMap();
	private final Map<UUID,Map<BlockPos,IInventory>> buffers = Maps.newHashMap();
	
	private DataContainer(World world) {
		this.world = world;
	}
	
	//*Return the result message.
	public MarkChestResult markChest(BlockPos pos) {
		Block block = world.getBlockState(pos).getBlock();
		if(!(block instanceof BlockChest))
			return MarkChestResult.FAILED_NOCHEST;
		if(this.marks.containsKey(pos))
			return MarkChestResult.FAILED_ALREADY;
		this.marks.put(pos,UUID.randomUUID().toString());
		this.saveMarks();
		return MarkChestResult.SUCCESSFUL;
	}
	
	public boolean unmarkChest(BlockPos pos) {
		if(!this.marks.containsKey(pos))
			return false;
		this.marks.remove(pos);
		this.saveMarks();
		return true;
	}
	
	public boolean isMarked(BlockPos pos) {
		return this.marks.containsKey(pos);
	}
	
	public void saveMarks() {
		if(MinecraftForge.EVENT_BUS.post(new ChestMarkContainerEvent.SavePre(this)))
			return;
		NBTTagCompound data = new NBTTagCompound();
		for(Entry<BlockPos, String> each:this.marks.entrySet()) {
			BlockPos pos = each.getKey();
			data.setIntArray(each.getValue(), new int[] {pos.getX(),pos.getY(),pos.getZ()});
		}
		this.world.getWorldInfo().getDimensionData(this.world.provider.getDimension()).setTag("AftergrowChestMarks", data);
		MinecraftForge.EVENT_BUS.post(new ChestMarkContainerEvent.SaveEnd(this));
	}
	
	public void readMarks() {
		if(MinecraftForge.EVENT_BUS.post(new ChestMarkContainerEvent.ReadPre(this)))
			return;
		NBTTagCompound data = getData();
		for(String uuid:data.getKeySet()) {
			int[] posL = data.getIntArray(uuid);
			BlockPos pos = new BlockPos(posL[0],posL[1],posL[2]);
			this.marks.put(pos, uuid);
		}
		MinecraftForge.EVENT_BUS.post(new ChestMarkContainerEvent.ReadPre(this));
	}
	
	private NBTTagCompound getData() {
		NBTTagCompound didat = this.world.getWorldInfo().getDimensionData(this.world.provider.getDimension());
		NBTTagCompound nbtdat = didat.getCompoundTag("AftergrowChestMarks");
		didat.setTag("AftergrowChestMarks", nbtdat);
		return nbtdat;
	}
	
	public IInventory getChest(EntityPlayer player, ContainerChest realChest, BlockPos pos) {
		this.checkBuffers();
		this.checkBuffer(player.getUniqueID());
		IInventory inv = this.getBuffer(player.getUniqueID(), pos);
		//*存在缓冲
		if(inv !=null)
			return inv;
		inv = new InventoryBasic("宝箱", true, 27);
		//*没有缓冲，且已经拿过，返回空箱子
		if(hasTaken(player,pos))
			return inv;
		for(int i=0;i<27;i++)
			inv.setInventorySlotContents(i, realChest.inventorySlots.get(i).getStack().copy());
		//*放置缓冲
		this.putBuffer(player.getUniqueID(), pos, inv);
		//*标记已经拿过
		this.addTaken(player, pos);
		return inv;
	}
	
	private void checkBuffers() {
		for(UUID player:buffers.keySet())
			if(this.world.getPlayerEntityByUUID(player)==null)
				buffers.remove(player);
	}
	
	private void checkBuffer(UUID player) {
		if(!buffers.containsKey(player))
			buffers.put(player, Maps.newHashMap());
	}
	
	@Nullable
	private IInventory getBuffer(UUID player, BlockPos pos) {
		return buffers.get(player).get(pos);
	}
	
	private void putBuffer(UUID player, BlockPos pos, IInventory inv) {
		buffers.get(player).put(pos, inv);
	}
	
	private void addTaken(EntityPlayer player, BlockPos pos) {
		if(MinecraftForge.EVENT_BUS.post(new ChestMarkContainerEvent.SetTakenPre(this, player, pos)))
			return;
		NBTTagCompound dimentionDataList = player.getEntityData().getCompoundTag("AftergrowChestTaken");
		NBTTagCompound takens = dimentionDataList.getCompoundTag(String.valueOf(this.world.provider.getDimension()));
		takens.setBoolean(this.marks.get(pos),true);
		dimentionDataList.setTag(String.valueOf(this.world.provider.getDimension()), takens);
		player.getEntityData().setTag("AftergrowChestTaken", dimentionDataList);
		MinecraftForge.EVENT_BUS.post(new ChestMarkContainerEvent.SetTakenEnd(this, player, pos));
	}
	
	private boolean hasTaken(EntityPlayer player, BlockPos pos) {
		ChestMarkContainerEvent.CheckTakenPre event = new ChestMarkContainerEvent.CheckTakenPre(this, player, pos);
		if(MinecraftForge.EVENT_BUS.post(event))
			return event.hasTaken;
		NBTTagCompound dimentionDataList = player.getEntityData().getCompoundTag("AftergrowChestTaken");
		NBTTagCompound takens = dimentionDataList.getCompoundTag(String.valueOf(this.world.provider.getDimension()));
		String posUUID = this.marks.get(pos);
		MinecraftForge.EVENT_BUS.post(new ChestMarkContainerEvent.CheckTakenEnd(this, player, pos));
		return takens.getBoolean(posUUID);
	}
	
	private Map<BlockPos,List<EntityPlayer>> playParticles = Maps.newHashMap();
	private int existTick = 0;
	
	public void update() {
		if(existTick%20==0) {
			playParticles.clear();
			for(BlockPos pos:this.marks.keySet()) {
				if(!this.world.isBlockLoaded(pos))
					continue;
				List<EntityPlayer> players = Lists.newLinkedList();
				for(EntityPlayer player:this.world.playerEntities)
					if(!this.hasTaken(player, pos))
						players.add(player);
				playParticles.put(pos, players);
			}
		}if(existTick%AGChestConfig.spawnTick==0) {
			for(Entry<BlockPos, List<EntityPlayer>> entry:this.playParticles.entrySet()) {
				BlockPos pos = entry.getKey();
				List<EntityPlayer> players = entry.getValue();
				Packet<?> packet = new SPacketParticles(EnumParticleTypes.getByName(AGChestConfig.particleName), false, (float)(pos.getX()+0.5+AGChestConfig.blockCentralOffset[0]), (float)(pos.getY()+0.5+AGChestConfig.blockCentralOffset[1]), (float)(pos.getZ()+0.5+AGChestConfig.blockCentralOffset[2]), (float)AGChestConfig.expansion, (float)AGChestConfig.expansion, (float)AGChestConfig.expansion, (float)0, 1, new int[0]);
				for(EntityPlayer player:players) {
					//*即距离<=128
					if(player.getPosition().distanceSq(pos)<=16384)
						((EntityPlayerMP)player).connection.sendPacket(packet);
				}
					
			}
		}
		existTick++;
	}
	
	public static void resetTaken(EntityPlayer player) {
		if(MinecraftForge.EVENT_BUS.post(new ChestMarkContainerEvent.ResetTakenPre(player)))
			return;
		player.getEntityData().setTag("AftergrowChestTaken", new NBTTagCompound());
		for(DataContainer data:DataContainer.datas.values())
			data.buffers.put(player.getUniqueID(), Maps.newHashMap());
		MinecraftForge.EVENT_BUS.post(new ChestMarkContainerEvent.ResetTakenEnd(player));
	}
	
	public static void reset() {
		datas.clear();;
	}
	
	protected static DataContainer load(World world) {
		DataContainer data = datas.get(world);
		if(data == null) {
			data = new DataContainer(world);
			datas.put(world, data);
		}
		ChestMarkContainerEvent event = new ChestMarkContainerEvent.Load(data);
		MinecraftForge.EVENT_BUS.post(event);
		data = event.getData();
		data.readMarks();
		return data;
	}
	
	protected static void unload(World world) {
		DataContainer data = datas.get(world);
		MinecraftForge.EVENT_BUS.post(new ChestMarkContainerEvent.Unload(data));
		data.saveMarks();
		datas.remove(world);
	}

	public static DataContainer get(World world) {
		DataContainer data = datas.get(world);
		if(data==null)
			return load(world);
		return data;
	}
	
	public static enum MarkChestResult{
		SUCCESSFUL("Mark chest successful.",true),
		FAILED_NOCHEST("There's isn't a chest.",false),
		FAILED_ALREADY("The chest is already mark.",false);
		//FAILED_NONAME("You must give the chest a name.",false);
		
		MarkChestResult(String str, boolean isSuccess) {
			this.message = str;
			this.success = isSuccess;
		}

		public final String message;
		public final boolean success;
	}
	
}
