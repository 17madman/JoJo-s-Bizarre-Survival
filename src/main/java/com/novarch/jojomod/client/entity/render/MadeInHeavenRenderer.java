package com.novarch.jojomod.client.entity.render;

import com.novarch.jojomod.JojoBizarreSurvival;
import com.novarch.jojomod.client.entity.model.MadeInHeavenModel;
import com.novarch.jojomod.entities.stands.MadeInHeavenEntity;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MadeInHeavenRenderer extends MobRenderer<MadeInHeavenEntity, MadeInHeavenModel<MadeInHeavenEntity>>
{
	protected static final ResourceLocation texture = new ResourceLocation(JojoBizarreSurvival.MOD_ID, "textures/stands/made_in_heaven.png");

	public MadeInHeavenRenderer(EntityRendererManager renderManagerIn)
	{
		super(renderManagerIn, new MadeInHeavenModel<>(), 0.5f);
	}

	public ResourceLocation getEntityTexture(final MadeInHeavenEntity entity)
	{
		return MadeInHeavenRenderer.texture;
	}
}
