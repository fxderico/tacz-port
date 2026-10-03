plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.11"

stonecutter parameters {
    // "1.21.11" is Fabric, "1.21.11-neoforge" NeoForge: `//? if fabric {`, `//? if neoforge {`
    val loader = current.project.substringAfter('-', "fabric")
    constants.match(loader, "fabric", "neoforge", "forge")

    replacements {
        // before 1.21.6 NBT was read and written as CompoundTags, TACZ's stand-ins mimic the new value API over them
        string(current.parsed < "1.21.6") {
            replace("net.minecraft.world.level.storage.ValueInput", "com.tacz.guns.platform.compat.ValueInput")
            replace("net.minecraft.world.level.storage.ValueOutput", "com.tacz.guns.platform.compat.ValueOutput")
            // GUI drawing was immediate: a PoseStack instead of the 2D matrix stack, render type functions instead of pipelines
            replace("import org.joml.Matrix3x2fStack;", "import com.tacz.guns.platform.client.compat.Matrix3x2fStack;")
            replace("Matrix3x2fStack poseStack = graphics.pose();", "Matrix3x2fStack poseStack = new Matrix3x2fStack(graphics.pose());")
            replace("Matrix3x2fStack poseStack = gui.pose();", "Matrix3x2fStack poseStack = new Matrix3x2fStack(gui.pose());")
            replace("graphics.blit(RenderPipelines.GUI_TEXTURED,", "graphics.blit(net.minecraft.client.renderer.RenderType::guiTextured,")
            replace("setComponentTooltipForNextFrame(", "renderComponentTooltip(")
            replace("graphics.setTooltipForNextFrame(", "graphics.renderTooltip(")
            replace("SharedConstants.getCurrentVersion().name()", "SharedConstants.getCurrentVersion().getName()")
            // uniforms were declared one by one before the 1.21.6 uniform blocks
            replace("RenderPipelines.MATRICES_PROJECTION_SNIPPET", "RenderPipelines.MATRICES_COLOR_SNIPPET")
            replace("TextureStateShard(texture, false)", "TextureStateShard(texture, net.minecraft.util.TriState.FALSE, false)")
            // textures had no usage flags or layers before 1.21.6
            replace("createTexture(Ljava/lang/String;ILcom/mojang/blaze3d/textures/TextureFormat;IIII)", "createTexture(Ljava/lang/String;Lcom/mojang/blaze3d/textures/TextureFormat;III)")
        }
        // before 1.21.9: no submit pass, TACZ's stand-in collector carries the buffer source (platform.client.compat)
        string(current.parsed < "1.21.9") {
            replace("net.minecraft.client.renderer.SubmitNodeCollector", "com.tacz.guns.platform.client.compat.SubmitNodeCollector")
            // players and mannequins share AvatarRenderer since 1.21.9
            // (full names: rules also run backwards on newer versions, a bare "PlayerRenderer" would hit getPlayerRenderer)
            replace("AvatarRenderer<net.minecraft.client.player.AbstractClientPlayer>", "net.minecraft.client.renderer.entity.player.PlayerRenderer")
            replace("import net.minecraft.client.renderer.entity.player.AvatarRenderer;", "import net.minecraft.client.renderer.entity.player.PlayerRenderer;")
            // before the 1.21.9 KeyEvent/MouseButtonEvent input records: raw key codes and argumentless button presses
            replace(".matches(event.getKeyEvent())", ".matches(event.getKey(), event.getScanCode())")
            replace(".matchesMouse(event.getMouseButtonEvent())", ".matchesMouse(event.getButton())")
            replace("onPress(net.minecraft.client.input.InputWithModifiers input)", "onPress()")
            replace("super.onPress(input);", "super.onPress();")
            replace("net.minecraft.client.Minecraft.getInstance().hasShiftDown()", "net.minecraft.client.gui.screens.Screen.hasShiftDown()")
            replace(".sendToTrackingPlayers(entity,", ".broadcast(entity,")
            replace("minecraft.playerSkinRenderCache().getOrDefault(blockEntity.getOwner()).playerSkin().body().texturePath()", "minecraft.getSkinManager().getInsecureSkin(blockEntity.getOwner().gameProfile()).texture()")
            // what GameRenderer#bobView used before the 1.21.9 avatar state
            replace("acp.avatarState().getBackwardsInterpolatedWalkDistance(partialTicks)", "(acp.walkDist + (acp.walkDist - acp.walkDistO) * partialTicks)")
            replace("ResolvableProfile.createUnresolved(stack.getHoverName().getString())", "new ResolvableProfile(java.util.Optional.of(stack.getHoverName().getString()), java.util.Optional.empty(), new com.mojang.authlib.properties.PropertyMap())")
            replace(".requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))", ".requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))")
            replace(".sendToTrackingPlayersAndSelf(entity,", ".broadcastAndSend(entity,")
        }
        // 1.21.10 and older: ResourceLocation before its 1.21.11 rename to Identifier, render types before the rendertype package
        regex(current.parsed < "1.21.11") {
            replace("\\bIdentifier\\b", "ResourceLocation", "\\bResourceLocation\\b", "Identifier")
        }
        regex(current.parsed < "1.21.11") {
            replace("\\bRenderTypes\\.", "RenderType.",
                "\\bRenderType\\.(entity|energySwirl|eyes|text|lines|lightning|translucent|cutout|solid|leash|beaconBeam|outline|itemEntity|armor|glint|gui|debug)", "RenderTypes.$1")
        }
        string(current.parsed < "1.21.11") {
            replace("import net.minecraft.client.renderer.rendertype.RenderTypes;", "import net.minecraft.client.renderer.*;")
            replace("import net.minecraft.client.renderer.rendertype.RenderType;", "import net.minecraft.client.renderer.RenderType;")
            replace("readIdentifier()", "readResourceLocation()")
            replace("writeIdentifier(", "writeResourceLocation(")
            replace(".identifier()", ".location()")
            replace("net.minecraft.util.Util", "net.minecraft.Util")
            replace("camera.yRot()", "camera.getYRot()")
            replace("camera.xRot()", "camera.getXRot()")
            replace("mainCamera.yRot()", "mainCamera.getYRot()")
            replace("mainCamera.xRot()", "mainCamera.getXRot()")
            replace("getCamera().entity()", "getCamera().getEntity()")
            replace("ARGB.srgbLerp(", "ARGB.lerp(")
            replace("net.minecraft.world.entity.boss.enderdragon.EnderDragonPart", "net.minecraft.world.entity.boss.EnderDragonPart")
            replace("net.minecraft.world.entity.monster.zombie.Husk", "net.minecraft.world.entity.monster.Husk")
            replace("net.minecraft.world.level.gamerules.GameRules", "net.minecraft.world.level.GameRules")
        }

        // NeoForge: its own dist marker and config spec (Fabric gets the Forge flavoured config from Forge Config API Port)
        string(loader == "neoforge") {
            // a do-nothing marker in place of Fabric's @Environment (NeoForge's @OnlyIn no longer strips and warns on every use)
            replace("net.fabricmc.api.EnvType", "com.tacz.guns.platform.marker.EnvType")
            replace("net.fabricmc.api.Environment", "com.tacz.guns.platform.marker.Environment")
            replace("import net.minecraftforge.common.ForgeConfigSpec;", "import net.neoforged.neoforge.common.ModConfigSpec;")
            replace("ForgeConfigSpec", "ModConfigSpec")
        }

        // 26.1+ ships without obfuscation, access wideners name things in the official namespace
        string(current.parsed >= "26.1") {
            replace("accessWidener v2 named", "accessWidener v2 official")
        }

        // Fabric API 26.1 renamed its Yarn flavoured names to Mojang's
        string(current.parsed >= "26.1") {
            replace("api.client.keybinding.v1", "api.client.keymapping.v1")
            replace("KeyBindingHelper", "KeyMappingHelper")
            replace("registerKeyBinding", "registerKeyMapping")
            replace("api.itemgroup.v1", "api.creativetab.v1")
            replace("FabricItemGroup", "FabricCreativeModeTab")
            replace("api.screenhandler.v1", "api.menu.v1")
            replace("ExtendedScreenHandlerFactory", "ExtendedMenuProvider")
            replace("ExtendedScreenHandlerType", "ExtendedMenuType")
            replace("ParticleFactoryRegistry", "ParticleProviderRegistry")
            replace("SpecialGuiElementRegistry", "PictureInPictureRendererRegistry")
            replace("TooltipComponentCallback", "ClientTooltipComponentCallback")
            replace("registerReloader(", "registerReloadListener(")
            replace("addReloaderOrdering(", "addListenerOrdering(")
            replace("ResourceLoader.RELOADER_REGISTRY_LOOKUP_KEY", "ResourceLoader.REGISTRY_LOOKUP_KEY")
            replace("createS2CPacket(", "createClientboundPacket(")
            replace("playS2C()", "clientboundPlay()")
            replace("playC2S()", "serverboundPlay()")
            replace("getClientWorld()", "getClientLevel()")
            replace("@Inject(method = \"pickBlock\"", "@Inject(method = \"pickBlockOrEntity\"")
            replace("@Inject(method = \"renderCrosshair\"", "@Inject(method = \"extractCrosshair\"")
        }

        // 26.1 moved or renamed these vanilla classes
        string(current.parsed >= "26.1") {
            replace("net.minecraft.client.renderer.block.model.ItemTransform", "net.minecraft.client.resources.model.cuboid.ItemTransform")
            replace("net/minecraft/client/renderer/block/model/ItemTransform", "net/minecraft/client/resources/model/cuboid/ItemTransform")
            replace("net.minecraft.client.renderer.state.CameraRenderState", "net.minecraft.client.renderer.state.level.CameraRenderState")
            replace("net.minecraft.client.renderer.LightTexture", "net.minecraft.util.LightCoordsUtil")
            replace("LightTexture.", "LightCoordsUtil.")
            replace("new ChunkPos(pos)", "ChunkPos.containing(pos)")
            replace("net.minecraft.client.gui.render.state.pip", "net.minecraft.client.renderer.state.gui.pip")
            replace("guiRenderState.submitPicturesInPictureState(", "guiRenderState.addPicturesInPictureState(")
            replace("ctx.vertexConsumers()", "ctx.bufferSource()")
            replace("net.minecraft.client.renderer.state.QuadParticleRenderState", "net.minecraft.client.renderer.state.level.QuadParticleRenderState")
            replace("Layer.TERRAIN", "Layer.TRANSLUCENT_TERRAIN")
            replace("getBlockRenderer().getBlockModelShaper().getParticleIcon(state)", "getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite()")
            replace("this.mainCamera.getPartialTickTime()", "this.mainCamera.getCameraEntityPartialTicks(deltaTracker)")
        }

        // 26.1 GUI: GuiGraphics became GuiGraphicsExtractor, render* methods became extract*
        string(current.parsed >= "26.1") {
            replace("GuiGraphics", "GuiGraphicsExtractor")
            replace("void renderContents(", "void extractContents(")
            replace("void renderText(", "void extractText(")
            replace("void renderImage(", "void extractImage(")
            replace("void renderLabels(", "void extractLabels(")
            replace("void renderContent(", "void extractContent(")
            replace("void renderListSeparators(", "void extractListSeparators(")
            replace("void renderListBackground(", "void extractListBackground(")
            replace("void render(@NotNull ", "void extractRenderState(@NotNull ")
            replace("super.render(graphics,", "super.extractRenderState(graphics,")
            replace("super.render(gui,", "super.extractRenderState(gui,")
            replace("widget.render(", "widget.extractRenderState(")
            for (receiver in listOf("graphics.", "Graphics.", "gui.")) {
                replace("${receiver}drawString(", "${receiver}text(")
                replace("${receiver}drawCenteredString(", "${receiver}centeredText(")
                replace("${receiver}drawWordWrap(", "${receiver}textWithWordWrap(")
                replace("${receiver}renderItem(", "${receiver}item(")
                replace("${receiver}renderFakeItem(", "${receiver}fakeItem(")
            }
        }

        // 26.2 moved screens and HUD state into Gui / Hud, render targets and lighting into GameRenderer
        string(current.parsed >= "26.2") {
            replace("Minecraft.getInstance().screen", "Minecraft.getInstance().gui.screen()")
            replace("minecraft.screen", "minecraft.gui.screen()")
            replace("mc.screen", "mc.gui.screen()")
            replace("minecraft.setScreen(", "minecraft.gui.setScreen(")
            replace("Minecraft.getInstance().setScreen(", "Minecraft.getInstance().gui.setScreen(")
            replace("mc.getOverlay()", "mc.gui.overlay()")
            replace("gui.getGuiTicks()", "gui.hud.getGuiTicks()")
            replace("options.hideGui", "net.minecraft.client.Minecraft.getInstance().gui.hud.isHidden()")
            replace("Minecraft.getInstance().getMainRenderTarget()", "Minecraft.getInstance().gameRenderer.mainRenderTarget()")
            replace("gameRenderer.getLighting()", "gameRenderer.lighting()")
            replace("gameRenderer.getMainCamera()", "gameRenderer.mainCamera()")
            for (color in listOf("DARK_GRAY", "YELLOW", "WHITE", "GRAY")) {
                replace("ChatFormatting.$color.getColor()", "net.minecraft.network.chat.TextColor.fromLegacyFormat(ChatFormatting.$color).getValue()")
            }
        }

        // 26.2 removed MultiBufferSource, TACZ uses a stand-in that submits its batches as custom geometry
        string(current.parsed >= "26.2") {
            replace("import net.minecraft.client.renderer.MultiBufferSource;", "import com.tacz.guns.platform.client.compat.MultiBufferSource;")
            replace("Minecraft.getInstance().renderBuffers().bufferSource()", "com.tacz.guns.platform.client.compat.MultiBufferSource.immediate()")
        }

        // 26.3: SDL instead of GLFW, Renderpearl instead of Blaze3D for the render API, PoseStack#mulPose(Quaternionf) is rotate
        string(current.parsed >= "26.3") {
            replace("InputConstants.Type.KEYSYM", "InputConstants.Type.KEYBOARD")
            // only the quaternion overload was renamed; the qualified Axis keeps the new text unique (Matrix4f#rotate(Axis...) exists too)
            replace(".mulPose(Axis.", ".rotate(com.mojang.math.Axis.")
            replace(".mulPose(rotation)", ".rotate(rotation)")
            replace(".mulPose(quaternion)", ".rotate(quaternion)")
            replace(".mulPose(additionalQuaternion)", ".rotate(additionalQuaternion)")
            // render API classes that moved to com.mojang.renderpearl (Std140Builder, RenderSystem, PoseStack... stayed)
            replace("com.mojang.blaze3d.opengl.", "com.mojang.renderpearl.backend.opengl.")
            replace("import com.mojang.blaze3d.buffers.GpuBuffer;", "import com.mojang.renderpearl.api.buffers.GpuBuffer;")
            replace("import com.mojang.blaze3d.pipeline.BindGroupLayout;", "import com.mojang.renderpearl.api.pipeline.BindGroupLayout;")
            replace("import com.mojang.blaze3d.pipeline.RenderPipeline;", "import com.mojang.renderpearl.api.pipeline.RenderPipeline;")
            replace("import com.mojang.blaze3d.pipeline.ColorTargetState;", "import com.mojang.renderpearl.api.pipeline.ColorTargetState;")
            replace("import com.mojang.blaze3d.pipeline.DepthStencilState;", "import com.mojang.renderpearl.api.pipeline.DepthStencilState;")
            replace("import com.mojang.blaze3d.platform.CompareOp;", "import com.mojang.renderpearl.api.pipeline.CompareOp;")
            replace("import com.mojang.blaze3d.shaders.UniformType;", "import com.mojang.renderpearl.api.pipeline.UniformType;")
            replace("import com.mojang.blaze3d.systems.RenderPass;", "import com.mojang.renderpearl.api.commands.RenderPass;")
            replace("import com.mojang.blaze3d.textures.GpuTexture;", "import com.mojang.renderpearl.api.textures.GpuTexture;")
            replace("import com.mojang.blaze3d.vertex.VertexFormat;", "import com.mojang.renderpearl.api.vertex.VertexFormat;")
            // vanilla moved to setters / a new enum constant name
            replace(".invulnerableTime = 0;", ".setInvulnerableTime(0);")
            replace("PushReaction.DESTROY", "PushReaction.POPPED")
            replace("entity.hurtMarked = true;", "entity.needsSync = true;")
        }
    }
}
