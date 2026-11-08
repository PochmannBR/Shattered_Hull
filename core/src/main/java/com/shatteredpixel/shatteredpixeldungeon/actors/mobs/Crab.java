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
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CrabSprite;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Crab extends Mob {

	{
		spriteClass = CrabSprite.class;
		
		HP = HT = 15;
		defenseSkill = 5;
		baseSpeed = 2f;
		
		EXP = 4;
		maxLvl = 9;
		
		loot = MysteryMeat.class;
		lootChance = 0.167f;
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 7 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 12;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 4);
	}

	//Shattered Hull: o escavador se enterra e reaparece ao lado do alvo
	private int burrowCooldown = 4;

	@Override
	protected boolean act() {
		if (burrowCooldown > 0) burrowCooldown--;

		if (paralysed <= 0 && state == HUNTING && enemy != null && enemy.isAlive()
				&& alignment == Alignment.ENEMY && burrowCooldown <= 0
				&& Dungeon.level.distance(pos, enemy.pos) >= 3 && Random.Int(3) == 0) {
			int cell = emergeCell(enemy.pos);
			if (cell != -1) {
				burrowTo(cell);
				burrowCooldown = 10;
				spend( 1f );
				return true;
			}
		}
		return super.act();
	}

	private int emergeCell( int target ){
		ArrayList<Integer> cells = new ArrayList<>();
		for (int n : PathFinder.NEIGHBOURS8){
			int c = target + n;
			if (c >= 0 && c < Dungeon.level.length() && Dungeon.level.passable[c]
					&& !Dungeon.level.solid[c] && Actor.findChar(c) == null
					&& !Dungeon.level.pit[c]){
				cells.add(c);
			}
		}
		return cells.isEmpty() ? -1 : Random.element(cells);
	}

	private void burrowTo( int cell ){
		boolean seenFrom = Dungeon.level.heroFOV[pos];
		if (seenFrom) CellEmitter.get(pos).burst(Speck.factory(Speck.ROCK), 6);
		move(cell, false);
		if (sprite != null) {
			sprite.place(cell);
			sprite.visible = Dungeon.level.heroFOV[cell];
		}
		if (Dungeon.level.heroFOV[cell]) {
			CellEmitter.get(cell).burst(Speck.factory(Speck.ROCK), 8);
			Sample.INSTANCE.play(Assets.Sounds.ROCKS, 0.6f, 1.4f);
			GLog.w(Messages.get(this, "burrow"));
		}
	}

	private static final String BURROW_CD = "burrow_cd";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(BURROW_CD, burrowCooldown);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(BURROW_CD)) burrowCooldown = bundle.getInt(BURROW_CD);
	}
}
