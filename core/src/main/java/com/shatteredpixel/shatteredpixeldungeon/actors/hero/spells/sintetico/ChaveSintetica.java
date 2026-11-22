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

import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SkeletonKey;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

//Shattered Hull: o cartão mestre do Sintético. Reaproveita a chave caveira, com cada carga dela
//valendo uma quantidade de energia.
public class ChaveSintetica extends SkeletonKey {

	public enum Modo { ABRIR, TRANCAR, BARREIRA }

	private Modo modo;
	private int unidade;
	private int antes;
	private HabilidadeSintetica habilidade;
	private Hero dono;

	public static void usar(Hero hero, Modo modo, int unidade, HabilidadeSintetica hab){
		ChaveSintetica k = new ChaveSintetica();
		k.modo = modo;
		k.unidade = unidade;
		k.habilidade = hab;
		k.dono = hero;
		k.charge = unidade <= 0 ? 99 : (int)(Sintetico.energia(hero) / unidade);
		k.antes = k.charge;
		curUser = hero;
		GameScene.selectCell(k.seletor());
	}

	private boolean tranca(int cell){
		int t = Dungeon.level.map[cell];
		return t == Terrain.LOCKED_DOOR || t == Terrain.CRYSTAL_DOOR || t == Terrain.LOCKED_EXIT
				|| (Dungeon.level.heaps.get(cell) != null && (Dungeon.level.heaps.get(cell).type == Heap.Type.LOCKED_CHEST
				|| Dungeon.level.heaps.get(cell).type == Heap.Type.CRYSTAL_CHEST));
	}

	private boolean porta(int cell){
		int t = Dungeon.level.map[cell];
		return t == Terrain.DOOR || t == Terrain.OPEN_DOOR || t == Terrain.HERO_LKD_DR;
	}

	//cargas da antiga chave caveira para cada alvo
	private int cargasNecessarias(int cell){
		if (modo != Modo.ABRIR) return 2;
		int t = Dungeon.level.map[cell];
		Heap h = Dungeon.level.heaps.get(cell);
		if (t == Terrain.CRYSTAL_DOOR || (h != null && h.type == Heap.Type.CRYSTAL_CHEST)) return 5;
		if (h != null && h.type == Heap.Type.LOCKED_CHEST) return 2;
		return 1;
	}

	private CellSelector.Listener seletor(){
		return new CellSelector.Listener() {
			@Override
			public void onSelect(Integer cell) {
				if (cell == null) return;
				boolean ok;
				switch (modo){
					case ABRIR: ok = tranca(cell); break;
					case TRANCAR: ok = porta(cell) && Dungeon.level.adjacent(cell, dono.pos); break;
					default: ok = !tranca(cell) && !porta(cell); break;
				}
				if (!ok){
					GLog.w(Messages.get(habilidade, "alvo_errado"));
					return;
				}
				int precisa = cargasNecessarias(cell);
				if (charge < precisa){
					GLog.w(Messages.get(HabilidadeSintetica.class, "sem_energia", precisa * unidade));
					return;
				}
				targeter.onSelect(cell);
			}
			@Override
			public String prompt() {
				return Messages.get(habilidade, "prompt");
			}
		};
	}

	//a chave caveira chama isto logo depois de gastar as cargas
	@Override
	public void gainExp(int xpGain) {
		int gasto = antes - charge;
		antes = charge;
		if (gasto > 0 && unidade > 0) Sintetico.gastar(dono, gasto * unidade);
		Sintetico.registrarUso(dono, habilidade);
	}
}
