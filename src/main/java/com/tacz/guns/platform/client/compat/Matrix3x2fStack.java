package com.tacz.guns.platform.client.compat;

//? if <1.21.6 {
/*import com.mojang.blaze3d.vertex.PoseStack;

/^*
 * Before 1.21.6 GUIs drew with the 3D PoseStack, this gives it the calls TACZ makes on the 2D matrix stack.
 ^/
public record Matrix3x2fStack(PoseStack poseStack) {
    public void pushMatrix() {
        poseStack.pushPose();
    }

    public void popMatrix() {
        poseStack.popPose();
    }

    public void translate(float x, float y) {
        poseStack.translate(x, y, 0.0F);
    }

    public void scale(float x, float y) {
        poseStack.scale(x, y, 1.0F);
    }
}
*///?}
