package net.runelite.client.plugins.microbot.rasCollectFood;

import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
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
    String generalSection = "General";
    @ConfigItem(
            keyName = "itemsToPick",
            name = "itemsToPick",
            description = "itemsToPick",
            position = 1,
            section = generalSection
    )
     default String itemsToPick()
    {
        return "Raw,Trout,Salmon";

    }
    @ConfigItem(
            keyName = "aretheyGroundObj",
            name = "Ground Obj?",
            description = "GroundObj",
            position = 2,
            section = generalSection
    )
    default boolean isGroundObj()
    {
        return true;
    }
    @ConfigItem(
            keyName = "sellthem",
            name = "sellthem",
            description = "sellthem",
            position = 3,
            section = generalSection
    )
    default boolean sellthem()
    {
        return true;
    }
    @ConfigItem(
            keyName = "area",
            name = "area",
            description = "area",
            position = 4,
            section = generalSection
    )
    default WorldPoint area()
    {
        return new WorldPoint(3105,3432,0);
    }
}
