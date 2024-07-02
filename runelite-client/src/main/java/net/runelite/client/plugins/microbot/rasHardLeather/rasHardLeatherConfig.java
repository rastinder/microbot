package net.runelite.client.plugins.microbot.rasHardLeather;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("example")
public interface rasHardLeatherConfig extends Config {
    //randomBonePick
    @ConfigSection(
            name = "General",
            description = "General",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "General";
    @ConfigItem(
            keyName = "randomBonePick",
            name = "Pick Bones randomly",
            description = "randomly pick bones",
            position = 1,
            section = generalSection
    )
    default boolean randomBonePick()
    {
        return true;
    }
    @ConfigItem(
            keyName = "loot",
            name = "Get from loot",
            description = "get from loot or get from bank",
            position = 0,
            section = generalSection
    )
    default boolean loot()
    {
        return true;
    }

}
