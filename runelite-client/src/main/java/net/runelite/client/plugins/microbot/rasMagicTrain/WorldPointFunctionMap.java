package net.runelite.client.plugins.microbot.rasMagicTrain;

import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.raschoclatebar.raschoclatebarScript.waitForAnimationStop;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;

public class WorldPointFunctionMap extends Script {

    // Define a functional interface for the actions
    @FunctionalInterface
    public interface WorldPointActionWithSpell  {
        void execute(WorldPoint worldPoint, MagicAction spellToCast);
    }

    // Create a class to hold WorldPoint and its associated action
    public static class WorldPointEntry {
        private final WorldPoint worldPoint;
        private final WorldPointActionWithSpell action;
        private final MagicAction spellToCast;

        public WorldPointEntry(WorldPoint worldPoint, WorldPointActionWithSpell  action,MagicAction spellToCast) {
            this.worldPoint = worldPoint;
            this.action = action;
            this.spellToCast = spellToCast;
        }

        public WorldPoint getWorldPoint() {
            return worldPoint;
        }

        public WorldPointActionWithSpell  getAction() {
            return action;
        }
        public MagicAction getSpellToCast() { // Add getter for spellToCast
            return spellToCast;
        }
    }
    // Define the functions associated with each WorldPoint
    public static void killKnights(WorldPoint wp,MagicAction spellToCast) {
        System.out.println("Executing killKnights at " + wp + " with: "+ spellToCast);
        walkto(wp);
        attackEnemies(null,spellToCast,8,true);
    }

    public static void worshipZamorak(WorldPoint wp,MagicAction spellToCast) {
        System.out.println("Executing worshipZamorak at " + wp+ " with: "+ spellToCast);
        if(Rs2Player.getWorldLocation().distanceTo(wp) > 5) {
            if (Math.random() < 0.5) walkto(new WorldPoint(2958, 3438, 0));
            else walkto(new WorldPoint(2968, 3438, 0));
        }
        if(Rs2Player.getWorldLocation().distanceTo(wp) > 1)
            walkto(wp);
        if (!Rs2Player.isInteracting()) {
            lootAndComeback(wp, true,"Zamorak monk bottom,Zamorak monk top");
        }
        List<String> targetNames = Arrays.asList("Monk of Zamorak");
            attackEnemies(targetNames,spellToCast);
    }

    public static void guardSkeleton(WorldPoint wp,MagicAction spellToCast) {
        System.out.println("Executing guardSkeleton at " + wp+ " with: "+ spellToCast);
        walkto(wp);
        List<String> targetNames = Arrays.asList("Skeleton");
        attackEnemies(targetNames,spellToCast);
    }

    public static void exploreSewer(WorldPoint wp,MagicAction spellToCast) {
        System.out.println("Executing exploreSewer at " + wp+ " with: "+ spellToCast);
        walkto(wp);
        attackEnemies(null,spellToCast);
    }

    public static void freeZamorak(WorldPoint wp,MagicAction spellToCast) {
        System.out.println("Executing freeZamorak at " + wp+ " with: "+ spellToCast);
        walkto(wp);
        List<String> targetNames = Arrays.asList("Monk of Zamorak");
        attackEnemies(targetNames,spellToCast);
    }

    public static void interrogateDemon(WorldPoint wp,MagicAction spellToCast) {
        System.out.println("Executing interrogateDemon at " + wp+ " with: "+ spellToCast);
        walkto(wp);
        List<String> targetNames = Arrays.asList("Lesser demon");
        attackEnemies(targetNames,spellToCast);
    }

    public static void milkCow(WorldPoint wp,MagicAction spellToCast) {
        System.out.println("Executing milkCow at " + wp+ " with: "+ spellToCast);
        walkto(wp);
        List<String> targetNames = Arrays.asList("Cow", "Cow calf");
        if (Rs2GameObject.interact(1564,"close",10))
            Rs2Player.waitForAnimation();
        attackEnemies(targetNames,spellToCast);
    }
    public static void protectCow(WorldPoint wp,MagicAction spellToCast) {
        System.out.println("Executing protectCow at " + wp+ " with: "+ spellToCast);
        walkto(wp);
        List<String> targetNames = Arrays.asList("Cow", "Cow calf");
        attackEnemies(targetNames,spellToCast);
    }

