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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

//Shattered Hull: amostra de tecido do enxame, que o Renegado instala no corpo
public class AmostraItem extends Item {

	public static final String AC_INSTALAR = "INSTALAR";

	{
		image = ItemSpriteSheet.SEED_SUNGRASS;
		defaultAction = AC_INSTALAR;
		unique = false;
	}

	public Renegado.Amostra tipo = Renegado.Amostra.MANDIBULAS;

	public AmostraItem tipo(Renegado.Amostra t){
		tipo = t;
		return this;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (Renegado.e(hero)) actions.add(AC_INSTALAR);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_INSTALAR) && Renegado.e(hero)){
			Renegado.Medidor m = Renegado.medidor(hero);
			if (m.mutado(tipo.lugar)){
				GLog.w(Messages.get(this, "travado"));
				return;
			}
			detach(hero.belongings.backpack);
			m.instalar(tipo);
			int ar = hero.pointsInTalent(Talent.ADAPTACAO_RAPIDA);
			if (ar > 0) hero.HP = Math.min(hero.HT, hero.HP + Math.round(hero.HT * 0.1f * ar));
			Sample.INSTANCE.play(Assets.Sounds.DRINK);
			GLog.p(Messages.get(this, "instalada", tipo.nome()));
			hero.spendAndNext(1f);
		}
	}

	@Override
	public String name() {
		return Messages.get(this, "name", tipo.nome());
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", Messages.get(Renegado.class, "lugar_" + tipo.lugar.name().toLowerCase()), tipo.habilidade(), tipo.medidor);
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing(0x66FF33, 1f);
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
	public boolean isSimilar(Item item) {
		return item instanceof AmostraItem && ((AmostraItem) item).tipo == tipo;
	}

	@Override
	public int value() {
		return 20 * quantity;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("tipo", tipo);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		tipo = bundle.getEnum("tipo", Renegado.Amostra.class);
	}
}
