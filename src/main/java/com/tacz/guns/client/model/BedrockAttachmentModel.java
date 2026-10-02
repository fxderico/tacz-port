package com.tacz.guns.client.model;

import com.mojang.blaze3d.vertex.*;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.model.bedrock.ModelRendererWrapper;
import com.tacz.guns.client.model.functional.BeamRenderer;
import com.tacz.guns.client.model.functional.TextShowRender;
import com.tacz.guns.client.renderer.ScopeRenderTypes;
import com.tacz.guns.client.resource.pojo.display.gun.TextShow;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import com.tacz.guns.util.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BedrockAttachmentModel extends BedrockAnimatedModel {
    private static final String SCOPE_VIEW_NODE = "scope_view";
    private static final String SCOPE_BODY_NODE = "scope_body";
    private static final String OCULAR_RING_NODE = "ocular_ring";
    private static final String DIVISION_NODE = "division";
    private static final String OCULAR_NODE = "ocular";
    private static final String OCULAR_SIGHT_NODE = "ocular_sight";
    private static final String OCULAR_SCOPE_NODE = "ocular_scope";
    private static final Pattern LASER_BEAM_PATTERN = Pattern.compile("^laser_beam(_(\\d+))?$");

    protected List<List<BedrockPart>> scopeViewPaths;
    protected @Nullable List<BedrockPart> scopeBodyPath;
    protected @Nullable List<BedrockPart> ocularRingPath;
    protected List<List<BedrockPart>> ocularNodePaths;
    protected List<Boolean> isScopeOcular;
    protected List<List<BedrockPart>> divisionNodePaths;
    protected @Nullable List<List<BedrockPart>> laserBeamPaths;

    private @Nullable ItemStack currentGunItem;
    private @Nullable ItemStack attachmentItem;
    private @Nullable Identifier texture;

    private boolean isScope = false;
    private boolean isSight = false;
    private float scopeViewRadiusModifier = 1;

    public BedrockAttachmentModel(BedrockModelPOJO pojo, BedrockVersion version) {
        super(pojo, version);
        scopeViewPaths = new ArrayList<>();
        ocularNodePaths = new ArrayList<>();
        isScopeOcular = new ArrayList<>();
        divisionNodePaths = new ArrayList<>();
        laserBeamPaths = new ArrayList<>();
        // 初始化 view 的 node path
        List<BedrockPart> path = getPath(modelMap.get(SCOPE_VIEW_NODE));
        int i = 2;
        while (path != null) {
            scopeViewPaths.add(path);
            path = getPath(modelMap.get(SCOPE_VIEW_NODE + '_' + i++));
        }
        // 初始化 ocular 的 node path
        String ocularRegex = "^(" + OCULAR_NODE + "|" + OCULAR_SIGHT_NODE + "|" + OCULAR_SCOPE_NODE + ")(_(\\d+))?$";
        Pattern ocularPattern = Pattern.compile(ocularRegex);
        TreeMap<Integer, OcularWrapper> map = new TreeMap<>();
        for (Map.Entry<String, ModelRendererWrapper> entry : modelMap.entrySet()) {
            Matcher matcher = ocularPattern.matcher(entry.getKey());
            if (matcher.matches()) {
                int num = 1;
                String numStr = matcher.group(3);
                if (numStr != null) {
                    num = Integer.parseInt(numStr);
                }
                String type = matcher.group(1);
                boolean isScope = OCULAR_SCOPE_NODE.equals(type);
                map.put(num, new OcularWrapper(entry.getValue(), isScope));
            }
            if (LASER_BEAM_PATTERN.matcher(entry.getKey()).find()) {
                laserBeamPaths.add(getPath(entry.getValue()));
            }
        }
        for (OcularWrapper wrapper : map.values()) {
            ocularNodePaths.add(getPath(wrapper.renderer));
            isScopeOcular.add(wrapper.isScope);
        }
        // 初始化 division 的 node path
        ModelRendererWrapper divisionModel = modelMap.get(DIVISION_NODE);
        path = getPath(modelMap.get(DIVISION_NODE));
        i = 2;
        while (path != null) {
            divisionNodePaths.add(path);
            divisionModel.setHidden(true);
            divisionModel = modelMap.get(DIVISION_NODE + '_' + i++);
            path = getPath(divisionModel);
        }

        scopeBodyPath = getPath(modelMap.get(SCOPE_BODY_NODE));
        ocularRingPath = getPath(modelMap.get(OCULAR_RING_NODE));
    }

    @Nullable
    public List<BedrockPart> getScopeViewPath(int viewSwitchCount) {
        if (scopeViewPaths.isEmpty()) {
            return null;
        }
        if (viewSwitchCount >= scopeViewPaths.size()) {
            return scopeViewPaths.get(0);
        }
        return scopeViewPaths.get(viewSwitchCount);
    }

    public void setIsScope(boolean isScope) {
        this.isScope = isScope;
    }

    public void setIsSight(boolean isSight) {
        this.isSight = isSight;
    }

    public boolean isScope() {
        return isScope;
    }

    public boolean isSight() {
        return isSight;
    }

    public void setScopeViewRadiusModifier(float scopeViewRadiusModifier) {
        this.scopeViewRadiusModifier = scopeViewRadiusModifier;
    }

    /**
     * 添加枪械自定义的文本显示
     */
    public void setTextShowList(Map<String, TextShow> textShowList) {
        textShowList.forEach((name, textShow) -> this.setFunctionalRenderer(name,
                bedrockPart -> new TextShowRender(this, textShow, currentGunItem)));
    }

    /**
     * Renders with {@code RenderTypes.entityCutout(texture)}, the texture is also needed for the stencil passes of
     * scopes and sights in first person.
     */
    public void render(@Nullable ItemStack attachmentItem, @Nullable ItemStack currentGunItem, PoseStack matrixStack, ItemDisplayContext transformType, Identifier texture, int light, int overlay) {
        this.texture = texture;
        try {
            render(attachmentItem, currentGunItem, matrixStack, transformType, RenderTypes.entityCutout(texture), light, overlay);
        } finally {
            this.texture = null;
        }
    }

    public void render(@Nullable ItemStack attachmentItem, ItemStack currentGunItem, PoseStack matrixStack, ItemDisplayContext transformType, RenderType renderType, int light, int overlay) {
        this.currentGunItem = currentGunItem;
        this.attachmentItem = attachmentItem;
        if (transformType.firstPerson() && texture != null) {
            //? if >=26.2 {
            /*if (isScope || isSight) {
                renderMasked(matrixStack, transformType, renderType, light, overlay);
            }
            *///?} else {
            if (isScope && isSight) {
                renderBoth(matrixStack, transformType, renderType, light, overlay);
            } else if (isScope) {
                renderScope(matrixStack, transformType, renderType, light, overlay);
            } else if (isSight) {
                renderSight(matrixStack, transformType, renderType, light, overlay);
            }
            //?}
        } else {
            if (scopeBodyPath != null) {
                renderTempPart(matrixStack, transformType, renderType, light, overlay, scopeBodyPath);
            }
            if (ocularRingPath != null) {
                renderTempPart(matrixStack, transformType, renderType, light, overlay, ocularRingPath);
            }
        }
        if (!isScope && !isSight && laserBeamPaths != null) {
            for (var entry : laserBeamPaths) {
                BeamRenderer.renderLaserBeam(attachmentItem, matrixStack, transformType, entry);
            }
        }
        super.render(matrixStack, transformType, renderType, light, overlay);
        if ((isScope || isSight) && laserBeamPaths != null) {
            for (var entry : laserBeamPaths) {
                BeamRenderer.renderLaserBeam(attachmentItem, matrixStack, transformType, entry);
            }
        }
    }

    private Vector3f getBedrockPartCenter(PoseStack poseStack, @Nonnull List<BedrockPart> path) {
        poseStack.pushPose();
        for (BedrockPart part : path) {
            part.translateAndRotateAndScale(poseStack);
        }
        Vector3f result = new Vector3f(poseStack.last().pose().m30(), poseStack.last().pose().m31(), poseStack.last().pose().m32());
        poseStack.popPose();
        return result;
    }

    private void renderTempPart(PoseStack poseStack, ItemDisplayContext transformType, RenderType renderType,
                                int light, int overlay, @Nonnull List<BedrockPart> path) {
        poseStack.pushPose();
        for (int i = 0; i < path.size() - 1; ++i) {
            path.get(i).translateAndRotateAndScale(poseStack);
        }
        BedrockPart part = path.get(path.size() - 1);
        part.visible = true;
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        part.render(poseStack, transformType, vertexConsumer, light, overlay);
        bufferSource.endBatch(renderType);
        part.visible = false;
        poseStack.popPose();
    }

    //? if >=26.2 {
    /*/^*
     * 26.2+: no stencil, every part discards its own fragments in the scope shader (see ScopeMask).
     *^/
    private void renderMasked(PoseStack matrixStack, ItemDisplayContext transformType, RenderType renderType, int light, int overlay) {
        List<List<BedrockPart>> scopeOculars = new ArrayList<>();
        List<List<BedrockPart>> sightOculars = new ArrayList<>();
        for (int i = 0; i < ocularNodePaths.size(); i++) {
            boolean scopeOcular = isScope && (!isSight || isScopeOcular.get(i));
            (scopeOcular ? scopeOculars : sightOculars).add(ocularNodePaths.get(i));
        }
        List<BedrockPart> primary = !scopeOculars.isEmpty() ? scopeOculars.getFirst() : sightOculars.isEmpty() ? null : sightOculars.getFirst();
        List<BedrockPart> secondary = !scopeOculars.isEmpty() && !sightOculars.isEmpty() ? sightOculars.getFirst() : null;
        if (primary != null) {
            // 80是一个随便找的大小合适的数值。
            float rad = 80 * scopeViewRadiusModifier;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                rad *= IClientPlayerGunOperator.fromLocalPlayer(player).getClientAimingProgress(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
            }
            com.tacz.guns.client.renderer.ScopeMask.setLenses(matrixStack, transformType, primary, !scopeOculars.isEmpty(), secondary, rad);
        }
        if (ocularRingPath != null) {
            renderTempPart(matrixStack, transformType, renderType, light, overlay, ocularRingPath);
        }
        if (scopeBodyPath != null) {
            renderTempPart(matrixStack, transformType, isScope ? com.tacz.guns.client.renderer.ScopeMask.outsideLens(texture) : renderType, light, overlay, scopeBodyPath);
        }
        // black eyepiece of the scope, opened by the aim circle
        for (List<BedrockPart> ocular : scopeOculars) {
            renderTempPart(matrixStack, transformType, com.tacz.guns.client.renderer.ScopeMask.ocularMask(texture), light, overlay, ocular);
        }
        // red dot sights draw their reticle over everything in the eyepiece, like the Forge version
        RenderType reticle = com.tacz.guns.client.renderer.ScopeMask.reticle(texture, isScope);
        for (List<BedrockPart> division : divisionNodePaths) {
            renderTempPart(matrixStack, transformType, reticle, light, overlay, division);
        }
        super.render(matrixStack, transformType, renderType, light, overlay);
    }
    *///?} else {
    private void renderOcularStencil(PoseStack matrixStack, ItemDisplayContext transformType, int light, int overlay, boolean isScope) {
        if (!ocularNodePaths.isEmpty()) {
            // color and depth writes are off in this render type
            RenderType stencilType = ScopeRenderTypes.ocularStencil(texture);
            GL11.glStencilMask(0xFF);
            RenderHelper.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
            // 绘制目镜
            for (int i = ocularNodePaths.size() - 1; i >= 0; i--) {
                if (isScope == isScopeOcular.get(i)) {
                    RenderHelper.stencilFunc(GL11.GL_GREATER, i + 1, 0xFF);
                    renderTempPart(matrixStack, transformType, stencilType, light, overlay, ocularNodePaths.get(i));
                }
            }
            // 恢复渲染状态
            RenderHelper.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        }
    }

    private void renderDivisionOnly(PoseStack matrixStack, ItemDisplayContext transformType, int light, int overlay) {
        if (!divisionNodePaths.isEmpty()) {
            // depth test is off in this render type
            RenderType divisionType = ScopeRenderTypes.divisionNoDepth(texture);
            for (int i = 0; i < divisionNodePaths.size(); i++) {
                RenderHelper.stencilFunc(GL11.GL_EQUAL, i + 1, 0xFF);
                renderTempPart(matrixStack, transformType, divisionType, light, overlay, divisionNodePaths.get(i));
            }
        }
    }

    private void renderOcularAndDivision(PoseStack matrixStack, ItemDisplayContext transformType, RenderType renderType, int light, int overlay, boolean selective) {
        if (!ocularNodePaths.isEmpty()) {
            // 准备渲染圆形模板层
            RenderHelper.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INVERT);
            // 80是一个随便找的大小合适的数值。
            float rad = 80 * scopeViewRadiusModifier;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                rad *= IClientPlayerGunOperator.fromLocalPlayer(player).getClientAimingProgress(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
            }
            // The Forge version drew the circle in view space with an identity model view. Here the model view holds
            // the camera rotation and the pose stack its inverse, so go to view space and back.
            Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewMatrix());
            Matrix4f viewToPose = modelView.invert(new Matrix4f());
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            Vector3f vertex = new Vector3f();
            for (int i = 0; i < ocularNodePaths.size(); i++) {
                if (selective && !isScopeOcular.get(i)) {
                    continue;
                }
                RenderHelper.stencilFunc(GL11.GL_EQUAL, i + 1, 0xFF);
                Vector3f ocularCenter = modelView.transformPosition(getBedrockPartCenter(matrixStack, ocularNodePaths.get(i)));
                float centerX = ocularCenter.x() * 16 * 90;
                float centerY = ocularCenter.y() * 16 * 90;
                // color and depth writes are off in this render type
                VertexConsumer builder = bufferSource.getBuffer(ScopeRenderTypes.SCOPE_VIEW_CIRCLE);
                viewToPose.transformPosition(centerX, centerY, -90.0F, vertex);
                builder.addVertex(vertex.x(), vertex.y(), vertex.z()).setColor(255, 255, 255, 255);
                for (int j = 0; j <= 90; j++) {
                    float angle = (float) j * ((float) Math.PI * 2F) / 90.0F;
                    float sin = Mth.sin(angle);
                    float cos = Mth.cos(angle);
                    viewToPose.transformPosition(centerX + cos * rad, centerY + sin * rad, -90.0F, vertex);
                    builder.addVertex(vertex.x(), vertex.y(), vertex.z()).setColor(255, 255, 255, 255);
                }
                bufferSource.endBatch(ScopeRenderTypes.SCOPE_VIEW_CIRCLE);
            }
            RenderHelper.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            for (int i = 0; i < ocularNodePaths.size() && i < divisionNodePaths.size(); i++) {
                if (i > Byte.MAX_VALUE) {
                    throw new IllegalArgumentException("Index of oculus is out of range for 127");
                }
                if (selective && !isScopeOcular.get(i)) {
                    RenderHelper.stencilFunc(GL11.GL_EQUAL, i + 1, 0xFF);
                    renderTempPart(matrixStack, transformType, renderType, light, overlay, divisionNodePaths.get(i));
                } else {
                    // 渲染目镜黑色遮罩
                    RenderHelper.stencilFunc(GL11.GL_EQUAL, i + 1, 0xFF);
                    renderTempPart(matrixStack, transformType, renderType, light, overlay, ocularNodePaths.get(i));
                    // 渲染划分
                    int b = ~(i + 1) & 0xFF;
                    RenderHelper.stencilFunc(GL11.GL_EQUAL, b, 0xFF);
                    renderTempPart(matrixStack, transformType, renderType, light, overlay, divisionNodePaths.get(i));
                }
            }
        }
    }

    private void renderBoth(PoseStack matrixStack, ItemDisplayContext transformType, RenderType renderType, int light, int overlay) {
        RenderHelper.enableItemEntityStencilTest();
        // 清空模板缓冲区、准备绘制模板缓冲
        RenderHelper.clearStencil();
        if (ocularRingPath != null) {
            RenderHelper.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
            RenderHelper.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            // 渲染目镜外环
            renderTempPart(matrixStack, transformType, renderType, light, overlay, ocularRingPath);
        }
        // 渲染目镜以写入模板桓冲值 (暂时只渲染 ocular_scope)
        renderOcularStencil(matrixStack, transformType, light, overlay, true);
        // 渲染镜身
        if (scopeBodyPath != null) {
            RenderHelper.stencilFunc(GL11.GL_EQUAL, 0, 0xFF);
            renderTempPart(matrixStack, transformType, renderType, light, overlay, scopeBodyPath);
        }
        // 渲染目镜以写入模板桓冲值 (渲染其他的目镜)
        renderOcularStencil(matrixStack, transformType, light, overlay, false);
        // 渲染目镜遮罩和划分
        renderOcularAndDivision(matrixStack, transformType, renderType, light, overlay, true);
        // 关闭模板缓冲
        RenderHelper.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        RenderHelper.disableItemEntityStencilTest();
        // 渲染其他部分
        super.render(matrixStack, transformType, renderType, light, overlay);
    }

    private void renderSight(PoseStack matrixStack, ItemDisplayContext transformType, RenderType renderType, int light, int overlay) {
        RenderHelper.enableItemEntityStencilTest();
        // 清空模板缓冲区、准备绘制模板缓冲
        RenderHelper.clearStencil();
        // 渲染目镜以写入模板桓冲值
        renderOcularStencil(matrixStack, transformType, light, overlay, false);
        // 渲染划分
        renderDivisionOnly(matrixStack, transformType, light, overlay);
        // 关闭模板缓冲
        RenderHelper.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        RenderHelper.disableItemEntityStencilTest();
        // 渲染其他部分
        if (scopeBodyPath != null) {
            renderTempPart(matrixStack, transformType, renderType, light, overlay, scopeBodyPath);
        }
        super.render(matrixStack, transformType, renderType, light, overlay);
    }

    private void renderScope(PoseStack matrixStack, ItemDisplayContext transformType, RenderType renderType, int light, int overlay) {
        RenderHelper.enableItemEntityStencilTest();
        // 清空模板缓冲区、准备绘制模板缓冲
        RenderHelper.clearStencil();
        // 渲染目镜外环
        if (ocularRingPath != null) {
            RenderHelper.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
            RenderHelper.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            renderTempPart(matrixStack, transformType, renderType, light, overlay, ocularRingPath);
        }
        // 渲染目镜以写入模板桓冲值
        renderOcularStencil(matrixStack, transformType, light, overlay, false);
        // 渲染镜身
        if (scopeBodyPath != null) {
            RenderHelper.stencilFunc(GL11.GL_EQUAL, 0, 0xFF);
            renderTempPart(matrixStack, transformType, renderType, light, overlay, scopeBodyPath);
        }
        // 渲染目镜遮罩和划分
        renderOcularAndDivision(matrixStack, transformType, renderType, light, overlay, false);
        // 关闭模板缓冲
        RenderHelper.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        RenderHelper.disableItemEntityStencilTest();
        // 渲染其他部分
        super.render(matrixStack, transformType, renderType, light, overlay);
    }
    //?}

    private static class OcularWrapper{
        public ModelRendererWrapper renderer;
        public boolean isScope;

        public OcularWrapper (ModelRendererWrapper renderer, boolean isScope){
            this.renderer = renderer;
            this.isScope = isScope;
        }
    }
}