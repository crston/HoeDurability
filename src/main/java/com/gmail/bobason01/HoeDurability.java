package com.gmail.bobason01;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class HoeDurability extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // 이벤트 리스너 등록
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // 손에 든 아이템이 괭이(HOE)인지 확인
        if (item.getType().toString().toLowerCase().contains("hoe")) {
            // 작물이라면 무조건 내구도 1 감소
            damageTool(player, item, 1);
        }
    }

    private void damageTool(Player player, ItemStack tool, int damage) {
        ItemMeta meta = tool.getItemMeta();
        if (!(meta instanceof Damageable damageable)) {
            return;
        }

        // Unbreakable 여부 확인
        if (meta.isUnbreakable()) {
            return;
        }

        // Unbreaking 인챈트 레벨 확인
        int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.DURABILITY);
        int actualDamage = 0;

        for (int i = 0; i < damage; i++) {
            if (unbreakingLevel > 0) {
                double chance = 1.0 / (unbreakingLevel + 1);
                if (Math.random() < chance) {
                    actualDamage++;
                }
            } else {
                actualDamage++;
            }
        }

        if (actualDamage == 0) {
            return; // 실제 감소할 내구도가 없음
        }

        // 내구도 감소 이벤트 호출
        PlayerItemDamageEvent damageEvent = new PlayerItemDamageEvent(player, tool, actualDamage);
        getServer().getPluginManager().callEvent(damageEvent);

        if (damageEvent.isCancelled()) {
            return;
        }

        int finalDamage = damageEvent.getDamage();
        damageable.setDamage(damageable.getDamage() + finalDamage);
        tool.setItemMeta(meta);
    }
}
