package com.ianeli.camerakey.playback;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CameraAnchorEntity extends Entity {


    public CameraAnchorEntity(Level level) {
        super(EntityType.MARKER, level);
        this.noPhysics = true;
        this.setInvisible(true);
    }

    public void moveTo(Vec3 position, float yaw, float pitch) {
        this.setOldPosAndRot();
        this.setPos(position);
        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }
}
