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

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;

public class InvadirMaquina extends HabilidadeSintetica {

	public static final InvadirMaquina INSTANCE = new InvadirMaquina();

	public static final int ALCANCE = 6;

	@Override
	public int custoBase(Hero hero) {
		return 60;
	}

	@Override
	public int icon() {
		return HeroIcon.MIND_FORM;
	}

	@Override
	public int targetingFlags() {
		return -1;
	}

	public static boolean invadivel(Char ch){
		return ch instanceof Mob && ch.alignment == Char.Alignment.ENEMY && Sintetico.maquina(ch)
				&& !Char.hasProp(ch, Char.Property.BOSS) && !Char.hasProp(ch, Char.Property.MINIBOSS);
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {
		escolherCelula(tome, hero);
	}

	@Override
	protected void naCelula(HolyTome tome, Hero hero, int cell) {
		Char alvo = Actor.findChar(cell);
		if (!invadivel(alvo) || !Dungeon.level.heroFOV[cell]){
			GLog.w(Messages.get(this, "sem_maquina"));
			return;
		}
		if (Dungeon.level.distance(hero.pos, cell) > ALCANCE){
			GLog.w(Messages.get(this, "longe"));
			return;
		}
		invadir(hero, alvo);
		//invasão em rede: a máquina mais próxima do alvo também cai, uma por ponto
		int extras = hero.pointsInTalent(Talent.INVASAO_REDE);
		for (int i = 0; i < extras; i++){
			Char prox = null;
			for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])){
				if (m == alvo || !invadivel(m)) continue;
				if (prox == null || Dungeon.level.distance(alvo.pos, m.pos) < Dungeon.level.distance(alvo.pos, prox.pos)) prox = m;
			}
			if (prox == null || Dungeon.level.distance(alvo.pos, prox.pos) > ALCANCE) break;
			invadir(hero, prox);
		}
		Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
		concluir(tome, hero);
	}

	public static void invadir(Hero hero, Char alvo){
		Buff.affect(alvo, Sintetico.Invadido.class, Sintetico.duracaoInvasao(hero));
		if (alvo.sprite != null) alvo.sprite.centerEmitter().burst(SparkParticle.FACTORY, 6);
		GLog.p(Messages.get(InvadirMaquina.class, "feito", alvo.name()));
	}
}
