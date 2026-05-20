package com.enviouse.bloodlinesae.lifecycle;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class WendigoAssignmentData extends SavedData {

    private static final String DATA_KEY = "bloodline_wendigo_assignments";

    private final Map<UUID, UUID> playerToWendigo = new HashMap<>();
    private final Map<UUID, Long> respawnSchedule = new HashMap<>();

    public static WendigoAssignmentData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                WendigoAssignmentData::load,
                WendigoAssignmentData::new,
                DATA_KEY
        );
    }

    public static WendigoAssignmentData load(CompoundTag tag) {
        WendigoAssignmentData data = new WendigoAssignmentData();
        ListTag list = tag.getList("Assignments", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            UUID player = entry.getUUID("Player");
            UUID wendigo = entry.getUUID("Wendigo");
            data.playerToWendigo.put(player, wendigo);
        }
        ListTag respawns = tag.getList("Respawns", Tag.TAG_COMPOUND);
        for (int i = 0; i < respawns.size(); i++) {
            CompoundTag entry = respawns.getCompound(i);
            data.respawnSchedule.put(entry.getUUID("Player"), entry.getLong("RespawnAt"));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, UUID> entry : this.playerToWendigo.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("Player", entry.getKey());
            entryTag.putUUID("Wendigo", entry.getValue());
            list.add(entryTag);
        }
        tag.put("Assignments", list);

        ListTag respawns = new ListTag();
        for (Map.Entry<UUID, Long> entry : this.respawnSchedule.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("Player", entry.getKey());
            entryTag.putLong("RespawnAt", entry.getValue());
            respawns.add(entryTag);
        }
        tag.put("Respawns", respawns);
        return tag;
    }

    public void assign(UUID player, UUID wendigo) {
        this.playerToWendigo.put(player, wendigo);
        this.respawnSchedule.remove(player);
        this.setDirty();
    }

    public void unassign(UUID player) {
        this.playerToWendigo.remove(player);
        this.setDirty();
    }

    public Optional<UUID> getWendigoFor(UUID player) {
        return Optional.ofNullable(this.playerToWendigo.get(player));
    }

    public UUID getPlayerForWendigo(UUID wendigo) {
        for (Map.Entry<UUID, UUID> entry : this.playerToWendigo.entrySet()) {
            if (entry.getValue().equals(wendigo)) return entry.getKey();
        }
        return null;
    }

    public void scheduleRespawn(UUID player, long gameTime) {
        this.respawnSchedule.put(player, gameTime);
        this.setDirty();
    }

    public boolean shouldRespawn(UUID player, long currentGameTime) {
        Long at = this.respawnSchedule.get(player);
        return at != null && currentGameTime >= at;
    }

    public void clearRespawn(UUID player) {
        if (this.respawnSchedule.remove(player) != null) {
            this.setDirty();
        }
    }
}
