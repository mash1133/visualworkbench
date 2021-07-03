package fuzs.visualworkbench.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import fuzs.visualworkbench.tileentity.WorkbenchTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;

import java.util.Map;
import java.util.WeakHashMap;

public class WorkbenchTileEntityRenderer extends TileEntityRenderer<WorkbenchTileEntity> {

    private final Map<WorkbenchTileEntity, WorkbenchTileEntityRenderer.RenderingState> states = new WeakHashMap<>();

    public WorkbenchTileEntityRenderer(TileEntityRendererDispatcher tileEntityRendererDispatcher) {

        super(tileEntityRendererDispatcher);
    }

    @Override
    public void render(WorkbenchTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

        for(int i = 0; i < tileEntityIn.getContainerSize(); ++i) {

            ItemStack itemStack = tileEntityIn.getItem(i);
            if (itemStack != ItemStack.EMPTY) {

                WorkbenchTileEntityRenderer.RenderingState state = this.states.computeIfAbsent(tileEntityIn, k -> new RenderingState());
                byte sector = (byte) ((int) (tileEntityIn.playerAngle * 2.0D / 3.141592653589793D));
                long time = System.currentTimeMillis();
                float shift;
                if (state.sector != sector) {

                    state.animating = true;
                    state.animationAngleStart = state.currentAngle;
                    float delta1 = (float) sector * 90.0F - state.currentAngle;
                    float abs1 = Math.abs(delta1);
                    float delta2 = delta1 + 360.0F;
                    shift = Math.abs(delta2);
                    float delta3 = delta1 - 360.0F;
                    float abs3 = Math.abs(delta3);
                    if (abs3 < abs1 && abs3 < shift) {

                        state.animationAngleEnd = delta3 + state.currentAngle;
                    } else if (shift < abs1 && shift < abs3) {

                        state.animationAngleEnd = delta2 + state.currentAngle;
                    } else {

                        state.animationAngleEnd = delta1 + state.currentAngle;
                    }

                    state.startTime = time;
                    state.sector = sector;
                }

                if (state.animating) {

                    if (time >= state.startTime + 1000L) {

                        state.animating = false;
                        state.currentAngle = (state.animationAngleEnd + 360.0F) % 360.0F;
                    } else {

                        state.currentAngle = (easeOutQuad(time - state.startTime, state.animationAngleStart, state.animationAngleEnd - state.animationAngleStart, 1000) + 360.0F) % 360.0F;
                    }
                }

                shift = (float) Math.abs((time + (i * 1000L)) % 5000L - 2500L) / 200000.0F;

                matrixStackIn.pushPose();
                matrixStackIn.translate(0.5D, shift, 0.5D);
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(state.currentAngle));
                matrixStackIn.translate((double) (i % 3) * 3.0D / 16.0D + 0.3125D - 0.5D, 1.09375D, (double)(i / 3) * 3.0D / 16.0D + 0.3125D - 0.5D);
                float scale = itemStack.getItem() instanceof BlockItem ? 0.24F : 0.18F;
                matrixStackIn.scale(scale, scale, scale);
                Minecraft.getInstance().getItemRenderer().renderStatic(itemStack, ItemCameraTransforms.TransformType.FIXED, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn);
                matrixStackIn.popPose();
            }
        }
    }

    private static float easeOutQuad(long t, float b, float c, int d) {

        float z = (float) t / (float) d;
        return -c * z * (z - 2.0F) + b;
    }

    private static class RenderingState {

        public byte sector;
        public float currentAngle;
        public boolean animating;
        public float animationAngleStart;
        public float animationAngleEnd;
        public long startTime;

    }

}
