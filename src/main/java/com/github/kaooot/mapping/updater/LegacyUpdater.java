package com.github.kaooot.mapping.updater;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtUtils;
import org.cloudburstmc.nbt.util.VarInts;

/**
 * @author Kaooot
 * @version 1.0
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LegacyUpdater implements Updater {

    Map<String, List<R12ToR18Block>> r12ToR18Blocks;

    public static LegacyUpdater build(InputStream inputStream) {
        final Map<String, List<R12ToR18Block>> r12ToR18Blocks = new HashMap<>();

        try (final DataInputStream dataInputStream = new DataInputStream(inputStream);
             final NBTInputStream nbtInputStream = NbtUtils.createReaderLE(inputStream)) {

            final int n = VarInts.readUnsignedInt(dataInputStream);

            for (int i = 0; i < n; i++) {
                final byte[] r12BlockNameData = new byte[VarInts.readUnsignedInt(dataInputStream)];

                dataInputStream.read(r12BlockNameData);

                final String r12BlockName = new String(r12BlockNameData);

                final int length = VarInts.readUnsignedInt(dataInputStream);
                final List<MetadataBlockStatePair<Integer, NbtMap>> pairs = new ArrayList<>();

                for (int j = 0; j < length; j++) {
                    final int metadata = VarInts.readUnsignedInt(dataInputStream);
                    final NbtMap blockState = (NbtMap) nbtInputStream.readTag();

                    pairs.add(new MetadataBlockStatePair<>(metadata, blockState));
                }

                r12ToR18Blocks.computeIfAbsent(r12BlockName, k -> new ArrayList<>())
                    .add(new R12ToR18Block(r12BlockName, pairs));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        r12ToR18Blocks.replaceAll((k, v) -> Collections.unmodifiableList(v));

        return new LegacyUpdater(Collections.unmodifiableMap(r12ToR18Blocks));
    }

    @Override
    public void update(NbtMapBuilder blockState) {
        if (blockState.containsKey("states")) {
            return;
        }

        final String name = blockState.get("name").toString();
        final Integer metadata = (Integer) blockState.remove("val");

        if (metadata == null) {
            return;
        }

        if (this.r12ToR18Blocks.containsKey(name)) {
            for (final R12ToR18Block r12ToR18Block : this.r12ToR18Blocks.get(name)) {
                for (final MetadataBlockStatePair<Integer, NbtMap> pair : r12ToR18Block.pairs) {
                    if (Objects.equals(pair.metadata, metadata)) {
                        blockState.putCompound("states",
                            pair.blockState.getCompound("states").toBuilder().build());

                        return;
                    }
                }
            }
        }
    }

    @Value
    private static class R12ToR18Block {
        String r12BlockName;
        List<MetadataBlockStatePair<Integer, NbtMap>> pairs;
    }

    @Value
    private static class MetadataBlockStatePair<K, V> {
        K metadata;
        V blockState;
    }
}