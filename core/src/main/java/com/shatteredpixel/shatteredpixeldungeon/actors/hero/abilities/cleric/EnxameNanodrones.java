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

//Shattered Hull: enxame de nanodrones do Sintético, no lugar do poder de muitos
public class EnxameNanodrones extends ArmorAbility {

	{
		baseChargeUse = 50;
	}

	public static final float DURACAO = 20f;

	public static int quantidade(Hero h){
		return 3 + h.pointsInTalent(Talent.ENXAME_MAIOR);
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		ArrayList<Integer> livres = new ArrayList<>();
		for (int n : PathFinder.NEIGHBOURS8){
			int p = hero.pos + n;
			if (Dungeon.level.passable[p] && Actor.findChar(p) == null) livres.add(p);
		}
		if (livres.isEmpty()){
			GLog.w(Messages.get(this, "sem_espaco"));
			return;
		}
		int n = Math.min(quantidade(hero), livres.size());
		for (int i = 0; i < n; i++){
			int p = livres.remove(Random.Int(livres.size()));
			DroneSintetico.Nanodrone d = new DroneSintetico.Nanodrone();
			d.configurar(hero);
			GameScene.add(d);
			ScrollOfTeleportation.appear(d, p);
			Buff.affect(d, DroneSintetico.Vida.class, DURACAO);
		}
		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();
		Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
		hero.spendAndNext(Actor.TICK);
	}

	@Override
	public int icon() {
		return HeroIcon.POWER_OF_MANY;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.ENXAME_MAIOR, Talent.NANODRONES_EXPLOSIVOS, Talent.REPARO_ENXAME, Talent.HEROIC_ENERGY};
	}
}
