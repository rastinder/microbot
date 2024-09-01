package net.runelite.client.plugins.microbot.rasGold;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("example")
public interface RasGoldConfig extends Config {
    @ConfigSection(
            name = "General",
            description = "you should have neclace mould,gold bar , ruby",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "general";

    //....
    @ConfigItem(
            keyName = "bar",
            name = "bar",
            description = "bar",
            position = 1,
            section = generalSection
    )
    default String getBarName() {
        return "gold bar";
    }

    @ConfigItem(
            keyName = "bar count",
            name = "count",
            description = "bar",
            position = 2,
            section = generalSection
    )
    default int getBarCount() {
        return 14;
    }

    @ConfigItem(
            keyName = "bar or stone",
            name = "bar or stone",
            description = "2nd bar or stone needed?",
            position = 3,
            section = generalSection
    )
    default boolean stoneNeeded()
    {
        return false;
    }

    @ConfigItem(
            keyName = "bar or stone name",
            name = "bar or stone name",
            description = "bar or stone name",
            position = 4,
            section = generalSection
    )
    default String stoneProductName() {
        return "";
    }

    @ConfigItem(
            keyName = "item 2 count",
            name = "count",
            description = "item 2",
            position = 5,
            section = generalSection
    )
    default int getStoneCount() {
        return 14;
    }

    @ConfigItem(
            keyName = "mould",
            name = "mould",
            description = "mould",
            position = 6,
            section = generalSection
    )
    default String mouldProductName() {
        return "";
    }

    @ConfigItem(
            keyName = "mould needed",
            name = "mould needed",
            description = "item 3",
            position = 7,
            section = generalSection
    )
    default boolean mouldNeeded() {
        return true;
    }

    @ConfigItem(
            keyName = "finishedProductName",
            name = "finishedProductName",
            description = "finishedProductName",
            position = 8,
            section = generalSection
    )
    default String finishedProductName() {
        return "";
    }
}
