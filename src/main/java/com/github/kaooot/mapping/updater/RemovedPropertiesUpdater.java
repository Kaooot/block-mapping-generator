package com.github.kaooot.mapping.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;

/**
 * @author Kaooot
 * @version 1.0
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RemovedPropertiesUpdater implements Updater {

    Map<String, List<String>> removedProperties;

    public static RemovedPropertiesUpdater build(JsonObject jsonObject) {
        final Map<String, List<String>> removedProperties = new HashMap<>();

        for (final Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            final JsonArray jsonArray = entry.getValue().getAsJsonArray();
            final List<String> properties = new LinkedList<>();

            for (int i = 0; i < jsonArray.size(); i++) {
                properties.add(jsonArray.get(i).getAsString());
            }

            removedProperties.put(entry.getKey(), Collections.unmodifiableList(properties));
        }

        return new RemovedPropertiesUpdater(Collections.unmodifiableMap(removedProperties));
    }

    @Override
    public void update(NbtMapBuilder blockState) {
        if (!blockState.containsKey("name") && !blockState.containsKey("states")) {
            return;
        }

        final String name = blockState.get("name").toString();
        final NbtMapBuilder states = ((NbtMap) blockState.get("states")).toBuilder();

        if (!this.removedProperties.containsKey(name)) {
            return;
        }

        final List<String> removedProperties = this.removedProperties.get(name);

        for (final String removedProperty : removedProperties) {
            states.remove(removedProperty);
        }

        blockState.putCompound("states", states.build());
    }
}