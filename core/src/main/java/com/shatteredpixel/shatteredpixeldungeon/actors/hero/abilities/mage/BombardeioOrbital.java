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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.hull.PedidoSuprimento;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

//Shattered Hull: bombardeio orbital do Exterminador, no lugar da explosão elemental do Mago
public class BombardeioOrbital extends ArmorAbility {

	{
		baseChargeUse = 35f;
	}

	public static final int CAPSULAS = 5;
	public static final int RAIO = 2;

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	public int targetedPos(Char user, int dst) {
		return dst;
	}

	public static int espera(Hero hero){
		int p = hero.pointsInTalent(Talent.AVISO_CURTO);
		if (p >= 4) return 1;
		if (p >= 2) return 2;
		return 3;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		if (target == null) return;
		if (!PedidoSuprimento.valido(hero, target)){
			GLog.w(Messages.get(PedidoSuprimento.class, "fora_alcance"));
			return;
		}

		//a primeira cápsula cai no alvo, as outras em pontos sorteados do raio
		ArrayList<Integer> area = new ArrayList<>();
		PathFinder.buildDistanceMap(target, BArrayAberto(), RAIO);
		for (int i = 0; i < PathFinder.distance.length; i++){
			if (PathFinder.distance[i] <= RAIO && i != target && !Dungeon.level.solid[i]) area.add(i);
		}
		int[] cells = new int[CAPSULAS];
		cells[0] = target;
		for (int i = 1; i < CAPSULAS; i++){
			cells[i] = area.isEmpty() ? target : area.remove(Random.Int(area.size()));
		}

		Salva s = Buff.append(hero, Salva.class);
		s.cells = cells;
		s.turnos = espera(hero);
		for (int c : cells) GameScene.targetedCell(c, 0xFF3300, s.turnos);

		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();
		Sample.INSTANCE.play(Assets.Sounds.BEACON);
		GLog.i(Messages.get(this, "chamado"));
		if (hero.sprite != null) hero.sprite.zap(target);
		hero.spendAndNext(Actor.TICK);
	}

	private static boolean[] BArrayAberto(){
		boolean[] b = new boolean[Dungeon.level.length()];
		for (int i = 0; i < b.length; i++) b[i] = !Dungeon.level.solid[i];
		return b;
	}

	public static void cair(int cell){
		Hero hero = Dungeon.hero;
		if (hero == null) return;
		int dano = PedidoSuprimento.dano(hero);
		atingir(hero, cell, dano);
		int pf = hero.pointsInTalent(Talent.FRAGMENTACAO);
		if (pf > 0){
			int estilhaco = Math.max(1, Math.round(dano * 0.15f * pf));
			for (int n : PathFinder.NEIGHBOURS8){
				int p = cell + n;
				if (p >= 0 && p < Dungeon.level.length() && !Dungeon.level.solid[p]) atingir(hero, p, estilhaco);
			}
		}
		if (Dungeon.level.heroFOV[cell]){
			CellEmitter.center(cell).burst(BlastParticle.FACTORY, 20);
			CellEmitter.get(cell).burst(SmokeParticle.FACTORY, 4);
		}
	}

	private static void atingir(Hero hero, int cell, int dano){
		Char ch = Actor.findChar(cell);
		if (ch == null) return;
		if (ch == hero || ch.alignment == Char.Alignment.ALLY){
			//perímetro seguro: 25% a menos por ponto, imune com 4
			float fator = 1f - 0.25f * hero.pointsInTalent(Talent.PERIMETRO_SEGURO);
			if (fator <= 0) return;
			dano = Math.round(dano * fator);
		}
		if (dano > 0) ch.damage(dano, BombardeioOrbital.class);
		if (ch == hero && !hero.isAlive()){
			Dungeon.fail(BombardeioOrbital.class);
			GLog.n(Messages.get(BombardeioOrbital.class, "ondeath"));
		}
	}

	public static class Salva extends Buff {
		{
			actPriority = HERO_PRIO + 1;
		}

		public int[] cells = new int[0];
		public int turnos;

		@Override
		public boolean act() {
			turnos--;
			if (turnos <= 0){
				for (int c : cells) cair(c);
				Sample.INSTANCE.play(Assets.Sounds.BLAST, 1.5f);
				if (Camera.main != null) Camera.main.shake(4, 0.5f);
				detach();
			} else {
				spend(TICK);
			}
			return true;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put("cells", cells);
			bundle.put("turnos", turnos);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			cells = bundle.getIntArray("cells");
			turnos = bundle.getInt("turnos");
		}
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc") + "\n\n" + Messages.get(this, "cost", (int)baseChargeUse);
	}

	@Override
	public int icon() {
		return HeroIcon.ELEMENTAL_BLAST;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.AVISO_CURTO, Talent.FRAGMENTACAO, Talent.PERIMETRO_SEGURO, Talent.HEROIC_ENERGY};
	}
}
