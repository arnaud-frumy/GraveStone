package dev.teamvery.mc.GraveStone;

import dev.teamvery.mc.configframework.cfg;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static dev.teamvery.mc.GraveStone.main.i;
import static dev.teamvery.mc.GraveStone.main.plugin_name;

public class GraveStoneListener implements Listener {

    private Location compass = null;
    private boolean create = false;
    private final Random random = new Random();
    private int randomCode = 0;
    private int tmp = 0;
    private int tmpCount = 0;
    private int tmpSneak = 0;

    private List<ItemStack> compassItemStack = new ArrayList<>();

    private void createChest(PlayerDeathEvent e, Block getBlock, List<ItemStack> items) {
        getBlock.setType(Material.CHEST);

        randomCode = random.nextInt(9999);

        var Chest = (Chest) getBlock.getState();



        new BukkitRunnable() {
            @Override
            public void run() {
                int tmpItem = 0;

                for (var item : items) {
                    try {
                        i().set(randomCode + "." + tmpItem, item);
                        cfg.save(plugin_name, main.items, false);

                        tmpItem++;
                    } catch (ArrayIndexOutOfBoundsException error) {
                        i().set(randomCode + "." + tmpItem, null);
                        cfg.save(plugin_name, main.items, false);
                    }
                }
            }
        }.runTaskLater(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(plugin_name)), 0);

        Chest.setCustomName(ChatColor.RED + e.getEntity().getName() + "?????? ??????" + ChatColor.BLACK + " " + randomCode);

        Chest.update();

