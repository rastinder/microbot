package net.runelite.client.plugins.microbot.woodcutting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

@Getter
@RequiredArgsConstructor
public enum WoodcuttingTree {
    TREE("Tree", "Logs",1, "Chop down"),
    OAK("Oak tree", "Oak logs",15, "Chop down"),
    WILLOW("Willow tree", "Willow logs",30, "Chop down"),
    TEAK_TREE("Teak tree", "Teak logs",35, "Chop down"),
    MAPLE("Maple tree", "Maple logs",45, "Chop down"),
    MAHOGANY("Mahogany tree", "Mahogany logs",50, "Chop down"),
    YEW("Yew tree", "Yew logs",60, "Chop down"),
    MAGIC("Magic tree", "Magic logs",75, "Chop down"),
    REDWOOD("Redwood tree", "Redwood logs",90, "Cut");


    private final String name;
    private final String log;
    private final int woodcuttingLevel;
    private final String action;

    @Override
    public String toString() {
        return name;
    }

    public boolean hasRequiredLevel() {
        return Rs2Player.getSkillRequirement(Skill.WOODCUTTING, this.woodcuttingLevel);
    }
}
