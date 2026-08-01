package com.ianeli.camerakey.data;

import com.ianeli.camerakey.CameraKeyMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class SavedSequences extends SavedData {

    private final Map<String, Sequence> sequences = new HashMap<>();

    public static final Codec<Map<String, Sequence>> MAP_CODEC =
            Codec.unboundedMap(Codec.STRING, Sequence.CODEC.codec());

    public static SavedSequences create() {
        return new SavedSequences();
    }

    public static SavedSequences get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(SavedSequences::create, SavedSequences::load),
                "camerakey_saved_sequences"
        );
    }

    public static SavedSequences load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        SavedSequences data = create();
        Tag sequencesTag = tag.get("sequences");
        if (sequencesTag != null) {
            MAP_CODEC.parse(NbtOps.INSTANCE, sequencesTag)
                    .resultOrPartial(err -> CameraKeyMod.LOGGER.error("Failed to parse camerakey sequences: {}", err))
                    .ifPresent(data.sequences::putAll);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        MAP_CODEC.encodeStart(NbtOps.INSTANCE, sequences)
                .resultOrPartial(err -> CameraKeyMod.LOGGER.error("Failed to save camerakey sequences: {}", err))
                .ifPresent(tag -> compoundTag.put("sequences", tag));
        return compoundTag;
    }

    public void addSequence(Sequence sequence) {
        sequences.put(sequence.name(), sequence);
        this.setDirty();
    }
    public void removeSequence(String name) {
        sequences.remove(name);
        this.setDirty();
    }
    public boolean doesSequenceExist(String name) {
        return sequences.containsKey(name);
    }
    public Optional<Sequence> getSequence(String name) {
        return Optional.ofNullable(sequences.get(name));
    }
    public Iterable<String> getSequenceNames() {
        return sequences.keySet();
    }
    public int getSequenceCount() {
        return sequences.size();
    }
}
