package com.github.kaooot.mapping.updater;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.HashMap;
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
public class RenamedPropertiesUpdater implements Updater {

    Map<String, Map<String, String>> renamedProperties;

    public static RenamedPropertiesUpdater build(JsonObject jsonObject) {
        final Map<String, Map<String, String>> renamedProperties = new HashMap<>();

        for (final Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            final JsonObject block = entry.getValue().getAsJsonObject();
            final Map<String, String> properties = new HashMap<>();

            for (final String property : block.keySet()) {
                properties.put(property, block.get(property).getAsString());
            }

            renamedProperties.put(entry.getKey(), Collections.unmodifiableMap(properties));
        }

        return new RenamedPropertiesUpdater(Collections.unmodifiableMap(renamedProperties));
    }

    @Override
    public void update(NbtMapBuilder blockState) {
        if (!blockState.containsKey("name") && !blockState.containsKey("states")) {
            return;
        }

        final String name = blockState.get("name").toString();
        final NbtMapBuilder states = ((NbtMap) blockState.get("states")).toBuilder();

        if (!this.renamedProperties.containsKey(name)) {
            return;
        }

        final Map<String, String> renamedProperties = this.renamedProperties.get(name);

        for (final String property : renamedProperties.keySet()) {
            states.rename(property, renamedProperties.get(property));
        }

        blockState.putCompound("states", states.build());
    }
}