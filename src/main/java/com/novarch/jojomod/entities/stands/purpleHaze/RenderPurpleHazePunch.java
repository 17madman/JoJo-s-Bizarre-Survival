package com.novarch.jojomod.entities.stands.purpleHaze;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.novarch.jojomod.JojoBizarreSurvival;
import com.novarch.jojomod.entities.stands.EntityStandPunch;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RenderPurpleHazePunch extends EntityRenderer<EntityStandPunch.purpleHaze>
{
	protected ModelPurpleHazePunch<EntityStandPunch.purpleHaze> punch;
	protected static final ResourceLocation texture = new ResourceLocation(JojoBizarreSurvival.MOD_ID, "textures/stands/purple_haze_punch.png");

	public RenderPurpleHazePunch(EntityRendererManager renderManagerIn)
	{
		super(renderManagerIn);
		this.punch = new ModelPurpleHazePunch<>();
	}

	@Override
	public void render(@Nonnull EntityStandPunch.purpleHaze entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
	{
		renderEntityModel(entityIn, matrixStackIn, bufferIn, packedLightIn);
	}

	public void renderEntityModel(@Nonnull EntityStandPunch.purpleHaze entityIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
	{
		this.renderManager.textureManager.bindTexture(texture);
		GL11.glPushMatrix();
		GL11.glTranslatef((float) entityIn.getPosX(), (float) entityIn.getPosY(), (float) entityIn.getPosZ());
		GL11.glRotatef(entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * packedLightIn - 90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(entityIn.prevRotationPitch + (entityIn.rotationPitch - entityIn.prevRotationPitch) * packedLightIn, 0.0F, 0.0F, 1.0F);
		GL11.glEnable(32826);
		Minecraft.getInstance().textureManager.bindTexture(texture);
		GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glScalef(2.0F, 2.0F, 2.0F);
		this.punch.render(matrixStackIn, bufferIn.getBuffer(RenderType.getEntitySmoothCutout(getEntityTexture(entityIn))), packedLightIn, 0);
		GL11.glDisable(32826);
		GL11.glPopMatrix();
	}

	@Nonnull
	@Override
	public ResourceLocation getEntityTexture(final EntityStandPunch.purpleHaze entity)
	{
		return RenderPurpleHazePunch.texture;
	}
}

