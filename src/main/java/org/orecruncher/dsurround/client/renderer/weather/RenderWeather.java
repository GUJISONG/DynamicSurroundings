/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.orecruncher.dsurround.client.renderer.weather;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.lib.ReflectedField.IntegerField;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class RenderWeather extends IRenderHandler {

	
	private static final IntegerField<EntityRenderer> rendererUpdateCount = new IntegerField<>(
			EntityRenderer.class,
			"rendererUpdateCount",
			"field_78529_t"
		);
	
	public static int getRendererUpdateCount() {
		return rendererUpdateCount.get(Minecraft.getMinecraft().entityRenderer);
	}
		
	private final StormRenderer stormRenderer;

	protected RenderWeather() {
		this.stormRenderer = new StormRenderer();
	}

	/**
	 * Render rain particles. Redirect from EntityRenderer. Why can't there be a
	 * hook like that for rain/snow rendering?
	 */
	public static boolean addRainParticles(@Nonnull final EntityRenderer theThis) {
		final World world = EnvironState.getWorld();
		if (world != null && world.provider.getWeatherRenderer() instanceof RenderWeather) {
			StormSplashRenderer.renderStormSplashes(EnvironState.getDimensionId(), theThis);
			return true;
		}
		return false;
	}

	@Override
	public void render(final float partialTicks, @Nonnull final WorldClient world, @Nonnull final Minecraft mc) {
		this.stormRenderer.render(mc.entityRenderer, partialTicks);
	}

	/**
	 * Hook the weather renderer for the loading world.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onWorldLoad(@Nonnull final WorldEvent.Load e) {

		if (!ModOptions.asm.enableWeatherASM || !e.getWorld().isRemote)
			return;

		// Only want to hook if the provider doesn't have special
		// weather handling.
		final WorldProvider provider = e.getWorld().provider;
		final String dimName = provider.getDimensionType().getName();
		final IRenderHandler renderer = provider.getWeatherRenderer();
		if (renderer == null) {
			ModBase.log().info("Setting weather renderer for dimension [%s]", dimName);
			provider.setWeatherRenderer(new RenderWeather());
		} else {
			ModBase.log().info("Not hooking weather renderer for dimension [%s] (%s)", dimName, renderer.getClass());
		}
	}

}
