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
    default String itemList()
    {
        return  "Rune longsword:70,Rune battleaxe:70,Rune platebody:70,Rune platelegs:70,Rune plateskirt:70,Rune med helm:70,Rune 2h sword:70,Rune sq shield:70,Adamant platebody:125,Rune mace:70,Green d'hide body:125,Rune full helm:70,Mithril platebody:125,Green d'hide vambraces:125,Steel platebody:125,Rune warhammer:70,Adamant platelegs:125,Steel 2h sword:125,Rune dagger:70,Mithril sq shield:125,Mithril sword:125,Rune axe:40,Mithril longsword:125,Mithril chainbody:125,Mithril 2h sword:125,Steel battleaxe:125,Rune chainbody:70,Mithril mace:125,Steel kiteshield:125,Diamond necklace:18000,Mithril pickaxe:40,Iron platebody:125,Diamond ring:10000,Studded chaps:125,Diamond amulet (u):10000,Adamant dagger:125,Mithril axe:40,Emerald amulet (u):10000,Castle wars bracelet(3):10000,Ruby necklace:18000,Emerald necklace:18000,Mithril battleaxe:125,Steel full helm:125,Mithril scimitar:125,Sapphire necklace:18000,Rune scimitar:70,Mithril med helm:125,Iron 2h sword:125,Iron platelegs:125,Adamant scimitar:125,Diamond amulet:10000,Steel scimitar:125,Steel longsword:125,Ring of forging:10000,Ruby amulet (u):10000,Adamant med helm:125,Emerald ring:10000,Gold necklace:18000,Maple longbow:18000,Sapphire ring:10000,Mithril warhammer:125,Steel warhammer:125,Mithril dagger:125,Rune kiteshield:70,Adamant sq shield:125,Coif:125,Hardleather body:125,Willow longbow:18000,Unblessed symbol:10000,Iron warhammer:125,Mithril kiteshield:125,Gold bar:10000,Air tiara:40,Adamant 2h sword:125,Gold ring:18000,Adamant chainbody:125,Maple shortbow:18000,Gold amulet:18000,Earth tiara:40,Gold amulet (u):18000,Antipoison(2):2000,Willow shortbow:18000,Adamant arrow:11000,Sapphire amulet (u):10000,Maple logs:15000,Longbow:18000,Antipoison(4):2000,Iron kiteshield:125,Unstrung symbol:10000,Law rune:18000,Emerald amulet:10000,Mind tiara:40,Oak longbow:18000,Iron full helm:125,Mithril arrow:7000,Nature rune:18000,Leather vambraces:125,Steel platelegs:125,Oak shortbow:18000,Leather cowl:125,Pure essence:30000,Leather chaps:125,Death rune:25000,Silk:18000,Iron scimitar:125,Silver ore:13000,Earth rune:50000,Chaos rune:18000,Air rune:50000,Water rune:50000,Fire rune:50000,Ruby ring:10000,Mithril ore:13000,Rune essence:20000,Shortbow:18000,Leather body:125,Sardine:6000,Bronze chainbody:125,Steel arrow:7000,Pike:6000,Rune sword:70,Bowl:13000,Fire tiara:40,Tomato:13000,Herring:6000,Bucket of water:13000,Knife:40,Egg:13000,Iron axe:40,Ruby amulet:10000,Raw tuna:15000,Anchovies:15000,Trout:6000,Shrimps:6000,Willow logs:15000,Bucket of milk:13000,Adamant full helm:125,Meat pie:10000,Bronze longsword:125,Raw lobster:15000,Raw anchovies:13000,Adamant battleaxe:40";    }
    }