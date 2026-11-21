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

public class AbrirFechadura extends HabilidadeSintetica {

	public static final AbrirFechadura INSTANCE = new AbrirFechadura();

	//cada carga da antiga chave caveira vale 20 de energia, 15 e 10 com a chave universal
	public static int unidade(Hero hero){
		return 20 - 5 * hero.pointsInTalent(Talent.CHAVE_UNIVERSAL);
	}

	@Override
	public int custoBase(Hero hero) {
		return unidade(hero);
	}

	@Override
	public int icon() {
		return HeroIcon.HOLY_WARD;
	}

	@Override
	public int targetingFlags() {
		return -1;
	}

	@Override
	public String desc() {
		Hero h = Dungeon.hero;
		int u = Sintetico.custo(h, unidade(h), this);
		return Messages.get(this, "desc", u, 2 * u, 5 * u);
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {
		ChaveSintetica.usar(hero, ChaveSintetica.Modo.ABRIR, Sintetico.custo(hero, unidade(hero), this), this);
	}
}
