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

import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;

public class DesligarArmadilha extends HabilidadeSintetica {

	public static final DesligarArmadilha INSTANCE = new DesligarArmadilha();

	@Override
	public int custoBase(Hero hero) {
		return 15;
	}

	@Override
	public int icon() {
		return HeroIcon.HOLY_INTUITION;
	}

	@Override
	public int targetingFlags() {
		return -1;
	}

	public static int alcance(Hero hero){
		return 1 + 2 * hero.pointsInTalent(Talent.DESARME_REMOTO);
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {
		escolherCelula(tome, hero);
	}

	@Override
	protected void naCelula(HolyTome tome, Hero hero, int cell) {
		Trap t = Dungeon.level.traps.get(cell);
		if (t == null || !t.active || !t.visible){
			GLog.w(Messages.get(this, "sem_armadilha"));
			return;
		}
		if (Dungeon.level.distance(hero.pos, cell) > alcance(hero)){
			GLog.w(Messages.get(this, "longe"));
			return;
		}
		t.disarm();
		Sample.INSTANCE.play(Assets.Sounds.TRAP);
		GameScene.updateMap(cell);
		GLog.i(Messages.get(this, "feito"));
		concluir(tome, hero);
	}
}
