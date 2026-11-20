/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dobrokhotov
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * Shattered Hull
 * Copyright (C) 2026 Pablo Pochmann / Insane Goblin
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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

//Shattered Hull: célula de escudo do pedido de suprimento. Força 1 é o escudo de campo,
//2 o escudo reforçado, e 3 o reforçado com a embalagem reforçada.
public class CelulaEscudo extends Item {

	public static final String AC_ATIVAR = "ATIVAR";

	{
		image = ItemSpriteSheet.CELULA_ESCUDO;
		defaultAction = AC_ATIVAR;
	}

	public int forca = 1;

	public CelulaEscudo forca(int f){
		forca = f;
		return this;
	}

	public int escudo(Hero hero){
		return Math.round(hero.HT * 0.1f * (1 + forca));
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_ATIVAR);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_ATIVAR)){
			detach(hero.belongings.backpack);
			int e = escudo(hero);
			Buff.affect(hero, Barrier.class).setShield(e);
			if (hero.sprite != null) hero.sprite.showStatusWithIcon(0x66CCFF, Integer.toString(e), FloatingText.SHIELDING);
			Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
			hero.spendAndNext(1f);
		}
	}

	@Override
	public String name() {
		return Messages.get(this, forca >= 2 ? "name_reforcado" : "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", Math.round(10 * (1 + forca)));
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing(0x66CCFF, 1.5f);
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
		return 20 * forca;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("forca", forca);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		forca = bundle.getInt("forca");
	}
}
