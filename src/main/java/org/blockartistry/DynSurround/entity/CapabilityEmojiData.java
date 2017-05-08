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

package org.blockartistry.DynSurround.entity;

import java.util.concurrent.Callable;

import org.blockartistry.DynSurround.api.entity.IEmojiData;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityEmojiData {
	
	public static final class Storage implements Capability.IStorage<IEmojiData> {

		@Override
		public NBTBase writeNBT(Capability<IEmojiData> capability, IEmojiData instance, EnumFacing side) {
			return null;
		}

		@Override
		public void readNBT(Capability<IEmojiData> capability, IEmojiData instance, EnumFacing side, NBTBase nbt) {
		}
		
	}
	
	public static final class Factory implements Callable<IEmojiData> {

		@Override
		public IEmojiData call() throws Exception {
			return new EmojiData();
		}
		
	}
	
	public static void register() {
		CapabilityManager.INSTANCE.register(IEmojiData.class, new Storage(), new Factory());
	}

}