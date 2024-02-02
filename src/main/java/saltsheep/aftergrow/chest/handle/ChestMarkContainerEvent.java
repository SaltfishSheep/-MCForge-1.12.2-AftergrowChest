package saltsheep.aftergrow.chest.handle;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ChestMarkContainerEvent extends Event{
	
	protected DataContainer data;
	
	public ChestMarkContainerEvent(DataContainer data) {
		this.data = data;
	}
	
	public DataContainer getData() {
		return this.data;
	}
	
	public static class Load extends ChestMarkContainerEvent{
		public Load(DataContainer data) {
			super(data);
		}
		public void setDataContainer(DataContainer data) {
			this.data = data;
		}
	}
	
	public static class Unload extends ChestMarkContainerEvent{
		public Unload(DataContainer data) {
			super(data);
		}
	}
	
	@Cancelable
	public static class SavePre extends ChestMarkContainerEvent{
		public SavePre(DataContainer data) {
			super(data);
		}
	}
	
	public static class SaveEnd extends ChestMarkContainerEvent{
		public SaveEnd(DataContainer data) {
			super(data);
		}
	}
	
	@Cancelable
	public static class ReadPre extends ChestMarkContainerEvent{
		public ReadPre(DataContainer data) {
			super(data);
		}
	}
	
	public static class ReadEnd extends ChestMarkContainerEvent{
		public ReadEnd(DataContainer data) {
			super(data);
		}
	}
	
	public static class Taken extends ChestMarkContainerEvent {
		public final EntityPlayer player;
		public final BlockPos pos;
		public Taken(DataContainer data, EntityPlayer player, BlockPos pos) {
			super(data);
			this.player=player;
			this.pos=pos;
		}
	}
	
	@Cancelable
	public static class CheckTakenPre extends Taken {
		public boolean hasTaken = false;
		public CheckTakenPre(DataContainer data, EntityPlayer player, BlockPos pos) {
			super(data, player, pos);
		}
		public void forceHasTaken(boolean hasTaken) {
			this.setCanceled(true);
			this.hasTaken = hasTaken;
		}
	}
	
	public static class CheckTakenEnd extends Taken {
		public CheckTakenEnd(DataContainer data, EntityPlayer player, BlockPos pos) {
			super(data, player, pos);
		}
	}
	
	@Cancelable
	public static class SetTakenPre extends Taken {
		public SetTakenPre(DataContainer data, EntityPlayer player, BlockPos pos) {
			super(data, player, pos);
		}
	}
	
	public static class SetTakenEnd extends Taken {
		public SetTakenEnd(DataContainer data, EntityPlayer player, BlockPos pos) {
			super(data, player, pos);
		}
	}
	
	@Cancelable
	public static class ResetTakenPre extends PlayerEvent {
		public ResetTakenPre(EntityPlayer player) {
			super(player);
		}
	}
	
	public static class ResetTakenEnd extends PlayerEvent {
		public ResetTakenEnd(EntityPlayer player) {
			super(player);
		}
	}

}
