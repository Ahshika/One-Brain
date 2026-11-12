package com.onebrain.game.generator;

import com.onebrain.game.puzzle.WordClue;
import com.onebrain.game.puzzle.WordTheme;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Clue (emoji + theme) for every word in {@link WordBank}, so the player knows
 * which word the scrambled letters should form. Each entry is
 * {@code "WORD emoji THEME"}; WordBankTest checks that every bank word has one.
 */
final class WordClues {

    private static final String[] ENTRIES = {
            // 4 letters
            "BIRD 🪶 ANIMAL", "CAKE 🍰 FOOD", "FISH 🎣 ANIMAL", "GAME 🎮 FUN", "HOME 🏡 PLACE",
            "JUMP 🤸 ACTION", "KING 🤴 PERSON", "MILK 🥛 FOOD", "PARK 🛝 PLACE", "QUIZ ❔ LEARNING",
            "RAIN 🌧️ WEATHER", "YARD 🌻 PLACE", "ZONE 📍 PLACE", "FARM 🚜 PLACE", "GOLD 🥇 VALUABLE",
            "HAND ✋ BODY", "KITE 🪁 FUN", "MOON 🌙 SPACE", "OVEN ♨️ HOME", "PLAN 🗺️ IDEA",
            "ROAD 🛣️ PLACE", "TREE 🌳 NATURE", "WIND 💨 WEATHER", "BOOK 📖 LEARNING", "DUCK 🦆 ANIMAL",
            "FROG 🐸 ANIMAL", "GIFT 🎁 CELEBRATION", "HILL ⛰️ NATURE", "MAZE 🌀 FUN", "SAND 🏜️ NATURE",
            "WAVE 🌊 NATURE", "BELL 🔔 OBJECT", "CLAY 🏺 MATERIAL", "DRUM 🥁 MUSIC", "HERO 🦸 PERSON",
            "MINT 🌿 FOOD", "PINE 🌲 NATURE", "BABY 👶 PERSON", "CITY 🏙️ PLACE", "DESK 📎 OBJECT",
            "FOOD 🥘 FOOD", "GIRL 👧 PERSON", "JAZZ 🎷 MUSIC", "KICK 🦵 ACTION", "MATH ➗ LEARNING",
            "PINK 🩷 COLOR", "ROCK 🪨 NATURE", "SOUP 🍲 FOOD", "SOFA 🛋️ HOME", "TAXI 🚕 TRANSPORT",
            "WOOD 🪵 MATERIAL", "YOGA 🧘 SPORT", "ZERO 0️⃣ NUMBER", "CORN 🌽 FOOD", "DOVE 🕊️ ANIMAL",
            "FORK 🍴 HOME", "HORN 📯 MUSIC", "BOAT ⛵ TRANSPORT",
            // 5 letters
            "APPLE 🍎 FOOD", "BEACH 🏖️ PLACE", "BRAIN 🧠 BODY", "CANDY 🍭 FOOD", "CHAIR 🪑 HOME",
            "EAGLE 🦅 ANIMAL", "FLAME 🔥 NATURE", "FRUIT 🍇 FOOD", "GHOST 👻 FANTASY", "HAPPY 😄 FEELING",
            "HOUSE 🏠 PLACE", "JUICE 🧃 FOOD", "MAGIC 🪄 FANTASY", "MONEY 💵 VALUABLE", "MUSIC 🎵 MUSIC",
            "PIANO 🎹 MUSIC", "PIZZA 🍕 FOOD", "PLANT 🪴 NATURE", "QUEEN 👸 PERSON", "RADIO 📻 TECH",
            "RIVER 🏞️ NATURE", "ROBOT 🤖 TECH", "SPOON 🥄 HOME", "STORM ⛈️ WEATHER", "SUGAR 🍬 FOOD",
            "TIGER 🐯 ANIMAL", "TRAIN 🚆 TRANSPORT", "TRUCK 🚚 TRANSPORT", "WATER 💧 NATURE", "WHALE 🐋 ANIMAL",
            "WORLD 🌍 PLACE", "ZEBRA 🦓 ANIMAL", "CHESS ♟️ FUN", "CLOCK 🕰️ OBJECT", "GLASS 🥃 HOME",
            "HONEY 🍯 FOOD", "KNIFE 🔪 HOME", "LIGHT 💡 OBJECT", "MOUSE 🐭 ANIMAL", "SHEEP 🐑 ANIMAL",
            "SHIRT 👕 CLOTHING", "THUMB 👍 BODY", "VOICE 🎙️ BODY", "WHEEL 🛞 TRANSPORT", "YOUTH 🧒 PERSON",
            "BLOCK 🧱 OBJECT", "CROWN 👑 VALUABLE", "DRINK 🥤 FOOD", "FLOOR 🧹 HOME", "GIANT 🧌 FANTASY",
            "IVORY 🤍 COLOR", "JELLY 🍮 FOOD", "LUCKY 🍀 FEELING", "NOVEL 📕 LEARNING", "PARTY 🎉 CELEBRATION",
            "RHYME ✍️ LEARNING", "SALAD 🥗 FOOD", "SHARK 🦈 ANIMAL", "SKIRT 👗 CLOTHING", "TEETH 🦷 BODY",
            "UNCLE 👨 PERSON", "VIDEO 🎬 TECH", "WAGON 🐴 TRANSPORT", "BUNNY 🐰 ANIMAL", "CABIN 🛖 PLACE",
            "DAISY 🌼 NATURE", "FENCE 🚧 OBJECT", "GRASS 🌱 NATURE", "HOTEL 🏨 PLACE",
            // 6 letters
            "ANIMAL 🐾 ANIMAL", "BANANA 🍌 FOOD", "BASKET 🧺 OBJECT", "BRIDGE 🌉 PLACE", "BUTTER 🧈 FOOD",
            "CAMERA 📷 TECH", "CANDLE 🕯️ OBJECT", "CARROT 🥕 FOOD", "CIRCLE ⭕ SHAPE", "COFFEE ☕ FOOD",
            "COOKIE 🍪 FOOD", "DOCTOR 🩺 PERSON", "DRAGON 🐉 FANTASY", "GUITAR 🎸 MUSIC", "HAMMER 🔨 OBJECT",
            "ISLAND 🏝️ NATURE", "JACKET 🧥 CLOTHING", "JUNGLE 🌴 NATURE", "KITTEN 🐱 ANIMAL", "LADDER 🪜 OBJECT",
            "LAPTOP 💻 TECH", "LIZARD 🦎 ANIMAL", "MARKET 🏪 PLACE", "MIRROR 🪞 HOME", "MONKEY 🐒 ANIMAL",
            "ORANGE 🍊 FOOD", "PENCIL ✏️ LEARNING", "PEPPER 🌶️ FOOD", "PLANET 🪐 SPACE", "POCKET 👖 CLOTHING",
            "PUZZLE 🧩 FUN", "RABBIT 🐇 ANIMAL", "ROCKET 🚀 SPACE", "SCHOOL 🏫 PLACE", "SPRING 🌸 TIME",
            "SQUARE ⬛ SHAPE", "SUMMER ☀️ TIME", "TICKET 🎟️ OBJECT", "TOMATO 🍅 FOOD", "TUNNEL 🚇 PLACE",
            "TURTLE 🐢 ANIMAL", "WINDOW 🪟 HOME", "WINTER ❄️ TIME", "YELLOW 💛 COLOR", "BOTTLE 🍾 OBJECT",
            "BREEZE 🍃 WEATHER", "BUBBLE 🫧 OBJECT", "CACTUS 🌵 NATURE", "CHEESE 🧀 FOOD", "CHERRY 🍒 FOOD",
            "COTTON 🧵 MATERIAL", "DINNER 🍽️ FOOD", "ENGINE 🔧 TECH", "FABRIC 🧶 MATERIAL", "GALAXY 🌠 SPACE",
            "HELMET ⛑️ CLOTHING", "MAGNET 🧲 OBJECT", "MUFFIN 🧁 FOOD", "NAPKIN 🧻 HOME", "PILLOW 🛏️ HOME",
            "POTATO 🥔 FOOD", "PURPLE 💜 COLOR", "SADDLE 🏇 OBJECT", "SALMON 🐟 ANIMAL", "SHADOW 👤 NATURE",
            "SPONGE 🧽 HOME", "TEAPOT 🫖 HOME", "VIOLIN 🎻 MUSIC", "WALNUT 🌰 FOOD", "WIZARD 🧙 FANTASY",
            // 7 letters
            "AIRPORT 🛫 PLACE", "BALLOON 🎈 CELEBRATION", "BATTERY 🔋 TECH", "BICYCLE 🚲 TRANSPORT",
            "BLANKET 🛌 HOME", "BROTHER 👦 PERSON", "CABBAGE 🥬 FOOD", "CAPTAIN 👨‍✈️ PERSON", "CHICKEN 🐔 ANIMAL",
            "CRYSTAL 🔮 VALUABLE", "DIAMOND 💎 VALUABLE", "DOLPHIN 🐬 ANIMAL", "EXAMPLE 📋 IDEA", "FREEDOM 🗽 IDEA",
            "GIRAFFE 🦒 ANIMAL", "HARMONY 🎶 MUSIC", "HOLIDAY 🎊 CELEBRATION", "JOURNEY 🛤️ IDEA", "KITCHEN 🍳 PLACE",
            "LIBRARY 🏛️ PLACE", "MACHINE 🦾 TECH", "MORNING 🌅 TIME", "MYSTERY 🕵️ IDEA", "OCTOPUS 🐙 ANIMAL",
            "PENGUIN 🐧 ANIMAL", "PICTURE 🖼️ OBJECT", "PYRAMID 🔺 PLACE", "RAINBOW 🌈 WEATHER", "SCIENCE 🔬 LEARNING",
            "SHELTER ⛺ PLACE", "STATION 🚉 PLACE", "THUNDER ⚡ WEATHER", "TRUMPET 🎺 MUSIC", "UNICORN 🦄 FANTASY",
            "VILLAGE 🏘️ PLACE", "VOLCANO 🌋 NATURE", "WEATHER 🌦️ WEATHER", "WHISTLE 📣 OBJECT", "COMPASS 🧭 OBJECT",
            "CONCERT 🎤 MUSIC", "CURTAIN 🎭 HOME", "DESSERT 🍨 FOOD", "EMERALD 💚 VALUABLE", "FASHION 👠 CLOTHING",
            "FICTION 📘 LEARNING", "GRAMMAR 📝 LEARNING", "HAIRCUT 💇 BODY", "LOBSTER 🦞 ANIMAL", "NETWORK 📶 TECH",
            "ORCHARD 🍏 NATURE", "PANCAKE 🥞 FOOD", "QUALITY 🏅 IDEA", "SAUSAGE 🌭 FOOD", "SPINACH 🥬 FOOD",
            "TORNADO 🌪️ WEATHER", "VITAMIN 💊 BODY", "WEBSITE 🌐 TECH",
            // 8 letters
            "ALPHABET 🔤 LEARNING", "BASEBALL ⚾ SPORT", "BIRTHDAY 🎂 CELEBRATION", "BLUEBIRD 🐦 ANIMAL",
            "COMPUTER 🖥️ TECH", "DINOSAUR 🦖 ANIMAL", "ELEPHANT 🐘 ANIMAL", "FOOTBALL ⚽ SPORT",
            "GOLDFISH 🐠 ANIMAL", "HOSPITAL 🏥 PLACE", "KANGAROO 🦘 ANIMAL", "LANGUAGE 🗣️ LEARNING",
            "MAGAZINE 📰 LEARNING", "MOUNTAIN 🏔️ NATURE", "NOTEBOOK 📓 LEARNING", "PLATINUM 🥈 VALUABLE",
            "QUESTION ❓ IDEA", "SANDWICH 🥪 FOOD", "SCHEDULE 📅 TIME", "SHOULDER 💪 BODY",
            "SKELETON 💀 BODY", "SNOWBALL ☃️ WEATHER", "SUNSHINE 🌞 WEATHER", "TOMORROW ⏭️ TIME",
            "TREASURE 🏴‍☠️ VALUABLE", "UMBRELLA ☂️ OBJECT", "UNIVERSE 🌌 SPACE", "VACATION ✈️ CELEBRATION",
            "BACKPACK 🎒 OBJECT", "BLIZZARD 🌨️ WEATHER", "CAMPFIRE 🏕️ NATURE", "CUPBOARD 🗄️ HOME",
            "DAUGHTER 👨‍👧 PERSON", "DOORBELL 🛎️ HOME", "FIREWORK 🎆 CELEBRATION", "HEADACHE 🤕 BODY",
            "HOMEWORK 📚 LEARNING", "KEYBOARD ⌨️ TECH", "LEMONADE 🍋 FOOD", "MARATHON 🏃 SPORT",
            "MIDNIGHT 🌃 TIME", "MUSHROOM 🍄 NATURE", "NECKLACE 📿 CLOTHING", "OPPOSITE ↔️ IDEA",
            "PASSPORT 🛂 OBJECT", "PEACEFUL ☮️ FEELING", "PRINCESS 🫅 PERSON", "RAINCOAT 🧥 CLOTHING",
            "SEASHELL 🐚 NATURE", "STARFISH ⭐ ANIMAL", "SUITCASE 🧳 OBJECT", "SURPRISE 😲 FEELING",
            "CHAMPION 🏆 SPORT"
    };

    private static final Map<String, WordClue> CLUES = build();

    private WordClues() {
    }

    private static Map<String, WordClue> build() {
        Map<String, WordClue> map = new HashMap<>();
        for (String entry : ENTRIES) {
            String[] parts = entry.split(" ");
            if (parts.length != 3 || map.containsKey(parts[0])) {
                throw new IllegalStateException("Bad or duplicate clue entry: " + entry);
            }
            map.put(parts[0], new WordClue(parts[1], WordTheme.valueOf(parts[2])));
        }
        return Collections.unmodifiableMap(map);
    }

    /** @return the clue for {@code word}, or {@code null} if it has none. */
    static WordClue of(String word) {
        return CLUES.get(word);
    }
}
