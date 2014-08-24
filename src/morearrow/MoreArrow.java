package morearrow;

import java.io.File;
import static java.lang.System.out;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;


/**
 *
 * @author Administrator
 */
public class MoreArrow extends JavaPlugin{
        
        YamlConfiguration 配置文件;
        Map<String,箭效果> 玩家缓存列表=new HashMap<>();
        private void 加入缓存列表(Player 玩家,箭效果 值){
            玩家缓存列表.put(玩家.getName(), 值);
        }
        private boolean 检测是否在列表中(Player 玩家){
            return 玩家缓存列表.get(玩家.getName()) != null;
        }
        private 箭效果 获取缓存数据(Player 玩家){
            return (箭效果)玩家缓存列表.get(玩家.getName());
        }
        private void 从缓存列表中删除(Player 玩家){
            玩家缓存列表.remove(玩家.getName());
        }
        
    
    
    public class 事件监听器 implements Listener{
        @EventHandler
        public void 实体射箭事件(EntityShootBowEvent 事件){
            if(!(事件.getEntity() instanceof Player))
            {
                return;
            }
            Entity 箭=事件.getProjectile();
            Player 玩家=(Player)事件.getEntity();
            箭效果 效果=new 箭效果(玩家,箭);
            加入缓存列表(玩家,效果);
            效果.创建箭效果();
        }
        @EventHandler
        public void 箭击中事件(ProjectileHitEvent 事件){
            Entity 玩家=事件.getEntity().getVehicle();
            if(玩家==null)
                return;
            if(!检测是否在列表中((Player)玩家))
                return;
            从缓存列表中删除((Player)玩家);
        }
        @EventHandler
        public void 玩家切换潜行事件(PlayerToggleSneakEvent 事件){
            Player 玩家=事件.getPlayer();
            if(检测是否在列表中(玩家))
            {
                箭效果 效果=获取缓存数据(玩家);
                效果.玩家状态=false;
                效果.触发玩家.eject();
                效果.触发玩家.teleport(效果.玩家位置);
                从缓存列表中删除(玩家);
            }
        }
    }
    
    public class 箭效果 extends Thread{
        Player 触发玩家;
        Entity 箭;
        Boolean 玩家状态;
        Location 玩家位置;
        public 箭效果(Player 触发玩家, Entity 箭) {
            this.触发玩家 = 触发玩家;
            this.箭 = 箭;
        }
        public void 创建箭效果(){
            玩家状态=true;
            玩家位置=触发玩家.getLocation();
            箭.setPassenger(触发玩家);
            this.start();
        }
        @Override
            public void run() {
                int 循环计数=0;
                Vector 运动矢量;
                while(循环计数<200)
                {
                    运动矢量=触发玩家.getLocation().getDirection();
                    运动矢量.multiply(2/运动矢量.length());
                    箭.setVelocity(运动矢量);
                    if(!玩家状态)
                    {
                        return;
                    }
                    循环计数++;
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        out.print("延时出错");
                    }
                    
                }
                ProjectileHitEvent event=new ProjectileHitEvent((Projectile)箭);
                Bukkit.getServer().getPluginManager().callEvent(event);
            }
    }

    public void 加载配置文件(){
        File 配置=new File(getDataFolder(),"config.yml");
        if(!配置.exists())
        {
            this.saveDefaultConfig();
            配置=new File(getDataFolder(),"config.yml");
        }
        配置文件=YamlConfiguration.loadConfiguration(配置);
    }
    public void 加载合成公式(){
        ItemStack 弓=new ItemStack(Material.BOW);
        ShapedRecipe 合成公式=new ShapedRecipe(弓);
        String 合成公式字符串=配置文件.getString("Bow.Recipe");
        String 段字符串[]=new String[3];
        段字符串[0]=合成公式字符串.substring(0, 3);
        段字符串[1]=合成公式字符串.substring(3, 6);
        段字符串[2]=合成公式字符串.substring(6, 9);
        合成公式.shape(段字符串);
        for(int i=0;i<合成公式字符串.length();i++){
            if(合成公式字符串.charAt(i)!='X'){
                int ID=配置文件.getInt("Bow."+合成公式字符串.charAt(i));
                合成公式.setIngredient(合成公式字符串.charAt(i), new ItemStack(ID).getType());
            }
        }
        getServer().addRecipe(合成公式);
    }
    @Override
    public void onEnable() {
        加载配置文件();
        加载合成公式();
        out.print("更多的箭已载入！");
        getServer().getPluginManager().registerEvents(new 事件监听器(), this);
    }
    
}
