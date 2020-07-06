package com.novarch.jojomod.client.entity.render;

import com.novarch.jojomod.JojoBizarreSurvival;
import com.novarch.jojomod.client.entity.model.D4CModel;
import com.novarch.jojomod.entities.stands.D4CEntity;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class D4CRenderer extends MobRenderer<D4CEntity, D4CModel<D4CEntity>> {
	protected static final ResourceLocation TEXTURE = new ResourceLocation(JojoBizarreSurvival.MOD_ID, "textures/stands/d4c.png");

	public D4CRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new D4CModel<>(), 0.5f);
	}

	@Override
	public ResourceLocation getEntityTexture(final D4CEntity entity) {
		return TEXTURE;
	}
}
