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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.hull.Renegado;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

//Shattered Hull: feromônio de comando do Renegado, no lugar da finta
public class FeromonioComando extends ArmorAbility {

	{
		baseChargeUse = 50;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		int raio = 3 + hero.pointsInTalent(Talent.RAIO_FEROMONIO);
		float duracao = 5f + 2f * hero.pointsInTalent(Talent.FEROMONIO_DURADOURO);
		int ex = hero.pointsInTalent(Talent.EXAUSTAO);
		int n = 0;
		for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])){
			if (Renegado.enxame(m) && Dungeon.level.distance(hero.pos, m.pos) <= raio){
				Buff.affect(m, Amok.class, duracao);
				if (ex > 0) Buff.affect(m, Renegado.Exaustao.class, duracao).pontos = ex;
				n++;
			}
		}
		if (n == 0){
			GLog.w(Messages.get(this, "ninguem"));
			return;
		}
		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();
		Sample.INSTANCE.play(Assets.Sounds.CHARMS);
		if (hero.sprite != null) hero.sprite.operate(hero.pos);
		hero.spendAndNext(Actor.TICK);
	}

	@Override
	public int icon() {
		return HeroIcon.FEINT;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.RAIO_FEROMONIO, Talent.FEROMONIO_DURADOURO, Talent.EXAUSTAO, Talent.HEROIC_ENERGY};
	}
}
