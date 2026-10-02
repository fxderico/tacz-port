package com.tacz.guns.platform.client.compat;

//? if >=26.2 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.GunMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/^*
 * 26.2 removed MultiBufferSource: everything is drawn by feature renderers from submitted nodes. TACZ's renderers
 * still write vertices into buffers and flush them, so this stand-in (same name and shape as the vanilla class,
 * swapped in by Stonecutter) records the vertices per render type and submits them as custom geometry to the
 * collector the current render runs under when a batch ends.
 * Positions and normals are already transformed by the renderers, they are submitted with an identity pose.
 *^/
@Environment(EnvType.CLIENT)
public interface MultiBufferSource {
    VertexConsumer getBuffer(RenderType renderType);

    /^*
     * The shared buffer source that replaces Minecraft#renderBuffers().bufferSource().
     *^/
    static BufferSource immediate() {
        return BufferSource.SHARED;
    }

    /^*
     * Runs {@code action} with {@code collector} as the target of every flushed batch, flushing what is left at the end.
     *^/
    static void withCollector(SubmitNodeCollector collector, Runnable action) {
        BufferSource.COLLECTORS.push(collector);
        try {
            action.run();
            BufferSource.SHARED.endBatch();
        } finally {
            BufferSource.COLLECTORS.pop();
        }
    }

    @Nullable
    static SubmitNodeCollector currentCollector() {
        return BufferSource.COLLECTORS.peek();
    }

    @Environment(EnvType.CLIENT)
    class BufferSource implements MultiBufferSource {
        private static final BufferSource SHARED = new BufferSource();
        private static final Deque<SubmitNodeCollector> COLLECTORS = new ArrayDeque<>();
        private static boolean warnedNoCollector = false;

        private final Map<RenderType, RecordingVertexConsumer> pending = new LinkedHashMap<>();

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return this.pending.computeIfAbsent(renderType, type -> new RecordingVertexConsumer());
        }

        public void endBatch() {
            for (RenderType renderType : this.pending.keySet().toArray(RenderType[]::new)) {
                this.endBatch(renderType);
            }
        }

        public void endBatch(RenderType renderType) {
            RecordingVertexConsumer recorded = this.pending.remove(renderType);
            if (recorded == null || recorded.isEmpty()) {
                return;
            }
            SubmitNodeCollector collector = COLLECTORS.peek();
            if (collector == null) {
                if (!warnedNoCollector) {
                    warnedNoCollector = true;
                    GunMod.LOGGER.warn("TACZ geometry for {} was flushed outside of any submit collector, it is dropped", renderType);
                }
                return;
            }
            collector.submitCustomGeometry(new PoseStack(), renderType, (pose, buffer) -> recorded.replay(buffer));
        }
    }

    /^*
     * Keeps every vertex attribute written to it, in order, and writes them again into another consumer.
     *^/
    @Environment(EnvType.CLIENT)
    final class RecordingVertexConsumer implements VertexConsumer {
        private static final int POSITION = 0, COLOR = 1, UV = 2, UV1 = 3, UV2 = 4, NORMAL = 5, LINE_WIDTH = 6, UV3 = 7;
        private float[] floats = new float[256];
        private int[] ops = new int[64];
        private int floatCount;
        private int opCount;

        boolean isEmpty() {
            return this.opCount == 0;
        }

        private void op(int op, float a, float b, float c, float d) {
            if (this.opCount == this.ops.length) {
                this.ops = java.util.Arrays.copyOf(this.ops, this.ops.length * 2);
            }
            if (this.floatCount + 4 > this.floats.length) {
                this.floats = java.util.Arrays.copyOf(this.floats, this.floats.length * 2);
            }
            this.ops[this.opCount++] = op;
            this.floats[this.floatCount++] = a;
            this.floats[this.floatCount++] = b;
            this.floats[this.floatCount++] = c;
            this.floats[this.floatCount++] = d;
        }

        void replay(VertexConsumer target) {
            for (int i = 0, f = 0; i < this.opCount; i++, f += 4) {
                float a = this.floats[f], b = this.floats[f + 1], c = this.floats[f + 2], d = this.floats[f + 3];
                switch (this.ops[i]) {
                    case POSITION -> target.addVertex(a, b, c);
                    case COLOR -> target.setColor((int) a, (int) b, (int) c, (int) d);
                    case UV -> target.setUv(a, b);
                    case UV1 -> target.setUv1((int) a, (int) b);
                    case UV2 -> target.setUv2((int) a, (int) b);
                    case NORMAL -> target.setNormal(a, b, c);
                    case LINE_WIDTH -> target.setLineWidth(a);
                    case UV3 -> VertexUv3.set(target, a, b);
                    default -> throw new IllegalStateException();
                }
            }
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.op(POSITION, x, y, z, 0);
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            this.op(COLOR, r, g, b, a);
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            return this.setColor(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >>> 24);
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.op(UV, u, v, 0, 0);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            this.op(UV1, u, v, 0, 0);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            this.op(UV2, u, v, 0, 0);
            return this;
        }

        // VertexConsumer method since 26.3
        public VertexConsumer setUv3(float u, float v) {
            this.op(UV3, u, v, 0, 0);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            this.op(NORMAL, x, y, z, 0);
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            this.op(LINE_WIDTH, width, 0, 0, 0);
            return this;
        }
    }
}
*///?}
