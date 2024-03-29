package de.mvmo.armorstandedit.listener;

import de.mvmo.armorstandedit.misc.Axis;
import de.mvmo.armorstandedit.mode.ArmorStandEditMode;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PlayerInteractEventListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!ArmorStandEditMode.isInEditMode(player))
            return;

        ArmorStandEditMode editMode = ArmorStandEditMode.editModeFromPlayer(player);

        if (!(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
            return;

        ItemStack item = event.getItem();
        if (item == null || item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null || item.getType().equals(Material.AIR))
            return;

        if (item.getItemMeta().getDisplayName().equalsIgnoreCase("§cCreate ArmorStand")) {
            ArmorStand armorStand = (ArmorStand) player.getLocation().getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);

            armorStand.setBasePlate(false);
            armorStand.setGravity(false);
            armorStand.setArms(true);

            editMode.setArmorStand(armorStand);
            editMode.applyAxisEditInventory();

            event.setCancelled(true);

            return;
        }

        if (item.getItemMeta().getDisplayName().equalsIgnoreCase("§cEdit whole ArmorStand")) {
            editMode.applyWholeArmorStandEditInventory();
            editMode.setWhole(true);
            return;
        }

        if (item.getItemMeta().getDisplayName().equalsIgnoreCase("§cEdit Axis")) {
            editMode.applyAxisEditInventory();
            editMode.setWhole(false);
            return;
        }

        if (item.getItemMeta().getDisplayName().startsWith("§cChange Body Part")) {
            editMode.setPart(editMode.getPart().next());
            editMode.applyAxisEditInventory();
            return;
        }

        if (item.getItemMeta().getDisplayName().equalsIgnoreCase("§cGUI")) {
            editMode.openPropertiesInventory();
            event.setCancelled(true);
            return;
        }

        if (item.getItemMeta().getDisplayName().equalsIgnoreCase("§cRotation")) {
            Location location = editMode.getArmorStand().getLocation();
            location.setYaw(location.getYaw() + 5);

            player.sendTitle("", "§cRot. §8» §c" + Math.round(location.getYaw()));

            editMode.getArmorStand().teleport(location);
            event.setCancelled(true);
            return;
        }

        Axis axis;
        try {
            axis = Axis.valueOf(ChatColor.stripColor(item.getItemMeta().getDisplayName()).toUpperCase());
        } catch (IllegalArgumentException exception) {
            return;
        }

        event.setCancelled(true);

        if (editMode.isWhole()) {
            editMode.executeMove(axis);
            return;
        }

        EulerAngle eulerAngle = editMode.executePartRotation(axis);
        try {
            Method method = eulerAngle.getClass().getDeclaredMethod("get" + axis.toString());
            method.setAccessible(true);
            double value = (double) method.invoke(eulerAngle);

            player.sendTitle("", "§c" + axis.toString() + "§8 » §c" + Math.round(Math.toDegrees(value)));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            return;
        }
    }

}
