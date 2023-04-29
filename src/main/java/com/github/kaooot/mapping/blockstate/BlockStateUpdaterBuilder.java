package com.github.kaooot.mapping.blockstate;

import com.github.kaooot.mapping.updater.LegacyUpdater;
import com.github.kaooot.mapping.updater.Updater;
import com.github.kaooot.mapping.util.Util;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;

/**
 * @author Kaooot
 * @version 1.0
 */
public class BlockStateUpdaterBuilder {

    private static final List<BlockStateUpdater> UPDATERS;
    private static final Gson GSON = new Gson();

    static {
        UPDATERS = Arrays.asList(
            buildLegacy(),
            build("0001_1.9.0_to_1.10.0.json"),
            build("0011_1.10.0_to_1.12.0.json"),
            build("0021_1.12.0_to_1.13.0.json"),
            build("0031_1.13.0_to_1.14.0.json"),
            build("0041_1.14.0_to_1.16.0.57_beta.json"),
            build("0051_1.16.0.57_beta_to_1.16.0.59_beta.json"),
            build("0061_1.16.0.59_beta_to_1.16.0.68_beta.json"),
            build("0071_1.16.0_to_1.16.100.json"),
            build("0081_1.16.200_to_1.16.210.json"),
            build("0091_1.17.10_to_1.17.30.json"),
            build("0101_1.17.30_to_1.17.40.json"),
            build("0111_1.18.0_to_1.18.10.json"),
            build("0121_1.18.10_to_1.18.20.27_beta.json"),
            build("0131_1.18.20.27_beta_to_1.18.30.json"),
            build("0141_1.18.30_to_1.19.0.34_beta.json"),
            build("0151_1.19.0.34_beta_to_1.19.20.json"),
            build("0161_1.19.50_to_1.19.60.26_beta.json"),
            build("0171_1.19.60_to_1.19.70.26_beta.json"),
            build("0181_1.19.70_to_1.19.80.24_beta.json")
        );
    }

    public static NbtMap update(NbtMap blockState, int targetVersion) {
        final NbtMapBuilder builder = blockState.toBuilder();
        BlockStateUpdater lastUpdater = null;

        for (BlockStateUpdater updater : BlockStateUpdaterBuilder.UPDATERS) {
            if (updater.version() > targetVersion ||
                (int) builder.getOrDefault("version", 0) > updater.version()) {
                continue;
            }

            for (final Updater u : updater.updaters()) {
                u.update(builder);
            }

            lastUpdater = updater;
        }

        if (lastUpdater != null) {
            builder.putInt("version", lastUpdater.version());
        }

        return builder.build();
    }

    private static BlockStateUpdater buildLegacy() {
        try (final InputStream inputStream = BlockStateUpdaterBuilder.class.getClassLoader()
            .getResourceAsStream(
                "BedrockBlockUpgradeSchema/1.12.0_to_1.18.10_blockstate_map.bin")) {
            if (inputStream != null) {
                return new BlockStateUpdater(Util.blockPaletteVersion(1, 18, 10, 1),
                    Collections.singleton(LegacyUpdater.build(inputStream)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Could not retrieve LegacyUpdater");
    }

    private static BlockStateUpdater build(String fileName) {
        try (final InputStream inputStream =
                 BlockStateUpdaterBuilder.class.getClassLoader().getResourceAsStream(
                     "BedrockBlockUpgradeSchema/nbt_upgrade_schema/" + fileName)) {
            if (inputStream != null) {
                return BlockStateUpdater.fromJson(
                    BlockStateUpdaterBuilder.GSON.fromJson(new InputStreamReader(inputStream),
                        JsonObject.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Could not retrieve BlockStateUpdater from file " + fileName);
    }
}