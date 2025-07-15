package com.crowfunder.cogmaster.Index;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import com.crowfunder.cogmaster.Configs.ConfigReference;
import com.crowfunder.cogmaster.Configs.Path;
import com.crowfunder.cogmaster.Translations.TranslationsService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

@Service
public class IndexService {

    IndexRepository indexRepository;
    TranslationsService translationsService;

    public IndexService(IndexRepository indexRepository, TranslationsService translationsService) {
        this.indexRepository = indexRepository;
        this.translationsService = translationsService;
    }

    // Get ConfigEntry object by its config path
    public ConfigEntry resolveConfig(String configName, Path path) {
        return indexRepository.readConfigIndex(configName, path);
    }

    // Get ConfigEntry object by its config path
    public ConfigEntry resolveConfig(String configName, String path) {
        return resolveConfig(configName, new Path(path));
    }

    // Get ConfigEntry by path that leads both to the correct index and entry within it
    public ConfigEntry resolveConfig(Path path) {
        return indexRepository.readConfigIndex(path.getNextPath(), path.rotatePath());
    }

    // Get ConfigEntry by path that leads both to the correct index and entry within it
    public ConfigEntry resolveConfig(String path) {
        return resolveConfig(new Path(path));
    }

    // Get ConfigEntry object by resolving a ConfigReference object
    public ConfigEntry resolveConfig(ConfigReference configReference) {
        return indexRepository.readConfigIndex(configReference.getSourceConfig(), configReference.getPath());
    }

    // Get multiple ConfigEntry objects by paths
    // Works only for full paths (indicating the exact PathIndex entry)
    public List<ConfigEntry> resolveConfigsFullPath(List<Path> paths) {
        List<ConfigEntry> configs = new ArrayList<>();
        for (Path path : paths) {
            configs.add(resolveConfig(path));
        }
        if (configs.isEmpty()) {
            return null;
        }
        return configs;
    }

    // Resolve one or more ConfigEntry objects by
    // querying the propertiesService for name mappings
    // that can be used in nameIndex
    // Ignores case (always searches by first character of word uppercase)
    // Commented out case ignorance, because it is faulty and we use Autocomplete regardless
    public List<ConfigEntry> resolveConfigByName(String name) {
//        name = uppercaseFirstLetters(name.toLowerCase());
        return resolveConfigsFullPath(indexRepository.readNameIndex(name));
    }

    // Get a list of all available configs
    public Set<String> getAllConfigNames() {
        return indexRepository.getAllIndexKeys();
    }

    public Set<String> getAllEntryNames() {
        return indexRepository.getAllNameIndexKeys();
    }

    public Set<String> getAllConfigPaths() {
        return indexRepository.getAllConfigIndexKeysJoint();
    }

    public Map<String, Set<String>> getConfigPathsMap() {
        return indexRepository.getAllConfigIndexKeysMapped();
    }


    // Endpoint tailored for Kozma Bot, with love
    // Return name index keys into a single list
    // Attempts to only return items that are tradeable in game
    // Using a few heurestics, namely filter by implementations
    // and look for known parameters defining being tradeable
    @Cacheable("getTradeableEntryNames")
    public Set<String> getTradeableEntryNames() {
        class UniqueVariants {
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
                "{0} Ctr Med"
            ));
            private static final Set<String> variantsBomb = new HashSet<>(Set.of(
                "{0} Ctr Very High",
                "{0} Ctr High",
                "{0} Ctr Med"
            ));
            private static final Set<String> variantsArmor = new HashSet<>(Set.of(
                "{0} Fire High",
                "{0} Fire Max",
                "{0} Shadow High",
                "{0} Shadow Max",
                "{0} Normal High",
                "{0} Normal Max"
            ));
            private static final Set<String> implementationsBomb = new HashSet<>(Set.of(
                    "com.threerings.projectx.item.config.ItemConfig$Bomb"
            ));
            private static final Set<String> implementationsWeapon = new HashSet<>(Set.of(
                    "com.threerings.projectx.item.config.ItemConfig$Handgun",
                    "com.threerings.projectx.item.config.ItemConfig$SwingingHandgun",
                    "com.threerings.projectx.item.config.ItemConfig$Sword"
            ));

            private static final Set<String> implementationsArmor = new HashSet<>(Set.of(
                    "com.threerings.projectx.item.config.ItemConfig$Armor",
                    "com.threerings.projectx.item.config.ItemConfig$Helm",
                    "com.threerings.projectx.item.config.ItemConfig$Shield"
            ));

            public static List<String> getItemVariants(String name, String implementation) {
                List<String> result = new ArrayList<>();
                result.add(name);
                Set<String> variants;
                if (implementationsBomb.contains(implementation)) { variants = variantsBomb; }
                else if (implementationsWeapon.contains(implementation)) { variants = variantsWeapon; }
                else if (implementationsArmor.contains(implementation)) { variants = variantsArmor; }
                else { return result; }
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
                "com.threerings.projectx.item.config.ItemConfig$Ticket",
                "com.threerings.projectx.item.config.ItemConfig$Upgrade",
                "com.threerings.projectx.item.config.ItemConfig$WrappingPaper",
                "com.threerings.projectx.design.config.FurniConfig$Prop",
                "com.threerings.projectx.design.config.FurniConfig$SpecialProp",
                "com.threerings.projectx.item.config.AccessoryConfig$Footstep",
                "com.threerings.projectx.item.config.AccessoryConfig$Original"
        ));
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
                "Exalted Honor Guard"
        ));

        Set<String> result = new HashSet<>();

        for (String name : indexRepository.getAllNameIndexKeys()) {
            // Check if name is blacklisted or null
            if (name == null || namesBlacklist.contains(name)) {
                continue;
            }

            // Check if implementation is whitelisted
            ConfigEntry configEntry = resolveConfigByName(name).get(0); // Get any entry, shouldn't matter for tradeable items
            if (!implementationsWhitelist.contains(configEntry.getEffectiveImplementation())) {
                continue;
            }

            // Check if known parameters marking items as untradeable exist
            if (!(configEntry.getRoutedParameters().resolveParameterPath("locked") == null)) {
                continue;
            }

            result.addAll(UniqueVariants.getItemVariants(name, configEntry.getEffectiveImplementation()));
        }
        return result;
    }

    @Cacheable("getIndexStats")
    public Map<String, Integer> getIndexStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Parsed Configs", indexRepository.getNumberIndexKeys());
        stats.put("Loaded Config Entries", indexRepository.getNumberConfigIndexKeys());
        stats.put("Named Config Entries", indexRepository.getNumberNameConfigKeys());
        return stats;
    }
}
