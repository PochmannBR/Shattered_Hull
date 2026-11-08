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

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SnakeSprite;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Snake extends Mob {
	
	{
		spriteClass = SnakeSprite.class;
		
		HP = HT = 4;
		defenseSkill = 25;
		
		EXP = 2;
		maxLvl = 7;
		
		loot = Generator.Category.SEED;
		lootChance = 0.25f;
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 4 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 10;
	}

	//Shattered Hull: o emboscador causa dano dobrado no primeiro golpe
	//quando chega sem ter sido visto pelo herói
	private boolean ambushReady = true;
	private int seenTurns = 0;

	@Override
	protected boolean act() {
		if (Dungeon.level.heroFOV[pos]) {
			seenTurns++;
			if (seenTurns >= 2) ambushReady = false;
		} else {
			seenTurns = 0;
			ambushReady = true;
		}
		return super.act();
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc(enemy, damage);
		if (ambushReady && enemy == Dungeon.hero && alignment == Alignment.ENEMY) {
			damage *= 2;
			ambushReady = false;
			if (sprite != null) sprite.showStatus(CharSprite.NEGATIVE, Messages.get(this, "ambush_status"));
			GLog.w(Messages.get(this, "ambush"));
		}
		return damage;
	}

	private static final String AMBUSH = "ambush_ready";
	private static final String SEEN   = "seen_turns";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(AMBUSH, ambushReady);
		bundle.put(SEEN, seenTurns);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(AMBUSH)) ambushReady = bundle.getBoolean(AMBUSH);
		if (bundle.contains(SEEN)) seenTurns = bundle.getInt(SEEN);
	}

	private static int dodges = 0;

	@Override
	public String defenseVerb() {
		if (Dungeon.level.heroFOV[pos]) {
			dodges++;
		}
		if ((dodges >= 2 && !Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_SURPRISE_ATKS))
				|| (dodges >= 4 && !Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_1))){
			GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_SURPRISE_ATKS);
			dodges = 0;
		}
		return super.defenseVerb();
	}
}
