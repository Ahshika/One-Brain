package com.onebrain.game.generator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Curated word lists. Words with common anagrams were deliberately left out so
 * a valid English word is (almost) never marked wrong. The list order is part of
 * the daily-puzzle contract: append new words only at the end of a list.
 */
public final class WordBank {

    private static final String[] FOUR = {
            "BIRD", "CAKE", "FISH", "GAME", "HOME", "JUMP", "KING", "MILK", "PARK", "QUIZ",
            "RAIN", "YARD", "ZONE", "FARM", "GOLD", "HAND", "KITE", "MOON", "OVEN", "PLAN",
            "ROAD", "TREE", "WIND", "BOOK", "DUCK", "FROG", "GIFT", "HILL", "MAZE", "SAND",
            "WAVE", "BELL", "CLAY", "DRUM", "HERO", "MINT", "PINE", "BABY", "CITY", "DESK",
            "FOOD", "GIRL", "JAZZ", "KICK", "MATH", "PINK", "ROCK", "SOUP", "SOFA", "TAXI",
            "WOOD", "YOGA", "ZERO", "CORN", "DOVE", "FORK", "HORN", "BOAT"
    };

    private static final String[] FIVE = {
            "APPLE", "BEACH", "BRAIN", "CANDY", "CHAIR", "EAGLE", "FLAME", "FRUIT", "GHOST", "HAPPY",
            "HOUSE", "JUICE", "MAGIC", "MONEY", "MUSIC", "PIANO", "PIZZA", "PLANT", "QUEEN", "RADIO",
            "RIVER", "ROBOT", "SPOON", "STORM", "SUGAR", "TIGER", "TRAIN", "TRUCK", "WATER", "WHALE",
            "WORLD", "ZEBRA", "CHESS", "CLOCK", "GLASS", "HONEY", "KNIFE", "LIGHT", "MOUSE", "SHEEP",
            "SHIRT", "THUMB", "VOICE", "WHEEL", "YOUTH", "BLOCK", "CROWN", "DRINK", "FLOOR", "GIANT",
            "IVORY", "JELLY", "LUCKY", "NOVEL", "PARTY", "RHYME", "SALAD", "SHARK", "SKIRT", "TEETH",
            "UNCLE", "VIDEO", "WAGON", "BUNNY", "CABIN", "DAISY", "FENCE", "GRASS", "HOTEL"
    };

    private static final String[] SIX = {
            "ANIMAL", "BANANA", "BASKET", "BRIDGE", "BUTTER", "CAMERA", "CANDLE", "CARROT", "CIRCLE", "COFFEE",
            "COOKIE", "DOCTOR", "DRAGON", "GUITAR", "HAMMER", "ISLAND", "JACKET", "JUNGLE", "KITTEN", "LADDER",
            "LAPTOP", "LIZARD", "MARKET", "MIRROR", "MONKEY", "ORANGE", "PENCIL", "PEPPER", "PLANET", "POCKET",
            "PUZZLE", "RABBIT", "ROCKET", "SCHOOL", "SPRING", "SQUARE", "SUMMER", "TICKET", "TOMATO", "TUNNEL",
            "TURTLE", "WINDOW", "WINTER", "YELLOW", "BOTTLE", "BREEZE", "BUBBLE", "CACTUS", "CHEESE", "CHERRY",
            "COTTON", "DINNER", "ENGINE", "FABRIC", "GALAXY", "HELMET", "MAGNET", "MUFFIN", "NAPKIN", "PILLOW",
            "POTATO", "PURPLE", "SADDLE", "SALMON", "SHADOW", "SPONGE", "TEAPOT", "VIOLIN", "WALNUT", "WIZARD"
    };

    private static final String[] SEVEN = {
            "AIRPORT", "BALLOON", "BATTERY", "BICYCLE", "BLANKET", "BROTHER", "CABBAGE", "CAPTAIN", "CHICKEN", "CRYSTAL",
            "DIAMOND", "DOLPHIN", "EXAMPLE", "FREEDOM", "GIRAFFE", "HARMONY", "HOLIDAY", "JOURNEY", "KITCHEN", "LIBRARY",
            "MACHINE", "MORNING", "MYSTERY", "OCTOPUS", "PENGUIN", "PICTURE", "PYRAMID", "RAINBOW", "SCIENCE", "SHELTER",
            "STATION", "THUNDER", "TRUMPET", "UNICORN", "VILLAGE", "VOLCANO", "WEATHER", "WHISTLE", "COMPASS", "CONCERT",
            "CURTAIN", "DESSERT", "EMERALD", "FASHION", "FICTION", "GRAMMAR", "HAIRCUT", "LOBSTER", "NETWORK", "ORCHARD",
            "PANCAKE", "QUALITY", "SAUSAGE", "SPINACH", "TORNADO", "VITAMIN", "WEBSITE"
    };

    private static final String[] EIGHT = {
            "ALPHABET", "BASEBALL", "BIRTHDAY", "BLUEBIRD", "COMPUTER", "DINOSAUR", "ELEPHANT", "FOOTBALL", "GOLDFISH", "HOSPITAL",
            "KANGAROO", "LANGUAGE", "MAGAZINE", "MOUNTAIN", "NOTEBOOK", "PLATINUM", "QUESTION", "SANDWICH", "SCHEDULE", "SHOULDER",
            "SKELETON", "SNOWBALL", "SUNSHINE", "TOMORROW", "TREASURE", "UMBRELLA", "UNIVERSE", "VACATION", "BACKPACK", "BLIZZARD",
            "CAMPFIRE", "CUPBOARD", "DAUGHTER", "DOORBELL", "FIREWORK", "HEADACHE", "HOMEWORK", "KEYBOARD", "LEMONADE", "MARATHON",
            "MIDNIGHT", "MUSHROOM", "NECKLACE", "OPPOSITE", "PASSPORT", "PEACEFUL", "PRINCESS", "RAINCOAT", "SEASHELL", "STARFISH",
            "SUITCASE", "SURPRISE", "CHAMPION"
    };

    private final Map<Integer, List<String>> byLength = new HashMap<>();
    private final Map<String, Set<String>> anagramIndex = new HashMap<>();

    public WordBank() {
        register(FOUR);
        register(FIVE);
        register(SIX);
        register(SEVEN);
        register(EIGHT);
    }

    private void register(String[] words) {
        byLength.put(words[0].length(), Collections.unmodifiableList(Arrays.asList(words)));
        for (String w : words) {
            Set<String> set = anagramIndex.get(key(w));
            if (set == null) {
                set = new HashSet<>();
                anagramIndex.put(key(w), set);
            }
            set.add(w);
        }
    }

    private static String key(String word) {
        char[] c = word.toCharArray();
        Arrays.sort(c);
        return new String(c);
    }

    public List<String> wordsOfLength(int length) {
        List<String> list = byLength.get(length);
        if (list == null) {
            throw new IllegalArgumentException("No words of length " + length);
        }
        return list;
    }

    public Set<String> anagramsOf(String word) {
        Set<String> set = anagramIndex.get(key(word));
        return set == null ? Collections.singleton(word) : Collections.unmodifiableSet(set);
    }
}
