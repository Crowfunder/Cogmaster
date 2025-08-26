package com.crowfunder.cogmaster.Index;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.crowfunder.cogmaster.Configs.Path;
import com.crowfunder.cogmaster.Parseable.NameIndexService;
import com.crowfunder.cogmaster.Parseable.NewConfigEntry;
import com.crowfunder.cogmaster.Parseable.ParseableGraphRepository;

@Service
public class IndexService2 {

    private final ParseableGraphRepository graphRepo;
    private final NameIndexService nameIndexService;

    public IndexService2(ParseableGraphRepository graphRepo, NameIndexService nameIndexService) {
        this.graphRepo = graphRepo;
        this.nameIndexService = nameIndexService;
    }

    public Set<String> getAllConfigFileNames() {
        return graphRepo.getAll().keySet();
    }

    // Returns all config index keys(from all files) concatenated into a single set
    public Set<String> getAllConfigEntryKeys() {
        Set<String> result = new HashSet<>();
        for (Map<Path, NewConfigEntry> subIndex : graphRepo.getAll().values()) {
            for (Path path : subIndex.keySet()) {
                result.add(path.toString());
            }
        }
        return result;
    }

    // returns a map keyed by config file name and a value being the list of all the
    // config entry keys in that file
    public Map<String, Set<String>> getAllConfigIndexKeysMapped() {
        Map<String, Set<String>> result = new HashMap<>();
        for (Map.Entry<String, Map<Path, NewConfigEntry>> entry : graphRepo.getAll().entrySet()) {
            String outerKey = entry.getKey();
            Set<Path> innerKeys = entry.getValue().keySet();
            Set<String> innerKeysString = new HashSet<>();
            for (Path path : innerKeys) {
                innerKeysString.add(path.toString());
            }

            result.put(outerKey, innerKeysString);
        }

        return result;
    }

    // Resolve one or more ConfigEntry objects by
    // querying the propertiesService for name mappings
    // that can be used in nameIndex
    // Ignores case (always searches by lowercase)
    public List<NewConfigEntry> resolveConfigByName(String name) {
        return graphRepo.resolveConfigsFullPath(nameIndexService.readNameIndex(name));
    }

