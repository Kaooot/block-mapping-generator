package com.github.kaooot.mapping.updater;

import org.cloudburstmc.nbt.NbtMapBuilder;

/**
 * @author Kaooot
 * @version 1.0
 */
public interface Updater {

    /**
     * Updates the given block state
     *
     * @param blockState that should be updated
     */
    void update(NbtMapBuilder blockState);
}