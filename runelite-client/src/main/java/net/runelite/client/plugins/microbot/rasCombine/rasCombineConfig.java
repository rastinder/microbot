package net.runelite.client.plugins.microbot.rasCombine;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("Combine")
public interface rasCombineConfig extends Config {
    @ConfigSection(
            name = "General",
            description = "General",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "general";

    @ConfigItem(
            keyName = "item 1",
            name = "item 1",
            description = "item 1",
            position = 1,
            section = generalSection
    )
    default String item1()
    {
        return "";
    }

    @ConfigItem(
            keyName = "item 1 count",
            name = "count",
            description = "item 1",
            position = 2,
            section = generalSection
    )
    default int item1Count()
    {
        return 14;
    }

    @ConfigItem(
            keyName = "item 2",
            name = "item 2",
            description = "item 2",
            position = 3,
            section = generalSection
    )
    default String item2()
    {
        return "";
    }

    @ConfigItem(
            keyName = "item 2 count",
            name = "count",
            description = "item 2",
            position = 4,
            section = generalSection
    )
    default int item2Count()
    {
        return 14;
    }

    @ConfigItem(
            keyName = "item 3",
            name = "item 3",
            description = "item 3",
            position = 5,
            section = generalSection
    )
    default String item3()
    {
        return "";
    }

    @ConfigItem(
            keyName = "item 3 count",
            name = "count",
            description = "item 3",
            position = 6,
            section = generalSection
    )
    default int item3Count()
    {
        return 14;
    }

    @ConfigItem(
            keyName = "itembuylimit",
            name = "itembuylimit",
            description = "itembuylimit",
            position = 7,
            section = generalSection
    )
    default int itemMaxLimit()
    {
        return 1500;
    }

    @ConfigItem(
            keyName = "press space",
            name = "press space?",
            description = "need to press space or not?",
            position = 8,
            section = generalSection
    )
    default boolean spacepress()
    {
        return false;
    }

    @ConfigItem(
            keyName = "buyMissingItems",
            name = "buyMissingItems",
            description = "buyMissingItems",
            position = 9,
            section = generalSection
    )
    default boolean itemFromGE()
    {
        return false;
    }

}
