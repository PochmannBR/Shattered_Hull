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

//Shattered Hull: assimilação forçada do Renegado, no lugar do desafio
public class AssimilacaoForcada extends ArmorAbility {

	{
		baseChargeUse = 35;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	public int targetedPos(Char user, int dst) {
		return dst;
	}

	@Override
	protected void activate(final ClassArmor armor, final Hero hero, Integer target) {
		if (target == null) return;
		final Char alvo = Actor.findChar(target);
		if (alvo == null || alvo == hero || alvo.alignment == Char.Alignment.ALLY || !Dungeon.level.adjacent(hero.pos, target)){
			GLog.w(Messages.get(Renegado.class, "sem_alvo_adjacente"));
			return;
		}
		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();
		hero.busy();
		final float mult = 2f + 0.25f * hero.pointsInTalent(Talent.DANO_ASSIMILADOR);
		Callback golpe = new Callback() {
			@Override
			public void call() {
				Buff.affect(hero, Renegado.AssimilacaoGarantida.class, 1f);
				hero.attack(alvo, mult, 0, Char.INFINITE_ACCURACY);
				if (hero.buff(Renegado.AssimilacaoGarantida.class) != null) hero.buff(Renegado.AssimilacaoGarantida.class).detach();
				int gp = hero.pointsInTalent(Talent.GOLPE_PURGADOR);
				if (gp > 0) Renegado.medidor(hero).baixar(5 * gp);
				hero.spendAndNext(hero.attackDelay());
			}
		};
		if (hero.sprite != null) hero.sprite.attack(alvo.pos, golpe);
		else golpe.call();
	}

	@Override
	public int icon() {
		return HeroIcon.CHALLENGE;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.DANO_ASSIMILADOR, Talent.COLETA_FORCADA, Talent.GOLPE_PURGADOR, Talent.HEROIC_ENERGY};
	}
}
