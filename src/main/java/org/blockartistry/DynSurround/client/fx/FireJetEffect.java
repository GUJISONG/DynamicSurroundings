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

package org.blockartistry.DynSurround.client.fx;

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.api.effects.BlockEffectType;
import org.blockartistry.DynSurround.client.fx.particle.system.ParticleFireJet;
import org.blockartistry.DynSurround.client.fx.particle.system.ParticleJet;
import org.blockartistry.lib.WorldUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FireJetEffect extends JetEffect {

	public FireJetEffect(final int chance) {
		super(chance);
	}

	@Override
	@Nonnull
	public BlockEffectType getEffectType() {
		return BlockEffectType.FIRE_JET;
	}

	@Override
	public boolean canTrigger(final IBlockState state, final World world, final BlockPos pos, final Random random) {
		return WorldUtils.isAirBlock(world, pos.getX(), pos.getY() + 1, pos.getZ())
				&& super.canTrigger(state, world, pos, random);
		// return super.canTrigger(state, world, pos, random)
		// && WorldUtils.isAirBlock(world, pos.getX(), pos.getY() + 1,
		// pos.getZ());
	}

	@Override
	public void doEffect(final IBlockState state, final World world, final BlockPos pos, final Random random) {
		final int lavaBlocks = countBlocks(world, pos, state, -1);
		final double spawnHeight = jetSpawnHeight(state, pos);
		final ParticleJet effect = new ParticleFireJet(lavaBlocks, world, pos.getX() + 0.5D, spawnHeight,
				pos.getZ() + 0.5D);
		addEffect(effect);
	}
}