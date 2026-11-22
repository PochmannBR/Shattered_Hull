/*
 * Shattered Hull
 * Copyright (C) 2026 Pablo Pochmann / Insane Goblin
 *
 * Baseado em Shattered Pixel Dungeon, Copyright (C) 2014-2025 Evan Debenham,
 * e Pixel Dungeon, Copyright (C) 2012-2015 Oleg Dobrokhotov.
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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PrismaticGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM100Sprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

//Shattered Hull: drone de apoio do Sintético. Base da imagem prismática, com as ordens do Eco do Sgt. Brandt.
public class DroneSintetico extends DirectableAlly {

	{
		spriteClass = Sprite.class;
		HP = HT = 10;
		defenseSkill = 1;
		properties.add(Property.INORGANIC);
		properties.add(Property.ELECTRIC);
		immunities.add(ToxicGas.class);
		immunities.add(CorrosiveGas.class);
	}

	protected Hero hero(){
		return Dungeon.hero;
	}

	public static DroneSintetico ativo(){
		if (Dungeon.level == null) return null;
		for (Mob m : Dungeon.level.mobs){
			if (m.getClass() == DroneSintetico.class && m.isAlive()) return (DroneSintetico) m;
		}
		return null;
	}

	public static int vidaMax(Hero h){
		float v = PrismaticGuard.maxHP(h);
		if (h.subClass == HeroSubClass.PRIEST) v *= 1.25f;
		v *= 1f + 0.2f * h.pointsInTalent(Talent.REDE_DRONES);
		return Math.round(v);
	}

	public void configurar(Hero h){
		HT = HP = vidaMax(h);
	}

	@Override
	protected boolean act() {
		Hero h = hero();
		if (h != null && getClass() == DroneSintetico.class){
			int ht = vidaMax(h);
			if (ht > HT){
				HP += ht - HT;
				HT = ht;
			}
		}
		return super.act();
	}

	//drone de combate: alcance de disparo
	protected int alcance(){
		Hero h = hero();
		return h == null ? 1 : (h.pointsInTalent(Talent.DRONE_COMBATE) > 0 ? 4 : 1);
	}

	@Override
	protected boolean canAttack(Char enemy) {
		if (alcance() > 1){
			Ballistica b = new Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT);
			return b.collisionPos == enemy.pos && Dungeon.level.distance(pos, enemy.pos) <= alcance();
		}
		return super.canAttack(enemy);
	}

	@Override
	public int damageRoll() {
		Hero h = hero();
		if (h == null) return Random.NormalIntRange(2, 4);
		int extra = 2 * h.pointsInTalent(Talent.DRONE_COMBATE);
		return Random.NormalIntRange(2 + h.lvl / 4 + extra, 4 + h.lvl / 2 + extra);
	}

	@Override
	public int attackSkill(Char target) {
		Hero h = hero();
		return h == null ? 0 : 9 + h.lvl;
	}

	@Override
	public int defenseSkill(Char enemy) {
		Hero h = hero();
		return h == null ? 0 : 4 + h.lvl;
	}

	@Override
	public int drRoll() {
		Hero h = hero();
		int dr = super.drRoll();
		if (h != null && h.belongings.armor() != null) dr += Random.NormalIntRange(h.belongings.armor().DRMin(), h.belongings.armor().DRMax());
		return dr;
	}

	@Override
	public int attackProc(Char enemy, int damage) {
		if (enemy instanceof Mob) ((Mob) enemy).aggro(this);
		return super.attackProc(enemy, damage);
	}

	@Override
	public String description() {
		return Messages.get(this, "desc");
	}

	public static class Sprite extends DM100Sprite {
		public Sprite(){
			super();
			hardlight(0.4f, 0.9f, 1f);
		}

		@Override
		public void resetColor() {
			super.resetColor();
			hardlight(0.4f, 0.9f, 1f);
		}
	}

	//---------------------------------------------------------------- nanodrones do enxame

	public static class Nanodrone extends DroneSintetico {

		{
			spriteClass = NanoSprite.class;
			HP = HT = 6;
		}

		private float reparo = 0;

		@Override
		public void configurar(Hero h) {
			HT = HP = 5 + h.lvl;
		}

		@Override
		protected boolean act() {
			Hero h = hero();
			if (h != null){
				//reparo em enxame: 1% da vida máxima por ponto, por turno, no Sintético e no drone principal
				int p = h.pointsInTalent(Talent.REPARO_ENXAME);
				if (p > 0){
					reparo += 0.01f * p;
					curar(h);
					DroneSintetico d = ativo();
					if (d != null) curar(d);
					reparo = 0;
				}
			}
			if (buff(Vida.class) == null){
				die(null);
				return true;
			}
			return super.act();
		}

		private void curar(Char c){
			int v = Math.round(c.HT * reparo);
			if (v > 0 && c.HP < c.HT) c.HP = Math.min(c.HT, c.HP + v);
		}

		@Override
		protected int alcance() {
			return 1;
		}

		@Override
		public int damageRoll() {
			Hero h = hero();
			return h == null ? Random.NormalIntRange(1, 3) : Random.NormalIntRange(1 + h.lvl / 6, 3 + h.lvl / 4);
		}

		@Override
		public void die(Object cause) {
			Hero h = hero();
			int p = h == null ? 0 : h.pointsInTalent(Talent.NANODRONES_EXPLOSIVOS);
			if (p > 0 && Dungeon.level != null){
				int dano = 2 + 2 * p + (h.lvl / 3);
				for (int n : PathFinder.NEIGHBOURS9){
					Char ch = Actor.findChar(pos + n);
					if (ch != null && ch.alignment == Alignment.ENEMY){
						ch.damage(dano, this);
						if (p >= 4 && ch.isAlive()) Buff.prolong(ch, Paralysis.class, 2f);
					}
				}
				if (Dungeon.level.heroFOV[pos]) CellEmitter.center(pos).burst(BlastParticle.FACTORY, 8);
			}
			super.die(cause);
		}

		@Override
		public String description() {
			return Messages.get(this, "desc");
		}

	}

	public static class Vida extends FlavourBuff {
	}

	public static class NanoSprite extends Sprite {
		public NanoSprite(){
			super();
			scale.set(0.6f);
		}
	}
}
