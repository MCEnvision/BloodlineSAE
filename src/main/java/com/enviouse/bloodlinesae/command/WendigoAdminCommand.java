package com.enviouse.bloodlinesae.command;

import com.enviouse.bloodlinesae.entity.custom.WendigoEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class WendigoAdminCommand {

    private WendigoAdminCommand() {}

    public static final List<String> ANIMATIONS = List.of(
            "idle", "run2", "attack", "attack2", "roar", "ass_scratch"
    );

    private static final double SEARCH_RADIUS = 64.0D;

    private static final SuggestionProvider<CommandSourceStack> ANIMATION_SUGGESTIONS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(ANIMATIONS, builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("wendigoadmin")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("wendigo")
                                .then(Commands.literal("animate")
                                        .then(Commands.argument("animation", StringArgumentType.word())
                                                .suggests(ANIMATION_SUGGESTIONS)
                                                .executes(ctx -> runAnimate(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "animation"))))))
        );
    }

    private static int runAnimate(CommandSourceStack source, String animation) {
        if (!ANIMATIONS.contains(animation)) {
            source.sendFailure(Component.literal("Unknown animation: " + animation
                    + ". Valid: " + String.join(", ", ANIMATIONS)));
            return 0;
        }

        ServerLevel level = source.getLevel();
        AABB box = new AABB(source.getPosition(), source.getPosition()).inflate(SEARCH_RADIUS);
        List<WendigoEntity> nearby = level.getEntitiesOfClass(WendigoEntity.class, box, LivingEntity::isAlive);

        if (nearby.isEmpty()) {
            source.sendFailure(Component.literal("No wendigos within " + (int) SEARCH_RADIUS + " blocks."));
            return 0;
        }

        WendigoEntity closest = nearby.get(0);
        double bestDist = closest.distanceToSqr(source.getPosition());
        for (WendigoEntity w : nearby) {
            double d = w.distanceToSqr(source.getPosition());
            if (d < bestDist) {
                bestDist = d;
                closest = w;
            }
        }

        closest.triggerAnim(WendigoEntity.MAIN_CONTROLLER, animation);

        final WendigoEntity target = closest;
        source.sendSuccess(() -> Component.literal("Triggered '" + animation + "' on wendigo "
                + target.getUUID().toString().substring(0, 8) + "."), true);
        return 1;
    }
}
