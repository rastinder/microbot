package net.runelite.client.plugins.microbot.rasReddie;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("example")
public interface rasReddieConfig extends Config {
    @ConfigSection(
            name = "General",
            description = "General",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "general";
    @ConfigItem(
            keyName = "DebugInfo",
            name = "show debug info",
            description = "player location num of berries etc etc",
            position = 1,
            section = generalSection
    )
    default boolean DebugInfofun()
    {
        return false;
    }
    @ConfigItem(
            keyName = "MakeDye",
            name = "Make Reddye",
            description = "Make Reddye",
            position = 1,
            section = generalSection
    )
    default boolean MakeDyefun()
    {
        return true;
    }
    @ConfigItem(
            keyName = "RedBeriesbuy",
            name = "Red berry buy quantity",
            description = "buy from grand exchange",
            position = 3,
            section = generalSection
    )
    default int RedBeriesbuyfun()
    {
        return 1000;
    }
}
