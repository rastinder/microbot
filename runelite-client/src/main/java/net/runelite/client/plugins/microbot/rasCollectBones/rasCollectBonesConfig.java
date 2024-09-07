package net.runelite.client.plugins.microbot.rasCollectBones;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("collectbones")
public interface rasCollectBonesConfig extends Config {
    @ConfigSection(
            name = "Combat",
            description = "Combat",
            position = 0,
            closedByDefault = false
    )
    String combatSection = "General";
    String generalSection = "General";
    @ConfigItem(
            keyName = "Combat",
            name = "Auto attack npc",
            description = "Attacks npc",
            position = 1,
            section = combatSection
    )
    default boolean toggleCombat()
    {
        return false;
    }
    @ConfigItem(
            keyName = "enemyToAttack",
            name = "enemy name",
            description = "comma separated enemy names ",
            position = 2,
            section = generalSection
    )
    default String enemyName(){return "";}
    //........
    @ConfigItem(
            keyName = "LengthX",
            name = "LengthX",
            description = "LengthX ",
            position = 2,
            section = combatSection
    )
    default int LengthX(){return 10;}
    //........
    @ConfigItem(
            keyName = "HeightY",
            name = "HeightY",
            description = "HeightY ",
            position = 2,
            section = combatSection
    )
    default int HeightY(){return 10;}
    //........



    @ConfigItem(
            keyName = "attack type",
            name = "attack type",
            description = "Ranged,Magic,Melee",
            position = 3,
            section = combatSection
    )
    default attackStyle attackType(){return attackStyle.Melee;}

    @ConfigItem(
            keyName = "eat food",
            name = "eat food",
            description = "eat food",
            position = 4,
            section = combatSection
    )
    default boolean eatfood()
    {
        return false;
    }

    @ConfigSection(
            name = "Loot",
            description = "Loot",
            position = 5,
            closedByDefault = false
    )
    String lootSection = "Loot";

    @ConfigItem(
            keyName = "Loot items",
            name = "Auto loot items",
            description = "Enable/disable loot items",
            position = 6,
            section = lootSection
    )
    default boolean toggleLootItems()
    {
        return true;
    }
    @ConfigItem(
            keyName = "items to loot",
            name = "items to loot",
            description = "comma separated",
            position = 7,
            section = lootSection
    )
    default String LootItems(){return "rubes,bones,arrow";}

    @ConfigItem(
            keyName = "min value to loot",
            name = "min loot value",
            description = "also loot any item with min value",
            position = 8,
            section = lootSection
    )
    default int minValues(){return 1000;}


    @ConfigSection(
            name = "canon",
            description = "canon",
            position = 9,
            closedByDefault = false
    )
    String canonSection = "canon";

    @ConfigItem(
            keyName = "refil",
            name = "Auto refil canon",
            description = "Enable/disable refil",
            position = 10,
            section = canonSection
    )
    default boolean togglecanonRefil()
    {
        return true;
    }

    @ConfigSection(
            name = "Bank",
            description = "bank Items",
            position = 11,
            closedByDefault = false
    )
    String BISection = "Bank/inventory";

    @ConfigItem(
            keyName = "Use bank",
            name = "Use bank",
            description = "Enable/disable bank Items",
            position = 12,
            section = BISection
    )
    default boolean bankItems()
    {
        return true;
    }
    //...
    @ConfigItem(
            keyName = "inv setup",
            name = "inv setup",
            description = "Enable/disable inv setup",
            position = 12,
            section = BISection
    )
    default boolean invMaintain()
    {
        return true;
    }
    //...
    @ConfigItem(
            keyName = "max food items",
            name = "max food items",
            description = "Enable/disable max food items",
            position = 12,
            section = BISection
    )
    default int maxFood()
    {
        return 4;
    }
    //...
    @ConfigItem(
            keyName = "cook food items",
            name = "cook food items",
            description = "Enable/disable cook food items",
            position = 12,
            section = BISection
    )
    default cookON cookFood(){return cookON.Fire;}

    String otherSection = "other settings";

    @ConfigItem(
            keyName = "move to initial postion",
            name = "move to initial postion",
            description = "Enable/disable move to initial postion",
            position = 12,
            section = otherSection
    )
    default boolean walktoCenter()
    {
        return true;
    }
    enum attackStyle
    {
        Ranged,
        Magic,
        Melee
    }
    enum cookON{
        Fire,
        Monk,
        Cabbage,
        not_supported_kitchen
    }

}
