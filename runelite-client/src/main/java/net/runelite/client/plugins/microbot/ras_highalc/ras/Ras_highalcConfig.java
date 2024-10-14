package net.runelite.client.plugins.microbot.ras_highalc.ras;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("highalc")
public interface Ras_highalcConfig extends Config {
    @ConfigSection(
            name = "General",
            description = "General",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "general";
    @ConfigItem(
            keyName = "Autobuy",
            name = "Auto buy",
            description = "from grand exchange",
            position = 1,
            section = generalSection
    )
    default boolean autoBuy()
    {
        return true;
    }

    @ConfigItem(
            keyName = "wait for buying in mins",
            name = "wait for buying in mins",
            description = "wait for buying in mins",
            position = 2,
            section = generalSection
    )
    default int  waitTime()
    {
        return 5;
    }

    @ConfigItem(
            keyName = "HighAlch",
            name = "auto high alch",
            description = "auto auto high alch",
            position = 3,
            section = generalSection
    )
    default boolean highAlch()
    {
        return true;
    }

    @ConfigItem(
            keyName = "naturalRuneCheck",
            name = "enable custom price?",
            description = "enable custom price for natural runes",
            position = 4,
            section = generalSection
    )
    default boolean naturalRuneCheck()
    {
        return true;
    }
    @ConfigItem(
            keyName = "naturalRunePrice",
            name = "price",
            description = "item price",
            position = 5,
            section = generalSection
    )
    default int naturalRunePrice()
    {
        return 95;
    }

    // Add list of items and their buy limits
    @ConfigItem(
            keyName = "itemList",
            name = "Item List",
            description = "List of items and their buy limits",
            position = 6,
            section = generalSection
    )
    default String itemList(){return  "Rune longsword:70,Rune battleaxe:70,Rune platebody:70,Rune platelegs:70,Rune plateskirt:70";}

}