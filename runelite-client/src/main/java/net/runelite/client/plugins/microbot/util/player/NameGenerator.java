package net.runelite.client.plugins.microbot.util.player;

import net.runelite.client.plugins.microbot.tutorialisland.NameUtils;

import java.util.AbstractMap;
import java.util.Calendar;
import java.util.Random;

import static net.runelite.client.plugins.microbot.util.Global.sleep;

/**
 * Just did it for fun. :|
 *
 * @author amit
 *
 */
public class NameGenerator {

    public AbstractMap.SimpleEntry<String, String> NameGenerator() {
        try {

            NameUtils nameUtils = new NameUtils();
            AbstractMap.SimpleEntry<String, String> names = nameUtils.getRandomNames();
            // Print the randomly chosen name
            String firstName = names.getKey();
            String lastName = names.getValue();
            if (firstName != null && lastName != null) {
                System.out.println("Randomly chosen name: " + firstName + " " + lastName);
                return names;
            } else {
                System.out.println("Failed to retrieve names.");
            }
        }
        catch (Exception e){
            sleep(10);
        }
        return null;
    }
}