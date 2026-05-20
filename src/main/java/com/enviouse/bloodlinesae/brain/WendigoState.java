package com.enviouse.bloodlinesae.brain;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum WendigoState implements StringRepresentable {
    DORMANT("dormant"),
    STALKING("stalking"),
    REVEALING("revealing"),
    HUNTING("hunting"),
    LOSING_SCENT("losing_scent"),
    STUNNED("stunned");

    public static final Codec<WendigoState> CODEC = StringRepresentable.fromEnum(WendigoState::values);

    private final String name;

    WendigoState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static WendigoState fromOrdinal(int ord) {
        WendigoState[] values = values();
        if (ord < 0 || ord >= values.length) return DORMANT;
        return values[ord];
    }
}
