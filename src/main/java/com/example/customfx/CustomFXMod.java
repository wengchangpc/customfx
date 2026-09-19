package com.example.customfx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CustomFXMod.MODID)
public class CustomFXMod {
    public static final String MODID = "customfx";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public CustomFXMod() {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            // 纯客户端模组：专用服务器上什么都不做
            return;
        }

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onClientSetup);
        modBus.addListener(this::onAddLayers);

        FxConfig.load();
        LOGGER.info("[CustomFX] 已初始化：发光描边 / 彩色名字 / 旋转光环，全部仅自己可见。");
    }

    /** 给默认与 slim 两种玩家渲染器挂上光环层 */
    private void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new HaloLayer(renderer));
            }
        }
        LOGGER.info("[CustomFX] 光环渲染层已挂载。");
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        Minecraft.getInstance().execute(() -> LOGGER.info("[CustomFX] 客户端就绪。"));
    }

    /** 客户端游戏事件：tick 发光描边、名字染色、指令注册 */
    @Mod.EventBusSubscriber(modid = CustomFXMod.MODID, value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null || mc.level == null) {
                FxConfig.setGlowApplied(false);
                return;
            }
            if (FxConfig.glow) {
                // 只改本地客户端的实体标记，不会同步给服务器或其他玩家
                player.setGlowingTag(true);
                FxConfig.setGlowApplied(true);
            } else if (FxConfig.isGlowApplied()) {
                player.setGlowingTag(false);
                FxConfig.setGlowApplied(false);
            }
        }

        @SubscribeEvent
        public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            FxConfig.setGlowApplied(false);
        }

        /** 只给自己的名字染色（改的是本地渲染内容，服务器零感知） */
        @SubscribeEvent
        public static void onNameTag(RenderNameTagEvent event) {
            if (!FxConfig.nameColorEnabled) return;
            Minecraft mc = Minecraft.getInstance();
            if (event.getEntity() != mc.player) return;

            long time = System.currentTimeMillis();
            int rgb = FxConfig.nameColor(time);
            String base = event.getContent() != null ? event.getContent().getString()
                    : mc.player.getGameProfile().getName();
            MutableComponent colored = Component.literal(base)
                    .withStyle(Style.EMPTY.withColor(TextColor.parseColor(String.format("#%06X", rgb))));
            event.setContent(colored);
        }

        @SubscribeEvent
        public static void onRegisterCommands(RegisterClientCommandsEvent event) {
            event.getDispatcher().register(Commands.literal("customfx")
                .then(Commands.literal("reload").executes(ctx -> {
                    FxConfig.load();
                    ctx.getSource().sendSuccess(() -> Component.literal(String.format(
                            "[CustomFX] 已重载：glow=%s halo=%s name=%s haloColor=%s nameColor=%s",
                            FxConfig.glow, FxConfig.halo, FxConfig.nameColorEnabled,
                            FxConfig.haloColorMode, FxConfig.nameColorMode)), false);
                    return 1;
                }))
                .then(Commands.literal("glow").executes(ctx -> {
                    FxConfig.glow = !FxConfig.glow;
                    FxConfig.save();
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "[CustomFX] 发光描边：" + (FxConfig.glow ? "开" : "关")), false);
                    return 1;
                }))
                .then(Commands.literal("halo").executes(ctx -> {
                    FxConfig.halo = !FxConfig.halo;
                    FxConfig.save();
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "[CustomFX] 旋转光环：" + (FxConfig.halo ? "开" : "关")), false);
                    return 1;
                }))
                .then(Commands.literal("name").executes(ctx -> {
                    FxConfig.nameColorEnabled = !FxConfig.nameColorEnabled;
                    FxConfig.save();
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "[CustomFX] 彩色名字：" + (FxConfig.nameColorEnabled ? "开" : "关")), false);
                    return 1;
                })));
        }
    }
}