    public static void feedCow(WorldPoint wp,MagicAction spellToCast) {
        System.out.println("Executing feedCow at " + wp+ " with: "+ spellToCast);
        walkto(wp);
        List<String> targetNames = Arrays.asList("Cow", "Cow calf");
        attackEnemies(targetNames,spellToCast);
    }
    public static void walkto(WorldPoint wp) {
            while (Rs2Player.getWorldLocation().distanceTo(wp) > 2) {
                if (Rs2Player.getWorldLocation().distanceTo(wp) > 9) {
                    Rs2Walker.walkTo(wp, 5);
                    sleep(200);
                    waitForAnimationStop();

                }
                if (Rs2Player.getWorldLocation().distanceTo(wp) <= 9) {
                    Rs2Walker.walkFastCanvas(wp);
                    sleep(200);
                    waitForAnimationStop();
                } //else System.out.println("major problem detected " + wp);
            }
    }
    public static void lootAndComeback(WorldPoint wp,Boolean Equip,String... names){
        if (!Rs2Inventory.isFull()){
            for (String name : names){
                if(Rs2GroundItem.loot(name,10)) {
                    sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)) , 100, 9000);
                    if (Equip){
                        if (!Rs2Equipment.hasEquippedContains(name)){
                            Rs2Inventory.interact(name,"Wield");
                            sleepUntilTrue(()->Rs2Inventory.waitForInventoryChanges(() -> sleep(100)) , 100, 9000);
                        }
                    }
                }

            }
            walkto(wp);
        }
    }
    public static void attackEnemies(List<String> targetNames, MagicAction spellToCast) {
        attackEnemies(targetNames,spellToCast,10,false);
    }
    public static void attackEnemiesOld(List<String> targetNames, MagicAction spellToCast,int range, boolean lineofsight) {
        List<NPC> enemies;
        if (targetNames == null) {
            enemies = Rs2Npc.getAttackableNpcs().collect(Collectors.toList());
        } else {
            enemies = Rs2Npc.getAttackableNpcs()
                    .filter(npc -> targetNames.contains(npc.getName()))
                    .collect(Collectors.toList());
        }

        if (!enemies.isEmpty() && !Rs2Player.isInteracting() && !Rs2Player.isAnimating()) {
            NPC nearestEnemy = enemies.stream()
                    .filter(enemy -> !enemy.isDead() && !enemy.isInteracting())
                    .filter(enemy -> Rs2Player.getWorldLocation().distanceTo(enemy.getWorldLocation()) <= range)
                    .filter(enemy -> !lineofsight || Rs2Npc.hasLineOfSight(enemy)) // Line of sight check
                    .min(Comparator.comparingInt(enemy -> Rs2Player.getWorldLocation().distanceTo(enemy.getWorldLocation())))
                    .orElse(null);

            if (nearestEnemy != null) {
                if(!randomSleep())
                    sleep(200,1200);
                //Rs2Npc.interact(nearestEnemy,"Attack");
                Rs2Magic.castOn(spellToCast, nearestEnemy);
                sleep(1000);
            }
        }
    }
    public static void attackEnemies(List<String> targetNames, MagicAction spellToCast, int range, boolean lineofsight) {
        List<NPC> enemies;

        if (targetNames == null) {
            enemies = Rs2Npc.getAttackableNpcs().collect(Collectors.toList());
        } else {
            enemies = Rs2Npc.getAttackableNpcs()
                    .filter(npc -> targetNames.contains(npc.getName()))
                    .collect(Collectors.toList());
        }

        System.out.println("Enemies after initial filtering: " + enemies.size());

        if (!enemies.isEmpty() && !Rs2Player.isInteracting() && !Rs2Player.isAnimating()) {
            NPC nearestEnemy = enemies.stream()
                    .filter(enemy -> {
                        boolean isDead = enemy.isDead();
                        System.out.println("Checking if enemy is dead: " + enemy.getName() + " - " + isDead);
                        return !isDead;
                    })
                    .filter(enemy -> {
                        boolean isInteracting = enemy.isInteracting();
                        System.out.println("Checking if enemy is interacting: " + enemy.getName() + " - " + isInteracting);
                        return !isInteracting;
                    })
                    .filter(enemy -> {
                        int distance = Rs2Player.getWorldLocation().distanceTo(enemy.getWorldLocation());
                        System.out.println("Checking distance to enemy: " + enemy.getName() + " - " + distance);
                        return distance <= range;
                    })
                    .filter(enemy -> {
                        boolean hasLOS = !lineofsight || Rs2Npc.hasLineOfSight(enemy);
                        System.out.println("Checking line of sight to enemy: " + enemy.getName() + " - " + hasLOS);
                        return hasLOS;
                    })
                    .min(Comparator.comparingInt(enemy -> Rs2Player.getWorldLocation().distanceTo(enemy.getWorldLocation())))
                    .orElse(null);

            if (nearestEnemy != null) {
                if (!randomSleep())
                    sleep(200, 1200);

                System.out.println("Attacking enemy: " + nearestEnemy.getName());
                Rs2Magic.castOn(spellToCast, nearestEnemy);
                sleep(1000);
            } else {
                System.out.println("No valid enemy found after filtering.");
            }
        } else {
            System.out.println("No enemies available or player is interacting/animating.");
        }
    }
    public static boolean randomSleep(){
        if (random(1, 60) == 1) {
            System.out.println("sleep max 3sec");
            sleep(2000,3000);
            return  true;
        } else if (random(1,300) == 1) {
            System.out.println("sleep max 30sec");
            sleep(20000,30000);
            return  true;
        } else if (random(1,1000) == 1) {
            System.out.println("sleep max 10min");
            sleep(280000, 600000);
            return  true;
        }
        return false;
    }

}
