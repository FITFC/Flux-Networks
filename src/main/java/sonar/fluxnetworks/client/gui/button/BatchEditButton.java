package sonar.fluxnetworks.client.gui.button;

/*import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import sonar.fluxnetworks.client.gui.ScreenUtils;
import sonar.fluxnetworks.client.gui.basic.GuiButtonCore;

public class BatchEditButton extends GuiButtonCore {

    public BatchEditButton(int x, int y, int id, String text) {
        super(x, y, 12, 12, id);
        this.text = text;
    }

    @Override
    public void drawButton(Minecraft mc, MatrixStack matrixStack, int mouseX, int mouseY, int guiLeft, int guiTop) {
        GlStateManager.pushMatrix();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableBlend();
        if (clickable) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0f);
        } else {
            RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0f);
        }

        boolean b = isMouseHovered(mc, mouseX - guiLeft, mouseY - guiTop);

        mc.getTextureManager().bindTexture(ScreenUtils.BUTTONS);
        blit(matrixStack, x, y, 16 * id, 48 + (b ? 16 : 0), 12, 12);

        if (clickable && b) {
            mc.fontRenderer.drawString(matrixStack, text, x - mc.fontRenderer.getStringWidth(text) / 2f + 6, y - 9, 0xFFFFFF);
        }

        GlStateManager.popMatrix();
    }

    public BatchEditButton setUnclickable() {
        clickable = false;
        return this;
    }
}*/
