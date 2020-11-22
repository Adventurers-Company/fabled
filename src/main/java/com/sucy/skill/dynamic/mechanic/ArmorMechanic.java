package com.sucy.skill.dynamic.mechanic;

import com.rit.sucy.text.TextFormatter;
import com.sucy.skill.SkillAPI;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.List;

/**
 * Sets the specified armor slot of the target to the item defined by the settings
 */
public class ArmorMechanic extends MechanicComponent {
    private static final String SLOT = "slot";
    private static final String MATERIAL = "material";
    private static final String AMOUNT   = "amount";
    private static final String DURABILITY = "durability";
    private static final String DATA     = "data";
    private static final String CUSTOM   = "custom";
    private static final String NAME     = "name";
    private static final String LORE     = "lore";
    private static final String OVERWRITE = "overwrite";

    @Override
    public String getKey() { return "armor"; }

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     *
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets)
    {
        String mat = settings.getString(MATERIAL, "arrow").toUpperCase().replace(" ", "_");
        EquipmentSlot slot;
        try {
            slot = EquipmentSlot.valueOf(settings.getString(SLOT, "HAND").toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException exception) { return false; }
        Material material;
        try { material = Material.valueOf(mat); } catch (Exception ex) { return false; }
        int amount = settings.getInt(AMOUNT, 1);
        int durability = settings.getInt(DURABILITY, 0);
        int data = settings.getInt(DATA, 0);
        boolean overwrite = settings.getBool(OVERWRITE, false);

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (settings.getString(CUSTOM, "false").toLowerCase().equals("true")) {
            String name = TextFormatter.colorString(settings.getString(NAME, ""));
            if (name.length() > 0) {
                meta.setDisplayName(name);
            }
            List<String> lore = TextFormatter.colorStringList(settings.getStringList(LORE));
            meta.setLore(lore);
        }

        if (SkillAPI.getSettings().useSkillModelData()) {
            meta.setCustomModelData(data);
        } else {
            item.setData(new MaterialData(material, (byte) data));
        }

        if (SkillAPI.getSettings().useOldDurability()) {
            item.setItemMeta(meta);
            item.setDurability((short) durability);
        } else {
            if (meta instanceof Damageable) {
                ((Damageable) meta).setDamage(durability);
            }
            item.setItemMeta(meta);
        }

        for (LivingEntity target : targets) {
            if (overwrite) {
                target.getEquipment().setItem(slot, item);
            } else {
                EntityEquipment equipment = target.getEquipment();
                if (equipment.getItem(slot).getType().equals(Material.AIR)) {
                    equipment.setItem(slot, item);
                }
            }
        }
        return targets.size() > 0;
    }
}
