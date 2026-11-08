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


package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SpitterSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

//Shattered Hull: cuspidor do Enxame Vórtice. Cospe ácido a cada turno
//enquanto tiver linha de tiro; de perto, só arranha.
public class Spitter extends Mob {

	{
		spriteClass = SpitterSprite.class;

		HP = HT = 10;
		defenseSkill = 4;

		EXP = 4;
		maxLvl = 9;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 3 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 10;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( 0, 1 );
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		return super.canAttack(enemy)
				|| new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE ).collisionPos == enemy.pos;
	}

	@Override
	protected boolean doAttack( Char enemy ) {
		if (Dungeon.level.adjacent( pos, enemy.pos )
				|| new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE ).collisionPos != enemy.pos) {
			return super.doAttack( enemy );
		} else {
			if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
				sprite.zap( enemy.pos );
				return false;
			} else {
				spit();
				return true;
			}
		}
	}

	public static class AcidSpit{}

	private void spit() {
		spend( 1f );

		Invisibility.dispel(this);
		Char enemy = this.enemy;
		if (hit( this, enemy, true )) {
			int dmg = Random.NormalIntRange( 2, 4 );
			dmg = Math.round(dmg * AscensionChallenge.statModifier(this));
			enemy.damage( dmg, new AcidSpit() );
			if (enemy.isAlive() && Random.Int( 3 ) == 0) {
				Buff.affect( enemy, Ooze.class ).set( 6f );
				if (enemy == Dungeon.hero) Sample.INSTANCE.play( Assets.Sounds.DEBUFF );
			}

			if (!enemy.isAlive() && enemy == Dungeon.hero) {
				Badges.validateDeathFromEnemyMagic();
				Dungeon.fail( this );
				GLog.n( Messages.get(this, "spit_kill") );
			}
		} else {
			enemy.sprite.showStatus( CharSprite.NEUTRAL, enemy.defenseVerb() );
		}
	}

	public void onSpitComplete() {
		spit();
		next();
	}
}
