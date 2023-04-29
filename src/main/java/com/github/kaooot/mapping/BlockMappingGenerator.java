package com.github.kaooot.mapping;

import com.github.kaooot.mapping.blockstate.BlockStateUpdaterBuilder;
import com.google.gson.GsonBuilder;
import java.io.EOFException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtUtils;

/**
 * @author Kaooot
 * @version 1.0
 */
public class BlockMappingGenerator {

    public static void main(String[] args) {
        if (args.length < 2) {
            return;
        }

        final List<NbtMap> sourcePalette = new ArrayList<>();
        final List<NbtMap> targetPalette = new ArrayList<>();

        BlockMappingGenerator.readPalette(args[0], sourcePalette);
        BlockMappingGenerator.readPalette(args[1], targetPalette);

        final Map<Integer, Integer> runtimeIdMapping = new HashMap<>();
        final int targetVersion = targetPalette.get(0).getInt("version");

        int sourceRuntimeId = 0;

        for (final NbtMap blockState : sourcePalette) {
            final int targetRuntimeId =
                targetPalette.indexOf(BlockStateUpdaterBuilder.update(blockState, targetVersion));

            if (targetRuntimeId == -1) {
                System.out.println("Cannot find targetRuntimeId for " + sourceRuntimeId);

                return;
            }

            runtimeIdMapping.put(sourceRuntimeId, targetRuntimeId);

            sourceRuntimeId++;
        }

        try (final FileWriter fileWriter = new FileWriter("mapping.json")) {
            new GsonBuilder().setPrettyPrinting().create().toJson(runtimeIdMapping, fileWriter);

            System.out.println("Mapping file generated");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readPalette(String url, List<NbtMap> palette) {
        try (final InputStream inputStream = new URL(url).openStream();
             final NBTInputStream nbtInputStream = NbtUtils.createNetworkReader(inputStream)) {

            while (true) {
                try {
                    palette.add((NbtMap) nbtInputStream.readTag());
                } catch (EOFException e) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}