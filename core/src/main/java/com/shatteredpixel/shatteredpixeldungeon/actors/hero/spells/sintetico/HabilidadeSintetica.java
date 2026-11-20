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

//Shattered Hull: habilidade do núcleo sintético, paga com a reserva de energia
public abstract class HabilidadeSintetica extends ClericSpell {

	public abstract int custoBase(Hero hero);

	@Override
	public float chargeUse(Hero hero) {
		return Sintetico.custo(hero, custoBase(hero), this);
	}

	@Override
	public boolean canCast(Hero hero) {
		return !Sintetico.economia(hero);
	}

	@Override
	public String shortDesc() {
		return Messages.get(this, "short_desc") + " " + Messages.get(HabilidadeSintetica.class, "custo", (int)chargeUse(Dungeon.hero));
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc") + "\n\n" + Messages.get(HabilidadeSintetica.class, "custo", (int)chargeUse(Dungeon.hero));
	}

	//habilidade com alvo num quadrado
	protected void escolherCelula(final HolyTome tome, final Hero hero){
		GameScene.selectCell(new CellSelector.Listener() {
			@Override
			public void onSelect(Integer cell) {
				if (cell != null) naCelula(tome, hero, cell);
			}
			@Override
			public String prompt() {
				return Messages.get(HabilidadeSintetica.this, "prompt");
			}
		});
	}

	protected void naCelula(HolyTome tome, Hero hero, int cell){
	}

	//conclui o uso: gasta a energia, registra e passa o turno
	protected void concluir(HolyTome tome, Hero hero){
		onSpellCast(tome, hero);
		hero.spendAndNext(Actor.TICK);
	}
}
