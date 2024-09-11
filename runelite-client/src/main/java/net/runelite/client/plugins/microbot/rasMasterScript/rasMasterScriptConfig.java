package net.runelite.client.plugins.microbot.rasMasterScript;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;


@ConfigGroup("example")
public interface rasMasterScriptConfig extends Config {
    @ConfigSection(
            name = "plugin timer",
            description = "after how many times a plugin will stop",
            position = 0,
            closedByDefault = false
    )
    String timerSection = "plugintimer";

    @ConfigItem(

            keyName = "plugintimer",
            name = "plugintimer",
            description = "plugintimer",
            position = 1,
            section = timerSection
    )
    default TimerOptions plugintimer() {
        return TimerOptions.MIN_30_45;
    }

    @ConfigSection(
            name = "moneymaking",
            description = "moneymaking scripts",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "moneymaking";

    @ConfigItem(

            keyName = "rasHardLeather",
            name = "rasHardLeather",
            description = "rasHardLeather",
            position = 2,
            section = generalSection
    )
    default boolean HardLeather(){return true;}

    @ConfigItem(

            keyName = "Wheat",
            name = "Wheat",
            description = "Wheat",
            position = 2,
            section = generalSection
    )
    default boolean wheat(){return true;}

    @ConfigItem(

            keyName = "Tinderbox",
            name = "Tinderbox",
            description = "Tinderbox",
            position = 2,
            section = generalSection
    )
    default boolean tinderbox(){return true;}

    @ConfigItem(

            keyName = "fishfood",
            name = "fishfood",
            description = "fishfood",
            position = 2,
            section = generalSection
    )
    default boolean fishfood(){return true;}

    @ConfigItem(

            keyName = "Woodcutting",
            name = "Woodcutting",
            description = "Woodcutting",
            position = 2,
            section = generalSection
    )
    default boolean Woodcutting(){return true;}

    @ConfigItem(

            keyName = "chocolateDust",
            name = "chocolateDust",
            description = "chocolateDust",
            position = 2,
            section = generalSection
    )
    default boolean chocolateDust(){return true;}

    @ConfigItem(

            keyName = "PieShell",
            name = "PieShell",
            description = "PieShell",
            position = 2,
            section = generalSection
    )
    default boolean pieShell(){return true;}

    @ConfigItem(

            keyName = "PastryDough",
            name = "PastryDough",
            description = "PastryDough",
            position = 2,
            section = generalSection
    )
    default boolean pastryDough(){return true;}

    @ConfigItem(

            keyName = "HighCal",
            name = "HighCal",
            description = "HighCal",
            position = 2,
            section = generalSection
    )
    default boolean highCal(){return true;}

    @ConfigItem(

            keyName = "trainmagic",
            name = "train magic",
            description = "trainmagic",
            position = 2,
            section = generalSection
    )
    default boolean trainmagic(){return true;}

    @ConfigItem(

            keyName = "fishing",
            name = "fishing",
            description = "fishing",
            position = 2,
            section = generalSection
    )
    default boolean fishing(){return true;}

    @ConfigItem(

            keyName = "combatTrain",
            name = "combatTrain",
            description = "combatTrain",
            position = 2,
            section = generalSection
    )
    default boolean combattrain(){return true;}

    @ConfigItem(

            keyName = "questAllz",
            name = "questAllz",
            description = "questAllz",
            position = 2,
            section = generalSection
    )
    default boolean questAllz(){return true;}
}
