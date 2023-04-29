package com.github.kaooot.mapping.util;

import com.github.kaooot.mapping.updater.AddedPropertiesUpdater;
import com.github.kaooot.mapping.updater.RemappedPropertyValuesUpdater;
import com.github.kaooot.mapping.updater.RemappedStatesUpdater;
import com.github.kaooot.mapping.updater.RemovedPropertiesUpdater;
import com.github.kaooot.mapping.updater.RenamedIdsUpdater;
import com.github.kaooot.mapping.updater.RenamedPropertiesUpdater;
import com.github.kaooot.mapping.updater.Updater;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kaooot
 * @version 1.0
 */
public class Util {

    public static int blockPaletteVersion(int major, int minor, int patch, int revision) {
        return (major << 24) | (minor << 16) | (patch << 8) | revision;
    }

    public static Set<Updater> updaters(JsonObject jsonObject) {
        final Set<Updater> updaters = new HashSet<>();

        for (final String key : jsonObject.keySet()) {
            if (key.equals("renamedIds")) {
                updaters.add(RenamedIdsUpdater.build(jsonObject.getAsJsonObject("renamedIds")));

                continue;
            }

            if (key.equals("renamedProperties")) {
                updaters.add(RenamedPropertiesUpdater.build(jsonObject.getAsJsonObject(
                    "renamedProperties")));

                continue;
            }

            if (key.equals("addedProperties")) {
                updaters.add(
                    AddedPropertiesUpdater.build(jsonObject.getAsJsonObject("addedProperties")));

                continue;
            }

            if (key.equals("removedProperties")) {
                updaters.add(RemovedPropertiesUpdater.build(jsonObject.getAsJsonObject(
                    "removedProperties")));

                continue;
            }

            if (key.equals("remappedPropertyValues") && jsonObject.has(
                "remappedPropertyValuesIndex")) {
                updaters.add(RemappedPropertyValuesUpdater.build(jsonObject.getAsJsonObject(
                        "remappedPropertyValues"),
                    jsonObject.getAsJsonObject("remappedPropertyValuesIndex")));

                continue;
            }

            if (key.equals("remappedStates")) {
                updaters.add(
                    RemappedStatesUpdater.build(jsonObject.getAsJsonObject("remappedStates")));
            }
        }

        return updaters.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(updaters);
    }
}