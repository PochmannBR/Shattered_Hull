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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

//Shattered Hull: munição especial do pedido de suprimento. Ao usar, os próximos golpes e
//disparos de arma especial aplicam o efeito.
public abstract class MunicaoEspecial extends Item {

	public static final String AC_USAR = "USAR";

	{
		image = ItemSpriteSheet.MAGAZINE;
		stackable = true;
		defaultAction = AC_USAR;
	}

	public abstract int efeito();

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_USAR);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_USAR)){
			detach(hero.belongings.backpack);
			Ativa a = Buff.affect(hero, Ativa.class);
			a.efeito = efeito();
			a.disparos = disparosPorCarga(hero);
			Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
			GLog.i(Messages.get(MunicaoEspecial.class, "carregada", name()));
			hero.spendAndNext(1f);
			updateQuickslot();
		}
	}

	public static int disparosPorCarga(Hero hero){
		return 5 + 2 * hero.pointsInTalent(Talent.MUNICAO_DURADOURA);
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
		return 15 * quantity;
	}

	public static final int FOGO = 0, GELO = 1, CHOQUE = 2;

	public static class Incendiaria extends MunicaoEspecial {
		{ image = ItemSpriteSheet.MUNICAO_INCENDIARIA; }
		@Override public int efeito() { return FOGO; }
		@Override public ItemSprite.Glowing glowing() { return new ItemSprite.Glowing(0xFF6600, 1f); }
	}

	public static class Criogenica extends MunicaoEspecial {
		{ image = ItemSpriteSheet.MUNICAO_CRIOGENICA; }
		@Override public int efeito() { return GELO; }
		@Override public ItemSprite.Glowing glowing() { return new ItemSprite.Glowing(0x66CCFF, 1f); }
	}

	public static class Choque extends MunicaoEspecial {
		{ image = ItemSpriteSheet.MUNICAO_CHOQUE; }
		@Override public int efeito() { return CHOQUE; }
		@Override public ItemSprite.Glowing glowing() { return new ItemSprite.Glowing(0xFFFF66, 1f); }
	}

	//aplica o efeito num alvo; usado pelos golpes, pelas armas especiais e pela rajada do transmissor
	public static void aplicar(int efeito, Char alvo){
		if (alvo == null || !alvo.isAlive()) return;
		switch (efeito){
			case FOGO:
				Buff.affect(alvo, Burning.class).reignite(alvo);
				break;
			case GELO:
				Buff.prolong(alvo, Chill.class, 4f);
				break;
			case CHOQUE:
				alvo.damage(Random.NormalIntRange(2, 4 + Dungeon.hero.lvl / 3), new MunicaoEspecial.Choque());
				if (alvo.isAlive()) Buff.prolong(alvo, Paralysis.class, 1f);
				if (alvo.sprite != null) alvo.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3);
				break;
		}
	}

	//munição carregada no herói
	public static class Ativa extends Buff {
		{
			type = buffType.POSITIVE;
		}

		public int efeito = FOGO;
		public int disparos = 0;

		//chamado nos golpes e disparos do herói
		public void disparar(Char alvo){
			if (alvo == null || alvo == target || alvo.alignment == Char.Alignment.ALLY) return;
			aplicar(efeito, alvo);
			disparos--;
			if (disparos <= 0) detach();
		}

		@Override
		public int icon() {
			return BuffIndicator.FIRE;
		}

		@Override
		public void tintIcon(Image icon) {
			switch (efeito){
				case GELO: icon.hardlight(0.4f, 0.8f, 1f); break;
				case CHOQUE: icon.hardlight(1f, 1f, 0.4f); break;
				default: icon.resetColor(); break;
			}
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(disparos);
		}

		@Override
		public String name() {
			return Messages.get(this, "name_" + efeito);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc_" + efeito, disparos);
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put("efeito", efeito);
			bundle.put("disparos", disparos);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			efeito = bundle.getInt("efeito");
			disparos = bundle.getInt("disparos");
		}
	}
}
