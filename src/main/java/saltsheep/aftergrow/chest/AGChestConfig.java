package saltsheep.aftergrow.chest;

import net.minecraftforge.common.config.Config;

@Config(modid = AftergrowChest.MODID)
public class AGChestConfig {

	@Config.Comment("粒子中心点相对于方块中心的坐标")
	public static double[] blockCentralOffset = {0,0,0};
	
	@Config.Comment("粒子距离粒子中心点的扩散范围")
	public static float expansion = 0.5f;
	
	@Config.Comment("多少刻生成一次粒子")
	public static int spawnTick = 1;
	
	@Config.Comment("采用粒子的名称")
	public static String particleName = "instantSpell";
	
}
