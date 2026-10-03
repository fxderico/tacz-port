package com.tacz.guns.platform.client.compat;

//? if <1.21.9 {
/*import net.minecraft.client.renderer.MultiBufferSource;

/^*
 * Before 1.21.9 there was no submit pass, everything drew straight into a buffer source. TACZ's code passes this
 * stand-in where newer versions pass vanilla's SubmitNodeCollector (the import is rewritten for those versions).
 *^/
public record SubmitNodeCollector(MultiBufferSource bufferSource) {
}
*///?}
