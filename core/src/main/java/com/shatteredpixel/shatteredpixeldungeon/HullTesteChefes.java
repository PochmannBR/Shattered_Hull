/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
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
 *
 * Shattered Hull
 * Copyright (C) 2026 Pablo Pochmann / Insane Goblin
 */

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM300;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.KingsCrown;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.LeatherArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ScaleArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.hull.Magazine;
import com.shatteredpixel.shatteredpixeldungeon.items.hull.ServiceRifle;
import com.shatteredpixel.shatteredpixeldungeon.items.hull.SuppressedPistol;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.AssassinsBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dirk;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Longsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shortsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword;

//Shattered Hull: atalho das builds de teste para começar a missão direto no setor de um chefe,
//com o herói montado do jeito que o simulador de balanceamento espera para aquele ponto.
//Só aparece com a loja simulada, que é a das builds de teste e do desktop.
public class HullTesteChefes {

	public static final int[] SETORES = {5, 10, 15, 20, 25};

	//setor do chefe escolhido na tela de seleção; zero quando a missão é normal
	public static int pendente = 0;

	public static boolean disponivel(){
		return HullStore.instance != null && HullStore.instance.simulada();
	}

	public static Mob chefe( int setor ){
		switch (setor){
			case 5: default: return new Goo();
			case 10: return new Tengu();
			case 15: return new DM300();
			case 20: return new DwarfKing();
			case 25: return new YogDzewa();
		}
	}

	//chamado pelo InterlevelScene logo depois do Dungeon.init() de uma missão nova
	public static boolean aplicar(){
		if (pendente <= 0) return false;
		int d = pendente;
		pendente = 0;
		Dungeon.depth = d;
		Statistics.deepestFloor = d;
		equipar(Dungeon.hero, d);
		return true;
	}

	public static void equipar( Hero h, int d ){
		h.lvl = Math.round(1 + 0.9f * (d - 1));
		h.exp = 0;
		h.STR = 10 + (2 * (d - 1)) / 5;
		h.updateHT(true);
		h.HP = h.HT;

		int tier = Math.min(5, 1 + (d - 1) / 5);
		int aprimoramentos = (3 * (d - 1)) / 5;
		int armUp = Math.round(aprimoramentos * 0.4f);
		int corpoUp = Math.round(aprimoramentos * 0.4f);
		int distUp = Math.max(0, aprimoramentos - armUp - corpoUp);

		Armor arm;
		switch (tier) {
			case 1: arm = new ClothArmor(); break;
			case 2: arm = new LeatherArmor(); break;
			case 3: arm = new MailArmor(); break;
			case 4: arm = new ScaleArmor(); break;
			default: arm = new PlateArmor(); break;
		}
		arm.level(armUp);
		arm.identify();
		if (h.belongings.armor != null){
			BrokenSeal seal = h.belongings.armor.checkSeal();
			if (seal != null){
				h.belongings.armor.detachSeal();
				arm.affixSeal(seal);
			}
		}
		h.belongings.armor = arm;

		MeleeWeapon mw;
		boolean operador = h.heroClass == HeroClass.ROGUE;
		switch (tier) {
			case 1: mw = operador ? new Dagger() : new WornShortsword(); break;
			case 2: mw = operador ? new Dirk() : new Shortsword(); break;
			case 3: mw = operador ? new Dirk() : new Sword(); break;
			case 4: mw = operador ? new AssassinsBlade() : new Longsword(); break;
			default: mw = operador ? new AssassinsBlade() : new Greatsword(); break;
		}
		mw.level(corpoUp);
		mw.identify();

		//a arma de fogo vai para a mochila e a arma corpo a corpo fica na mão
		Weapon fogo = null;
		if (h.belongings.weapon instanceof ServiceRifle){
			fogo = (Weapon) h.belongings.weapon;
			h.belongings.weapon = null;
			fogo.collect();
		} else {
			fogo = h.belongings.getItem(SuppressedPistol.class);
		}
		h.belongings.weapon = mw;
		if (fogo != null){
			fogo.level(distUp);
			fogo.identify();
			Dungeon.quickslot.setSlot(0, fogo);
		}

		new Magazine().quantity(3).collect();
		PotionOfHealing cura = new PotionOfHealing();
		cura.quantity(3).identify();
		cura.collect();
		new Food().quantity(2).collect();

		//especialização e habilidade da armadura chegam com os itens dos chefes anteriores
		if (d >= 15) new TengusMask().collect();
		if (d >= 25) new KingsCrown().collect();
	}
}