        compass = Chest.getLocation();
        create = true;
    }

    @EventHandler
    void GraveStoneSpawn(PlayerDeathEvent e) {
        create = false;

//        var items = e.getDrops().stream().toList();
        var items = new ArrayList<ItemStack>();
        var deathLocate = e.getEntity().getLocation();
        var getBlock = deathLocate.getBlock();

        if (!e.getEntity().getInventory().isEmpty()) {

            for (var i : e.getDrops().stream().toList()) {
                if (i.getType().equals(Material.COMPASS) && String.valueOf(i.getItemMeta().displayName()).contains(ChatColor.RED + "?????? ??????")) {
                    compassItemStack.add(i);
                } else {
                    items.add(i);
                }
            }

            if ((!deathLocate.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY)) && (!items.isEmpty())) {

                e.getDrops().clear();
                if (getBlock.getType().equals(Material.AIR)) {
                    createChest(e, getBlock, items);

                } else {
                    new BukkitRunnable() {
                        @Override
                        public void run() {

                            if (deathLocate.getY() <= 0) {
                                deathLocate.setY(10);
                                var getBlockUp = deathLocate.getBlock();
                                createChest(e, getBlockUp, items);

                            } else {

                                for (tmp = 0; tmp <= 256; tmp++) {
                                    deathLocate.setY(deathLocate.getY() + 1);
                                    var getBlockUp = deathLocate.getBlock();

                                    if (getBlockUp.getType().equals(Material.AIR)) {
                                        createChest(e, getBlockUp, items);
                                        break;
                                    } else if (deathLocate.getY() >= 254) {
                                        deathLocate.setX(deathLocate.getX() + 1);
                                        getBlockUp = deathLocate.getBlock();

                                        createChest(e, getBlockUp, items);
                                        break;
                                    }
                                }
                            }
                        }
                    }.runTaskLater(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(plugin_name)), 0);
                }
            } else {
                e.getDrops().clear();
            }
        }
    }

    @EventHandler
    void BreakDisable(BlockBreakEvent e) {
        if (e.getBlock().getState() instanceof Chest chest) {
            try {
                if (Objects.requireNonNull(chest.getCustomName()).contains("?????? ??????")) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED + "????????? ?????? ??? ????????????");
                }
            } catch (NullPointerException error) {
                e.setCancelled(false);
            }
        }
    }

    @EventHandler
    void AddGraveStoneLocationCompass(PlayerRespawnEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (create) {
                    List<Component> lore = new ArrayList<>();

                    lore.add(Component.text("World : " + compass.getWorld().getName()));
                    lore.add(Component.text("X : " + compass.getX()));
                    lore.add(Component.text("Y : " + compass.getY()));
                    lore.add(Component.text("Z : " + compass.getZ()));
                    lore.add(Component.text(" "));
                    lore.add(Component.text(ChatColor.RED + "??????! ???????????? ????????? ????????? ????????? ??? ??? ????????????"));

                    ItemStack tmp = new ItemStack(Material.COMPASS);
                    CompassMeta trackercompass = (CompassMeta) tmp.getItemMeta();

                    trackercompass.setLodestoneTracked(false);
                    trackercompass.setLodestone(compass);
                    if (trackercompass instanceof Repairable repairable) {
                        repairable.setRepairCost(2000000000);
                    }
                    trackercompass.displayName(Component.text(ChatColor.RED + "?????? ??????" + ChatColor.BLACK + " " + randomCode));

                    trackercompass.lore(lore);

                    tmp.setItemMeta(trackercompass);

                    e.getPlayer().getInventory().addItem(tmp);
                }

                if (compassItemStack != null) {
                    for (ItemStack i : compassItemStack) {
                        e.getPlayer().getInventory().addItem(i);
                    }
                    compassItemStack.clear();
                }
            }
        }.runTaskLater(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(plugin_name)), 0);
    }

    @EventHandler
    void DeleteChest(InventoryOpenEvent e) {
        if (e.getInventory().getHolder() instanceof Chest chest) {

            try {
                if (Objects.requireNonNull(chest.getCustomName()).contains("?????? ??????")) {
                    var length = Objects.requireNonNull(chest.getCustomName()).length();
                    var chestCode = chest.getCustomName().substring(length - 4, length);

                    try {

                        if (String.valueOf(e.getPlayer().getInventory().getItemInMainHand().getItemMeta().displayName()).contains(ChatColor.RED + "?????? ??????")) {

                            if (String.valueOf(e.getPlayer().getInventory().getItemInMainHand().getItemMeta().displayName()).contains(chestCode)) {

                                var loc = chest.getLocation();
                                if (loc.getBlock().getType() == Material.CHEST) {

                                    if (tmpCount == 1) {
                                        e.getPlayer().getInventory().remove(e.getPlayer().getInventory().getItemInMainHand());
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                int tmpItem;

                                                for (tmpItem = 0; tmpItem <= 40; tmpItem++) {
                                                    try {
                                                        loc.getWorld().dropItem(loc, Objects.requireNonNull(i().getItemStack(chestCode + "." + tmpItem)));

                                                        i().set(chestCode + "." + tmpItem, null);
                                                        cfg.save(plugin_name, main.items, false);
                                                    } catch (NullPointerException error) {
                                                        continue;
                                                    }
                                                }

                                                i().set(chestCode, null);
                                                cfg.save(plugin_name, main.items, false);

                                                loc.getBlock().setType(Material.AIR);
                                            }
                                        }.runTaskLater(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin(plugin_name)), 0);
                                        tmpCount = 0;
                                    } else {
                                        e.setCancelled(true);
                                        e.getPlayer().sendMessage(ChatColor.RED + "???????????? ???????????? ????????? ?????? ????????? ????????? (???????????? ?????? ??????????????? ????????? ????????? ???????????????)");
                                        tmpCount++;
                                    }
                                }
                            } else {
                                e.setCancelled(true);
                                e.getPlayer().sendMessage(ChatColor.RED + "[" + chest.getCustomName().substring(0, length - 5) + ChatColor.RED + "] ????????? ?????? ?????? ???????????? ?????? ????????? ????????????!");
                            }
                        } else {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage(ChatColor.RED + "[" + chest.getCustomName().substring(0, length - 5) + ChatColor.RED + "] ?????? ?????? ???????????? ?????? ????????? ????????????!");
                        }
                    } catch (NullPointerException error) {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(ChatColor.RED + "[" + chest.getCustomName().substring(0, length - 5) + ChatColor.RED + "] ????????? ?????? ?????? ???????????? ?????? ????????? ????????????!");
                    }
                }
            } catch (NullPointerException error) {

            }
        }
    }

    @EventHandler
    void RemoveCompass(PlayerDropItemEvent e) {
        if (String.valueOf(e.getItemDrop().getItemStack().getItemMeta().displayName()).contains(ChatColor.RED + "?????? ??????")) {
            if (!e.getPlayer().isSneaking()) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "?????? ?????? ???????????? ?????? ??? ????????????");

            } else {
                if (tmpSneak >= 2) {
                    e.getItemDrop().remove();
                    tmpSneak = 0;
                } else {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED + "?????? ?????? ???????????? ???????????? ??? ?????? ???????????????");
                    tmpSneak++;
                }
            }
        }
    }
}
