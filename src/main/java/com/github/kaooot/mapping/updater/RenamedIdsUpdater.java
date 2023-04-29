package com.github.kaooot.mapping.updater;

import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.cloudburstmc.nbt.NbtMapBuilder;

/**
 * @author Kaooot
 * @version 1.0
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RenamedIdsUpdater implements Updater {

    Map<String, String> renamedIds;

    public static RenamedIdsUpdater build(JsonObject jsonObject) {
        final Map<String, String> renamedIds = new HashMap<>();

        for (final String key : jsonObject.keySet()) {
            renamedIds.put(key, jsonObject.get(key).getAsString());
        }

        return new RenamedIdsUpdater(Collections.unmodifiableMap(renamedIds));
    }

    @Override
    public void update(NbtMapBuilder blockState) {
        if (!blockState.containsKey("name")) {
            return;
        }

        final String id = blockState.get("name").toString();

        if (this.renamedIds.containsKey(id)) {
            blockState.putString("name", this.renamedIds.get(id));
        }
    }
}