package com.github.kaooot.mapping.blockstate;

import com.github.kaooot.mapping.updater.Updater;
import com.github.kaooot.mapping.util.Util;
import com.google.gson.JsonObject;
import java.util.Set;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * @author Kaooot
 * @version 1.0
 */
@Value
@Getter
@Accessors(fluent = true)
public class BlockStateUpdater {

    int version;
    Set<Updater> updaters;

    public static BlockStateUpdater fromJson(JsonObject jsonObject) {
        final int major = jsonObject.get("maxVersionMajor").getAsInt();
        final int minor = jsonObject.get("maxVersionMinor").getAsInt();
        final int patch = jsonObject.get("maxVersionPatch").getAsInt();
        final int revision = jsonObject.get("maxVersionRevision").getAsInt();

        return new BlockStateUpdater(Util.blockPaletteVersion(major, minor, patch, revision),
            Util.updaters(jsonObject));
    }
}