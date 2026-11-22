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

//Shattered Hull: forma do enxame do Renegado, no lugar do golpe elemental
public class FormaEnxameAbility extends ArmorAbility {

	{
		baseChargeUse = 50;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		if (hero.buff(Renegado.FormaEnxame.class) != null){
			GLog.w(Messages.get(this, "ativa"));
			return;
		}
		Buff.affect(hero, Renegado.FormaEnxame.class, 10f + 3f * hero.pointsInTalent(Talent.FORMA_DURADOURA));
		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();
		Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
		if (hero.sprite != null) hero.sprite.operate(hero.pos);
		hero.spendAndNext(Actor.TICK);
	}

	@Override
	public int icon() {
		return HeroIcon.ELEMENTAL_STRIKE;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.FORMA_DURADOURA, Talent.SALTO_CONTIDO, Talent.REGENERACAO_ENXAME, Talent.HEROIC_ENERGY};
	}
}
