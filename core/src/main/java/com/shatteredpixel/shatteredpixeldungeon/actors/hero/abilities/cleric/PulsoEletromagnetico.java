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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DroneSintetico;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.hull.Sintetico;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

//Shattered Hull: pulso eletromagnético do Sintético, no lugar da forma ascendida
public class PulsoEletromagnetico extends ArmorAbility {

	{
		baseChargeUse = 50;
	}

	public static int raio(Hero h){
		return 2 + h.pointsInTalent(Talent.RAIO_AMPLIADO);
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		int r = raio(hero);
		ArrayList<Integer> area = new ArrayList<>();
		boolean[] livre = new boolean[Dungeon.level.length()];
		for (int i = 0; i < livre.length; i++) livre[i] = !Dungeon.level.solid[i];
		PathFinder.buildDistanceMap(hero.pos, livre, r);
		for (int i = 0; i < PathFinder.distance.length; i++){
			if (PathFinder.distance[i] <= r) area.add(i);
		}
		int captado = 0;
		int pc = hero.pointsInTalent(Talent.CAPTACAO);
		for (int c : area){
			Char ch = Actor.findChar(c);
			if (ch != null && ch != hero && ch.alignment == Char.Alignment.ENEMY){
				boolean maquina = Sintetico.maquina(ch);
				Buff.prolong(ch, Paralysis.class, maquina ? 5f : 3f);
				captado += 5 * pc * (maquina ? 2 : 1);
			}
			Trap t = Dungeon.level.traps.get(c);
			if (t != null && t.active){
				t.reveal();
				t.disarm();
				GameScene.updateMap(c);
			}
			if (Dungeon.level.heroFOV[c] && Random.Int(3) == 0) CellEmitter.get(c).burst(SparkParticle.FACTORY, 2);
		}
		//protegido durante o pulso
		Buff.affect(hero, Barrier.class).incShield(Math.round(hero.HT * 0.2f));
		if (captado > 0) Sintetico.ganhar(hero, captado);
		int pr = hero.pointsInTalent(Talent.CAMPO_RESIDUAL);
		if (pr > 0){
			Sintetico.CampoResidual cr = Buff.append(hero, Sintetico.CampoResidual.class);
			cr.celulas = new int[area.size()];
			for (int i = 0; i < area.size(); i++) cr.celulas[i] = area.get(i);
			cr.turnos = 2 * pr;
		}
		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();
		Sample.INSTANCE.play(Assets.Sounds.LIGHTNING);
		if (hero.sprite != null) hero.sprite.operate(hero.pos);
		hero.spendAndNext(Actor.TICK);
	}

	@Override
	public int icon() {
		return HeroIcon.ASCENDED_FORM;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.RAIO_AMPLIADO, Talent.CAMPO_RESIDUAL, Talent.CAPTACAO, Talent.HEROIC_ENERGY};
	}
}
