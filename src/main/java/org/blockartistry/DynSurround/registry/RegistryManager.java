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

package org.blockartistry.DynSurround.registry;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.client.event.RegistryEvent;
import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class RegistryManager {

	public static class RegistryType {
		public final static int SOUND = 0;
		public final static int BIOME = 1;
		public final static int BLOCK = 2;
		public final static int DIMENSION = 3;
		public final static int FOOTSTEPS = 4;
		public final static int SEASON = 5;
		public final static int ITEMS = 6;

		public final static int LENGTH = 7;
	}

	private static final RegistryManager[] managers = { null, null };

	@Nonnull
	private static RegistryManager getManager() {
		final Side side = FMLCommonHandler.instance().getEffectiveSide();
		final int idx = side.ordinal();
		if (managers[idx] == null) {
			managers[idx] = new RegistryManager(side);
			managers[idx].reload();
		}
		return managers[idx];
	}

	@Nonnull
	public static <T extends Registry> T get(@Nonnull final int type) {
		return (T) getManager().<T>getRegistry(type);
	}

	public static void reloadResources() {
		reloadResources(Side.CLIENT);
		reloadResources(Side.SERVER);
	}

	public static void reloadResources(@Nonnull final Side side) {
		// Reload can be called on either side so make sure we queue
		// up a scheduled task appropriately.
		final int idx = side.ordinal();
		if (managers[idx] != null) {
			final IThreadListener tl = side == Side.SERVER ? FMLCommonHandler.instance().getMinecraftServerInstance()
					: Minecraft.getMinecraft();
			if (tl == null)
				managers[idx] = null;
			else
				tl.addScheduledTask(new Runnable() {
					public void run() {
						managers[idx].reload();
					}
				});
		}
	}

	protected final Side side;
	protected final ResourceLocation SCRIPT;
	protected final Registry[] registries = new Registry[RegistryType.LENGTH];

	RegistryManager(final Side side) {
		this.side = side;
		this.registries[RegistryType.DIMENSION] = new DimensionRegistry(side);
		this.registries[RegistryType.BIOME] = new BiomeRegistry(side);
		this.registries[RegistryType.SOUND] = new SoundRegistry(side);
		this.registries[RegistryType.SEASON] = new SeasonRegistry(side);

		if (side == Side.CLIENT) {
			this.registries[RegistryType.BLOCK] = new BlockRegistry(side);
			this.registries[RegistryType.FOOTSTEPS] = new FootstepsRegistry(side);
			this.registries[RegistryType.ITEMS] = new ItemRegistry(side);
			this.SCRIPT = new ResourceLocation(DSurround.RESOURCE_ID, "configure.json");
		} else {
			this.SCRIPT = null;
		}
	}

	@SideOnly(Side.CLIENT)
	private boolean checkCompatible(@Nonnull final ResourcePackRepository.Entry pack) {
		return pack.getResourcePack().resourceExists(SCRIPT);
	}

	@SideOnly(Side.CLIENT)
	private InputStream openScript(@Nonnull final IResourcePack pack) throws IOException {
		return pack.getInputStream(SCRIPT);
	}

	void reload() {
		for (final Registry r : this.registries)
			if (r != null)
				r.init();

		new DataScripts(this.side).execute(getAdditionalScripts());

		for (final Registry r : this.registries)
			if (r != null)
				r.initComplete();

		MinecraftForge.EVENT_BUS.post(new RegistryEvent.Reload(this.side));
	}

	@SuppressWarnings("unchecked")
	protected <T> T getRegistry(final int type) {
		return (T) this.registries[type];
	}

	// NOTE: Server side has no resource packs so the client specific
	// code is not executed when initializing a server side registry.
	public List<InputStream> getAdditionalScripts() {
		if (this.side == Side.SERVER)
			return ImmutableList.of();

		final List<ResourcePackRepository.Entry> repo = Minecraft.getMinecraft().getResourcePackRepository()
				.getRepositoryEntries();

		final List<InputStream> streams = new ArrayList<InputStream>();

		// Look in other resource packs for more configuration data
		for (final ResourcePackRepository.Entry pack : repo) {
			if (checkCompatible(pack)) {
				DSurround.log().debug("Found script in resource pack: %s", pack.getResourcePackName());
				try {
					final InputStream stream = openScript(pack.getResourcePack());
					if (stream != null)
						streams.add(stream);
				} catch (final Throwable t) {
					DSurround.log().error("Unable to open script in resource pack", t);
				}
			}
		}
		return streams;
	}

}
