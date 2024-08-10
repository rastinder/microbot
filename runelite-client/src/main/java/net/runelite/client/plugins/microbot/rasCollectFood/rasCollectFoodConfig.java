package net.runelite.client.plugins.microbot.rasCollectFood;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("firstTimeChecks")
public interface rasCollectFoodConfig extends Config {

    @ConfigSection(
            name = "General",
            description = "General",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "general";
    @ConfigItem(
            keyName = "itemsToPick",
            name = "itemsToPick",
            description = "itemsToPick",
            position = 0,
            section = generalSection
    )
    default String itemsToPick()
    {
        return "";
    }
}
