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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.sintetico;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ClericSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.hull.Sintetico;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.watabou.utils.Random;

//Shattered Hull: descarga elétrica, o golpe próprio do Sintético desde o nível 1
public class DescargaEletrica extends HabilidadeSintetica {

	public static final DescargaEletrica INSTANCE = new DescargaEletrica();

	@Override
	public int custoBase(Hero hero) {
		return 20;
	}

	@Override
	public int icon() {
		return HeroIcon.GUIDING_LIGHT;
	}

	@Override
	public int targetingFlags() {
		return -1;
	}

	public static int danoMin(Hero hero){
		return 3 + hero.lvl;
	}

	public static int danoMax(Hero hero){
		return 6 + hero.lvl;
	}

	@Override
	public String desc() {
		Hero h = Dungeon.hero;
		return Messages.get(this, "desc", danoMin(h), danoMax(h)) + "\n\n" + Messages.get(HabilidadeSintetica.class, "custo", (int)chargeUse(h));
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {
		escolherCelula(tome, hero);
	}

	@Override
	protected void naCelula(HolyTome tome, Hero hero, int cell) {
		Char alvo = Actor.findChar(cell);
		if (alvo == null || alvo == hero || alvo.alignment == Char.Alignment.ALLY || !Dungeon.level.adjacent(hero.pos, cell)){
			GLog.w(Messages.get(this, "sem_alvo"));
			return;
		}
		if (hero.sprite != null) hero.sprite.zap(cell);
		alvo.damage(Random.NormalIntRange(danoMin(hero), danoMax(hero)), this);
		if (alvo.isAlive()) Buff.prolong(alvo, Paralysis.class, 1f);
		if (alvo.sprite != null) alvo.sprite.centerEmitter().burst(SparkParticle.FACTORY, 8);
		Sample.INSTANCE.play(Assets.Sounds.LIGHTNING);
		concluir(tome, hero);
	}
}
