package com.github.kaooot.mapping.updater;

import com.github.kaooot.mapping.property.BlockStateProperty;
import com.github.kaooot.mapping.property.BlockStatePropertyType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
public class RemappedStatesUpdater implements Updater {

    Map<String, List<RemappedState>> remappedStates;

    public static RemappedStatesUpdater build(JsonObject jsonObject) {
        final Map<String, List<RemappedState>> remappedStates = new HashMap<>();

        for (final Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            final JsonArray jsonArray = entry.getValue().getAsJsonArray();
            final List<RemappedState> remapped = new ArrayList<>();

            for (int i = 0; i < jsonArray.size(); i++) {
                final JsonObject remappedStateObject = jsonArray.get(i).getAsJsonObject();
                final JsonObject oldState = remappedStateObject.getAsJsonObject("oldState");
                final String newName = remappedStateObject.get("newName").getAsString();

                JsonObject newState = null;

                if (remappedStateObject.get("newState").isJsonObject()) {
                    newState = remappedStateObject.getAsJsonObject("newState");
                }

                final List<BlockStateProperty> oldProperties = new ArrayList<>();
                final List<BlockStateProperty> newProperties = new ArrayList<>();

                for (final Map.Entry<String, JsonElement> oldStateEntry : oldState.entrySet()) {
                    final String propertyName = oldStateEntry.getKey();
                    final JsonObject oldProperty = oldStateEntry.getValue().getAsJsonObject();
                    final BlockStatePropertyType oldPropertyType =
                        BlockStatePropertyType.from(oldProperty);

                    oldProperties.add(switch (oldPropertyType) {
                        case INT -> new BlockStateProperty(propertyName, oldPropertyType,
                            oldProperty.get(oldPropertyType.jsonName()).getAsInt());
                        case STRING -> new BlockStateProperty(propertyName, oldPropertyType,
                            oldProperty.get(oldPropertyType.jsonName()).getAsString());
                        case BYTE -> new BlockStateProperty(propertyName, oldPropertyType,
                            oldProperty.get(oldPropertyType.jsonName()).getAsByte());
                    });
                }

                if (newState != null) {
                    for (final Map.Entry<String, JsonElement> newStateEntry : newState.entrySet()) {
                        final String propertyName = newStateEntry.getKey();
                        final JsonObject newProperty = newState.get(propertyName).getAsJsonObject();
                        final BlockStatePropertyType newPropertyType =
                            BlockStatePropertyType.from(newProperty);

                        newProperties.add(switch (newPropertyType) {
                            case INT -> new BlockStateProperty(propertyName, newPropertyType,
                                newProperty.get(newPropertyType.jsonName()).getAsInt());
                            case STRING -> new BlockStateProperty(propertyName, newPropertyType,
                                newProperty.get(newPropertyType.jsonName()).getAsString());
                            case BYTE -> new BlockStateProperty(propertyName, newPropertyType,
                                newProperty.get(newPropertyType.jsonName()).getAsByte());
                        });
                    }
                }

                final List<String> copiedStates = new ArrayList<>();

                if (remappedStateObject.has("copiedState")) {
                    final JsonArray copiedStatesArray = remappedStateObject.getAsJsonArray(
                        "copiedState");

                    for (final JsonElement element : copiedStatesArray) {
                        copiedStates.add(element.getAsString());
                    }
                }

                remapped.add(new RemappedState(Collections.unmodifiableList(oldProperties),
                    newName, Collections.unmodifiableList(newProperties),
                    Collections.unmodifiableList(copiedStates)));
            }

            remappedStates.put(entry.getKey(), remapped);
        }

        return new RemappedStatesUpdater(Collections.unmodifiableMap(remappedStates));
    }

    @Override
    public void update(NbtMapBuilder blockState) {
        if (!blockState.containsKey("name") && !blockState.containsKey("states")) {
            return;
        }

        final String name = blockState.get("name").toString();
        final NbtMapBuilder states = ((NbtMap) blockState.get("states")).toBuilder();

        if (!this.remappedStates.containsKey(name)) {
            return;
        }

        final List<RemappedState> remappedStates = this.remappedStates.get(name);

        for (final RemappedState remappedState : remappedStates) {
            boolean found = true;

            outer:
            for (final BlockStateProperty property : remappedState.oldState) {
                if (!states.containsKey(property.name())) {
                    found = false;

                    break;
                }

                switch (property.type()) {
                    case INT -> {
                        if ((int) states.get(property.name()) != property.intValue()) {
                            found = false;

                            break outer;
                        }
                    }
                    case STRING -> {
                        if (!states.get(property.name()).equals(property.stringValue())) {
                            found = false;

                            break outer;
                        }
                    }
                    case BYTE -> {
                        if ((byte) states.get(property.name()) != property.byteValue()) {
                            found = false;

                            break outer;
                        }
                    }
                }
            }

            if (found) {
                blockState.putString("name", remappedState.newName);

                final NbtMapBuilder newStates = NbtMap.builder();

                for (final BlockStateProperty property : remappedState.newState) {
                    switch (property.type()) {
                        case INT -> newStates.putInt(property.name(), property.intValue());
                        case STRING -> newStates.putString(property.name(), property.stringValue());
                        case BYTE -> newStates.putByte(property.name(), property.byteValue());
                    }
                }

                for (final String copiedProperty : remappedState.copiedState) {
                    newStates.put(copiedProperty, states.get(copiedProperty));
                }

                blockState.putCompound("states", newStates.build());

                return;
            }
        }
    }

    @Value
    @Accessors(fluent = true)
    private static class RemappedState {
        List<BlockStateProperty> oldState;
        String newName;
        List<BlockStateProperty> newState;
        List<String> copiedState;
    }
}