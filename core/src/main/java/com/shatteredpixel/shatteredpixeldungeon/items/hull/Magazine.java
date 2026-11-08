/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Shattered Hull
 * Copyright (C) 2026 Pablo Pochmann / Insane Goblin
 */


package com.shatteredpixel.shatteredpixeldungeon.items.hull;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

//Shattered Hull: carregador do fuzil. Carregadores cheios empilham;
//um carregador parcial ocupa o próprio espaço na mochila.
public class Magazine extends Item {

	public static final int CAPACITY = 12;

	public int rounds = CAPACITY;

	{
		image = ItemSpriteSheet.MAGAZINE;
		stackable = true;
	}

	public Magazine(){
		super();
	}

	public Magazine( int rounds ){
		this();
		setRounds(rounds);
	}

	public Magazine setRounds( int rounds ){
		this.rounds = Math.max(0, Math.min(CAPACITY, rounds));
		image = this.rounds < CAPACITY ? ItemSpriteSheet.MAGAZINE_PARTIAL : ItemSpriteSheet.MAGAZINE;
		return this;
	}

	@Override
	public boolean isSimilar( Item item ) {
		return super.isSimilar(item) && ((Magazine)item).rounds == rounds;
	}

	@Override
	public String name() {
		if (rounds < CAPACITY){
			return Messages.get(this, "name_partial", rounds, CAPACITY);
		}
		return super.name();
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", CAPACITY);
	}

	@Override
	public String status() {
		if (rounds < CAPACITY){
			return quantity > 1 ? quantity + "x" + rounds : Integer.toString(rounds);
		}
		return super.status();
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 3 * quantity;
	}

	private static final String ROUNDS = "rounds";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(ROUNDS, rounds);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(ROUNDS)) setRounds(bundle.getInt(ROUNDS));
	}
}
