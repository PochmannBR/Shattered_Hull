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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Honeypot;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ThiefSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.watabou.utils.Random;

public class Thief extends Mob {
	
	public Item item;
	
	{
		spriteClass = ThiefSprite.class;
		
		HP = HT = 20;
		defenseSkill = 12;
		
		EXP = 5;
		maxLvl = 11;

		loot = Random.oneOf(Generator.Category.RING, Generator.Category.ARTIFACT);
		lootChance = 0.03f; //initially, see lootChance()

		WANDERING = new Wandering();
		FLEEING = new Fleeing();

		properties.add(Property.UNDEAD);
	}

	private static final String ITEM = "item";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ITEM, item );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		item = (Item)bundle.get( ITEM );
	}

	//Shattered Hull: o pirata cata Laztro do chão
	@Override
	protected boolean act() {
		if (alignment == Alignment.ENEMY && item == null && state != FLEEING && paralysed <= 0) {
			Heap heap = Dungeon.level.heaps.get(pos);
			if (heap != null && heap.type == Heap.Type.HEAP && heap.peek() instanceof Gold) {
				item = heap.pickUp();
				if (Dungeon.level.heroFOV[pos]) {
					GLog.w(Messages.get(Thief.class, "grabbed", item.quantity()));
					CellEmitter.get(pos).burst(Speck.factory(Speck.COIN), 4);
				}
				if (state == HUNTING) state = FLEEING;
				spend( 1f );
				return true;
			}
		}
		return super.act();
	}

	private int nearestLaztro() {
		int best = -1;
		int bestDist = 9;
		for (Heap heap : Dungeon.level.heaps.valueList()) {
			if (heap.type != Heap.Type.HEAP || !(heap.peek() instanceof Gold)) continue;
			if (fieldOfView == null || heap.pos >= fieldOfView.length || !fieldOfView[heap.pos]) continue;
			int d = Dungeon.level.distance(pos, heap.pos);
			if (d < bestDist) {
				bestDist = d;
				best = heap.pos;
			}
		}
		return best;
	}

	@Override
	public float speed() {
		if (item != null) return (5*super.speed())/6;
		else return super.speed();
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 10 );
	}

	@Override
	public float attackDelay() {
		return super.attackDelay()*0.5f;
	}

	@Override
	public float lootChance() {
		//each drop makes future drops 1/3 as likely
		// so loot chance looks like: 1/33, 1/100, 1/300, 1/900, etc.
		return super.lootChance() * (float)Math.pow(1/3f, Dungeon.LimitedDrops.THEIF_MISC.count);
	}

	@Override
	public void rollToDropLoot() {
		if (item != null) {
			Dungeon.level.drop( item, pos ).sprite.drop();
			//updates position
			if (item instanceof Honeypot.ShatteredPot) ((Honeypot.ShatteredPot)item).dropPot( this, pos );
			item = null;
		}
		super.rollToDropLoot();
	}

	@Override
	public Item createLoot() {
		Dungeon.LimitedDrops.THEIF_MISC.count++;
		return super.createLoot();
	}

	@Override
	public int attackSkill( Char target ) {
		return 12;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 3);
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		
		if (alignment == Alignment.ENEMY && item == null
				&& enemy instanceof Hero && steal( (Hero)enemy )) {
			state = FLEEING;
		}

		return damage;
	}

	@Override
	public int defenseProc(Char enemy, int damage) {
		if (state == FLEEING) {
			Dungeon.level.drop( new Gold(), pos ).sprite.drop();
		}

		return super.defenseProc(enemy, damage);
	}

	protected boolean steal( Hero hero ) {

		Item toSteal = hero.belongings.randomUnequipped();

		if (toSteal != null && !toSteal.unique && toSteal.level() < 1 ) {

			GLog.w( Messages.get(Thief.class, "stole", toSteal.name()) );
			if (!toSteal.stackable) {
				Dungeon.quickslot.convertToPlaceholder(toSteal);
			}
			Item.updateQuickslot();

			item = toSteal.detach( hero.belongings.backpack );
			if (item instanceof Honeypot){
				item = ((Honeypot)item).shatter(this, this.pos);
			} else if (item instanceof Honeypot.ShatteredPot) {
				((Honeypot.ShatteredPot)item).pickupPot(this);
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public String description() {
		String desc = super.description();

		if (item != null) {
			desc += Messages.get(this, "carries", item.name() );
		}

		return desc;
	}
	
	private class Wandering extends Mob.Wandering {
		
		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			if (item == null && alignment == Alignment.ENEMY) {
				int laztro = nearestLaztro();
				if (laztro != -1) target = laztro;
			}
			super.act(enemyInFOV, justAlerted);
			
			//if an enemy is just noticed and the thief posses an item, run, don't fight.
			if (state == HUNTING && item != null){
				state = FLEEING;
			}
			
			return true;
		}
	}

	private class Fleeing extends Mob.Fleeing {
		@Override
		protected void escaped() {
			if (item != null
					&& !Dungeon.level.heroFOV[pos]
					&& Dungeon.level.distance(Dungeon.hero.pos, pos) >= 6) {

				int count = 32;
				int newPos;
				do {
					newPos = Dungeon.level.randomRespawnCell( Thief.this );
					if (count-- <= 0) {
						break;
					}
				} while (newPos == -1 || Dungeon.level.heroFOV[newPos] || Dungeon.level.distance(newPos, pos) < (count/3));

				if (newPos != -1) {

					pos = newPos;
					sprite.place( pos );
					sprite.visible = Dungeon.level.heroFOV[pos];
					if (Dungeon.level.heroFOV[pos]) CellEmitter.get(pos).burst(Speck.factory(Speck.WOOL), 6);

				}

				if (item != null) GLog.n( Messages.get(Thief.class, "escapes", item.name()));
				item = null;
				state = WANDERING;
			} else {
				state = WANDERING;
			}
		}
	}
}
