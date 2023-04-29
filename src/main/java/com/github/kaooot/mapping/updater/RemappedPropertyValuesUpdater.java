package com.github.kaooot.mapping.updater;

import com.github.kaooot.mapping.property.BlockStateProperty;
import com.github.kaooot.mapping.property.BlockStatePropertyType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;

/**
 * @author Kaooot
 * @version 1.0
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RemappedPropertyValuesUpdater implements Updater {

    Map<String, Map<String, String>> remappedPropertyValues;
    Map<String, List<Pair<BlockStateProperty, BlockStateProperty>>> remappedPropertyValuesIndex;

    public static RemappedPropertyValuesUpdater build(JsonObject valuesObject,
                                                      JsonObject indexObject) {
        final Map<String, Map<String, String>> remappedPropertyValues = new HashMap<>();
        final Map<String, List<Pair<BlockStateProperty, BlockStateProperty>>>
            remappedPropertyValuesIndex = new LinkedHashMap<>();

        for (final Map.Entry<String, JsonElement> entry : valuesObject.entrySet()) {
            final JsonObject state = entry.getValue().getAsJsonObject();
            final Map<String, String> remappedProperties = new HashMap<>();

            for (final Map.Entry<String, JsonElement> propertyEntry : state.entrySet()) {
                final String propertyIndex = propertyEntry.getValue().getAsString();

                remappedProperties.put(propertyEntry.getKey(), propertyIndex);
            }

            remappedPropertyValues.put(entry.getKey(),
                Collections.unmodifiableMap(remappedProperties));
        }

        for (final Map.Entry<String, JsonElement> entry : indexObject.entrySet()) {
            final JsonArray jsonArray = entry.getValue().getAsJsonArray();
            final List<Pair<BlockStateProperty, BlockStateProperty>> pairs = new LinkedList<>();

            for (int i = 0; i < jsonArray.size(); i++) {
                final JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                final JsonObject oldProperty = jsonObject.getAsJsonObject("old");
                final JsonObject newProperty = jsonObject.getAsJsonObject("new");
                final BlockStatePropertyType oldPropertyType =
                    BlockStatePropertyType.from(oldProperty);
                final BlockStatePropertyType newPropertyType =
                    BlockStatePropertyType.from(newProperty);

                pairs.add(new Pair<>(new BlockStateProperty("", oldPropertyType,
                    switch (oldPropertyType) {
                        case INT -> oldProperty.get(oldPropertyType.jsonName()).getAsInt();
                        case STRING -> oldProperty.get(oldPropertyType.jsonName()).getAsString();
                        case BYTE -> oldProperty.get(oldPropertyType.jsonName()).getAsByte();
                    }),
                    new BlockStateProperty("", newPropertyType,
                        switch (newPropertyType) {
                            case INT -> newProperty.get(newPropertyType.jsonName()).getAsInt();
                            case STRING ->
                                newProperty.get(newPropertyType.jsonName()).getAsString();
                            case BYTE -> newProperty.get(newPropertyType.jsonName()).getAsByte();
                        })));
            }

            remappedPropertyValuesIndex.put(entry.getKey(), Collections.unmodifiableList(pairs));
        }

        return new RemappedPropertyValuesUpdater(
            Collections.unmodifiableMap(remappedPropertyValues),
            Collections.unmodifiableMap(remappedPropertyValuesIndex));
    }

    @Override
    public void update(NbtMapBuilder blockState) {
        if (!blockState.containsKey("name") && !blockState.containsKey("states")) {
            return;
        }

        final String name = blockState.get("name").toString();
        final NbtMapBuilder states = ((NbtMap) blockState.get("states")).toBuilder();

        if (!this.remappedPropertyValues.containsKey(name)) {
            return;
        }

        final Map<String, String> remappedPropertyValues = this.remappedPropertyValues.get(name);

        boolean modified = false;

        for (final Map.Entry<String, Object> entry : states.entrySet()) {
            final String propertyName = entry.getKey();
            final String propertyIndex = remappedPropertyValues.get(propertyName);

            if (propertyIndex == null) {
                continue;
            }

            boolean currentModified = false;

            for (final Pair<BlockStateProperty, BlockStateProperty> pair : this.remappedPropertyValuesIndex.get(
                propertyIndex)) {
                switch (pair.oldProperty.type()) {
                    case INT -> {
                        if ((int) entry.getValue() == pair.oldProperty.intValue()) {
                            entry.setValue(pair.newProperty.intValue());

                            currentModified = true;
                        }
                    }
                    case STRING -> {
                        if (entry.getValue().equals(pair.oldProperty.stringValue())) {
                            entry.setValue(pair.newProperty.stringValue());

                            currentModified = true;
                        }
                    }
                    case BYTE -> {
                        if ((byte) entry.getValue() == pair.oldProperty.byteValue()) {
                            entry.setValue(pair.newProperty.byteValue());

                            currentModified = true;
                        }
                    }
                }

                if (currentModified) {
                    break;
                }
            }

            if (currentModified) {
                modified = true;
            }
        }

        if (modified) {
            blockState.putCompound("states", states.build());
        }
    }

    @Value
    @Accessors(fluent = true)
    private static class Pair<K, V> {
        K oldProperty;
        V newProperty;
    }
}