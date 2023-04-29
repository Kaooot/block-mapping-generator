package com.github.kaooot.mapping.updater;

import com.github.kaooot.mapping.property.BlockStateProperty;
import com.github.kaooot.mapping.property.BlockStatePropertyType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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
public class AddedPropertiesUpdater implements Updater {

    Map<String, List<BlockStateProperty>> addedProperties;

    public static AddedPropertiesUpdater build(JsonObject jsonObject) {
        final Map<String, List<BlockStateProperty>> addedProperties = new LinkedHashMap<>();

        for (final Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            final JsonObject state = entry.getValue().getAsJsonObject();

            final List<BlockStateProperty> properties = new ArrayList<>();

            for (final Map.Entry<String, JsonElement> propertyEntry : state.entrySet()) {
                final String name = entry.getKey();
                final JsonObject value = propertyEntry.getValue().getAsJsonObject();
                final BlockStatePropertyType type = BlockStatePropertyType.from(value);

                properties.add(switch (type) {
                    case INT ->
                        new BlockStateProperty(name, type, value.get(type.jsonName()).getAsInt());
                    case STRING -> new BlockStateProperty(name, type,
                        value.get(type.jsonName()).getAsString());
                    case BYTE -> new BlockStateProperty(name, type,
                        value.get(type.jsonName()).getAsByte());
                });
            }

            addedProperties.put(entry.getKey(), Collections.unmodifiableList(properties));
        }

        return new AddedPropertiesUpdater(Collections.unmodifiableMap(addedProperties));
    }

    @Override
    public void update(NbtMapBuilder blockState) {
        if (!blockState.containsKey("name") && !blockState.containsKey("states")) {
            return;
        }

        final String name = blockState.get("name").toString();
        final NbtMapBuilder states = ((NbtMap) blockState.get("states")).toBuilder();

        if (!this.addedProperties.containsKey(name)) {
            return;
        }

        for (final BlockStateProperty property : this.addedProperties.get(name)) {
            switch (property.type()) {
                case INT -> states.putInt(property.name(), property.intValue());
                case STRING -> states.putString(property.name(), property.stringValue());
                case BYTE -> states.putByte(property.name(), property.byteValue());
            }
        }

        blockState.putCompound("states", states.build());
    }
}