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

package com.shatteredpixel.shatteredpixeldungeon.items.hull;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

//Shattered Hull: injetor supressor, que baixa o medidor de assimilação do Renegado
public class InjetorSupressor extends Item {

	public static final String AC_INJETAR = "INJETAR";

	{
		image = ItemSpriteSheet.HEALING_DART;
		defaultAction = AC_INJETAR;
		stackable = true;
	}

	public static int efeito(Hero h){
		int base = h.subClass == HeroSubClass.MONK ? 60 : 40;
		return base + 10 * h.pointsInTalent(Talent.INJECAO_EFICIENTE);
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_INJETAR);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_INJETAR)){
			detach(hero.belongings.backpack);
			if (Renegado.e(hero)){
				Renegado.medidor(hero).baixar(efeito(hero));
				int dd = hero.pointsInTalent(Talent.DOSE_DUPLA);
				if (dd > 0) hero.HP = Math.min(hero.HT, hero.HP + Math.round(hero.HT * 0.1f * dd));
				GLog.p(Messages.get(this, "efeito"));
			} else {
				GLog.i(Messages.get(this, "sem_efeito"));
			}
			Sample.INSTANCE.play(Assets.Sounds.DRINK);
			hero.spendAndNext(1f);
			updateQuickslot();
		}
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing(0x33CCFF, 1.5f);
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 30 * quantity;
	}
}