    // Copied from the old Index Service
    // Endpoint tailored for Kozma Bot, with love
    // Return name index keys into a single list
    // Attempts to only return items that are tradeable in game
    // Using a few heurestics, namely filter by implementations
    // and look for known parameters defining being tradeable
    public Set<String> getTradeableEntryNames() {
        class EntryNameVariants {
            private static final Set<String> variantsWeapon = new HashSet<>(Set.of(
                    "{0} Asi Very High",
                    "{0} Asi Very High Ctr Very High",
                    "{0} Asi Very High Ctr High",
                    "{0} Asi Very High Ctr Med",
                    "{0} Asi High",
                    "{0} Asi High Ctr Very High",
                    "{0} Asi High Ctr High",
                    "{0} Asi High Ctr Med",
                    "{0} Asi Med",
                    "{0} Asi Med Ctr Very High",
                    "{0} Asi Med Ctr High",
                    "{0} Asi Med Ctr Med",
                    "{0} Ctr Very High",
                    "{0} Ctr High",
                    "{0} Ctr Med"));
            private static final Set<String> variantsBomb = new HashSet<>(Set.of(
                    "{0} Ctr Very High",
                    "{0} Ctr High",
                    "{0} Ctr Med"));
            private static final Set<String> variantsArmor = new HashSet<>(Set.of(
                    "{0} Fire High",
                    "{0} Fire Max",
                    "{0} Shadow High",
                    "{0} Shadow Max",
                    "{0} Normal High",
                    "{0} Normal Max"));
            private static final Set<String> variantsShield = new HashSet<>(Set.of(
                    "{0} Fire High",
                    "{0} Fire Max"));
            private static final Set<String> implementationsBomb = new HashSet<>(Set.of(
                    "com.threerings.projectx.item.config.ItemConfig$Bomb"));
            private static final Set<String> implementationsWeapon = new HashSet<>(Set.of(
                    "com.threerings.projectx.item.config.ItemConfig$Handgun",
                    "com.threerings.projectx.item.config.ItemConfig$SwingingHandgun",
                    "com.threerings.projectx.item.config.ItemConfig$Sword"));

            private static final Set<String> implementationsArmor = new HashSet<>(Set.of(
                    "com.threerings.projectx.item.config.ItemConfig$Armor",
                    "com.threerings.projectx.item.config.ItemConfig$Helm",
                    "com.threerings.projectx.item.config.ItemConfig$Shield"));

            private static final Set<String> implementationsShield = new HashSet<>(Set.of(
                    "com.threerings.projectx.item.config.ItemConfig$Shield"));

            public static List<String> getItemVariants(String name, String implementation) {
                List<String> result = new ArrayList<>();
                result.add(name);
                Set<String> variants;
                if (implementationsBomb.contains(implementation)) {
                    variants = variantsBomb;
                } else if (implementationsWeapon.contains(implementation)) {
                    variants = variantsWeapon;
                } else if (implementationsArmor.contains(implementation)) {
                    variants = variantsArmor;
                } else if (implementationsShield.contains(implementation)) {
                    variants = variantsShield;
                } else {
                    return result;
                }
                for (String variant : variants) {
                    result.add(MessageFormat.format(variant, name));
                }
                return result;
            }
        }

        Set<String> implementationsWhitelist = new HashSet<>(Set.of(
                "com.threerings.projectx.item.config.ItemConfig$SpawnActor",
                "com.threerings.projectx.item.config.ItemConfig$AnimatedAction",
                "com.threerings.projectx.item.config.ItemConfig$Armor",
                "com.threerings.projectx.item.config.ItemConfig$ArmorCostume",
                "com.threerings.projectx.item.config.ItemConfig$Bomb",
                "com.threerings.projectx.item.config.ItemConfig$Color",
                "com.threerings.projectx.item.config.ItemConfig$Craft",
                "com.threerings.projectx.item.config.ItemConfig$GiftBox",
                "com.threerings.projectx.item.config.ItemConfig$Handgun",
                "com.threerings.projectx.item.config.ItemConfig$Height",
                "com.threerings.projectx.item.config.ItemConfig$Helm",
                "com.threerings.projectx.item.config.ItemConfig$HelmCostume",
                "com.threerings.projectx.item.config.ItemConfig$Lockbox",
                "com.threerings.projectx.item.config.ItemConfig$Shield",
                "com.threerings.projectx.item.config.ItemConfig$ShieldCostume",
                "com.threerings.projectx.item.config.ItemConfig$SpriteEgg",
                "com.threerings.projectx.item.config.ItemConfig$SwingingHandgun",
                "com.threerings.projectx.item.config.ItemConfig$Sword",
                "com.threerings.projectx.item.config.ItemConfig$Trinket",
                "com.threerings.projectx.item.config.ItemConfig$Ticket",
                "com.threerings.projectx.item.config.ItemConfig$Upgrade",
                "com.threerings.projectx.item.config.ItemConfig$WrappingPaper",
                "com.threerings.projectx.design.config.FurniConfig$Prop",
                "com.threerings.projectx.design.config.FurniConfig$SpecialProp",
                "com.threerings.projectx.item.config.AccessoryConfig$Footstep",
                "com.threerings.projectx.item.config.AccessoryConfig$Original"));
        Set<String> namesBlacklist = new HashSet<>(Set.of(
                "Prototype Rocket Hammer",
                "Stable Rocket Hammer",
                "Warmaster Rocket Hammer",
                "Dark Reprisal",
                "Dark Reprisal Mk II",
                "Dark Retribution",
                "Groundbreaker Armor",
                "Groundbreaker Helm",
                "Honor Blade",
                "Tempered Honor Blade",
                "Ascended Honor Blade",
                "Lionheart Honor Blade",
                "Honor Guard",
                "Great Honor Guard",
                "Mighty Honor Guard",
                "Exalted Honor Guard"));

        Set<String> result = new HashSet<>();

        for (String name : nameIndexService.getNameIndexKeysPretty()) {
            // Check if name is blacklisted or null or incomplete
            if (name == null || namesBlacklist.contains(name) || name.contains("{")) {
                continue;
            }

            // Check if implementation is whitelisted
            NewConfigEntry configEntry = resolveConfigByName(name).get(0); // Get any entry, shouldn't matter for
                                                                           // tradeable items
            if (!implementationsWhitelist.contains(configEntry.getRootImplementationType())) {
                continue;
            }

            // Check if known parameters marking items as untradeable exist
            if (!(configEntry.getRoutedParameters().resolveParameterPath("locked") == null)) {
                continue;
            }

            // Do not add rooms tickets
            if (configEntry.getRoutedParameters().parameterValueEquals("type", "DESIGN_ROOM") ||
                    configEntry.getRoutedParameters().parameterValueEquals("type", "GUILD_EXPANSION") ||
                    configEntry.getRoutedParameters().parameterValueEquals("type", "GUILD_UPGRADE") ||
                    configEntry.getRoutedParameters().parameterValueEquals("type", "DOOR_TYPE")) {
                continue;
            }

            // Do not variant 0,1-star items
            if (!(configEntry.getRoutedParameters().parameterValueEquals("rarity", "1")) &&
                    !(configEntry.getRoutedParameters().parameterValueEquals("rarity", "0"))) {
                result.addAll(EntryNameVariants.getItemVariants(name, configEntry.getRootImplementationType()));
            } else {
                result.add(name);
            }
        }
        return result;
    }

    public Map<String, Integer> getIndexStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Parsed Configs", getAllConfigFileNames().size());
        stats.put("Loaded Config Entries", getAllConfigEntryKeys().size());
        stats.put("Named Config Entries", nameIndexService.getNameIndexKeysPretty().size());
        return stats;
    }

}
